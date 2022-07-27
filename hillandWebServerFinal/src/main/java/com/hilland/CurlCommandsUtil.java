package com.hilland;

import com.google.gson.Gson;
import com.hilland.domain.Report;
import com.hilland.domain.State;
import com.hilland.domain.Temperature;
import com.hilland.domain.Thermostat;
import fi.iki.elonen.NanoHTTPD;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import static fi.iki.elonen.NanoHTTPD.MIME_PLAINTEXT;
import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;
import static com.hilland.JDBCConnection.addReport;
import static com.hilland.JDBCConnection.addState;
import static com.hilland.JDBCConnection.addTemp;

/**
 *
 * @author jhilland
 */
public final class CurlCommandsUtil {

    private static final String DELIM = ",";
    private static final String DEC = ".00000";
    private static final String TYPE_DELIM = ":";
    private static final String STATE = "state";
    private static final String TEMP = "temp";
    private static final String REPORT = "report";

    private CurlCommandsUtil() {
    }

    public static NanoHTTPD.Response performGet(NanoHTTPD.IHTTPSession session) {
        String jsonResp = null;
        String route = getRoute(session.getUri());
        String param = cleanValue(session.getUri());
        Gson gson = new Gson();

        if (route != null) {
            if (route.equals(TEMP)) {
                if (param != null && !param.equals("")) {
                    Temperature temp = JDBCConnection.getTemp(param);
                    if (temp == null) {
                        return failedAttempt("temp value was null");
                    }
                    jsonResp = gson.toJson(temp);
                } else {
                    List<Temperature> temps = JDBCConnection.getAllTemps();
                    if (temps.isEmpty()) {
                        return failedAttempt("get request has empty results");
                    }
                    jsonResp = gson.toJson(temps);
                }
            } else if (route.equals(STATE)) {
                State state = JDBCConnection.getState();
                if (state == null) {
                    jsonResp = gson.toJson(State.buildState(true));
                }
                jsonResp = gson.toJson(state);
            } else if (route.equals(REPORT)) {
                List<Report> reports = JDBCConnection.getAllReports();
                if (reports.isEmpty()) {
                    return failedAttempt("get request has empty results");
                }
                jsonResp = gson.toJson(reports);
            }

            return newFixedLengthResponse(jsonResp);
        }
        return failedAttempt("improper get url path");
    }

    public static NanoHTTPD.Response performPost(NanoHTTPD.IHTTPSession session) {
        try {
            session.parseBody(new HashMap<>());
            String route = session.getUri().replace("/", "");
            Thermostat thermostat = parseRouteParams(
                    session.getQueryParameterString(),
                    route);

            if (thermostat == null) {
                return newFixedLengthResponse("temp or time values unsupported");
            }

            // TODO: so much cleaner if used visitor pattern
            String result = null;
            if (thermostat instanceof Temperature) {
                result = addTemp((Temperature) thermostat);
            } else if (thermostat instanceof State) {
                result = addState((State) thermostat);
            } else if (thermostat instanceof Report) {
                handleTemperatureChange((Report) thermostat);
                result = addReport((Report) thermostat);
            }

            return newFixedLengthResponse(result);
        } catch (IOException | NanoHTTPD.ResponseException e) {
            return failedAttempt("unable to commit post");
        }
    }

    private static String handleTemperatureChange(Report temperature) {
        if (temperature.getTemp() < 20) {
            return JDBCConnection.updateState(true);
        } else if (temperature.getTemp() > 24) {
            return JDBCConnection.updateState(false);
        }
        return null;
    }

    public static NanoHTTPD.Response performDelete(NanoHTTPD.IHTTPSession session) {
        String route = session.getUri().replace("/", "");
        if (route == TEMP) {
            String result = JDBCConnection.deleteTemp(cleanValue(session.getUri()));
            return newFixedLengthResponse(result);
        } else if (route == REPORT) {
            String result = JDBCConnection.deleteTemp(cleanValue(session.getUri()));
            return newFixedLengthResponse(result);
        }

        return failedAttempt("failed to delete object, make sure correct route");
    }

    public static NanoHTTPD.Response failedAttempt(String message) {
        return newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_FOUND, MIME_PLAINTEXT,
                message);
    }

    // expected input for temp temp:time,temp
    private static Thermostat parseRouteParams(String input, String route) {
        if (route.equals(TEMP)) {
            String[] values = input.split(DELIM);
            String time = values[0];
            int temp = Integer.parseInt(values[1]);
            return new Temperature(temp, time);
        } else if (route.equals(REPORT)) {
            int temp = Integer.parseInt(cleanDecimal(input));
            return Report.buildReport(temp);
        }
        return null;
    }
    
    private static String cleanDecimal(String input) {
        return cleanValue(input.replace(DEC, ""));
    }

    private static String cleanValue(String param) {
        return param.replaceAll("[^0-9]", "");
    }

    private static String getRoute(String param) {
        if (param.contains(TEMP)) {
            return TEMP;
        } else if (param.contains(STATE)) {
            return STATE;
        } else if (param.contains(REPORT)) {
            return REPORT;
        }
        return null;
    }
}
