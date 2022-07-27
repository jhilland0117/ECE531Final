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
import java.util.Calendar;

import static fi.iki.elonen.NanoHTTPD.MIME_PLAINTEXT;
import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;
import static com.hilland.JDBCConnection.addReport;
import static com.hilland.JDBCConnection.addState;
import static com.hilland.JDBCConnection.updateTemp;

/**
 *
 * @author jhilland
 */
public final class CurlCommandsUtil {

    private static final String DELIM = ",";
    private static final String DEC = ".000000";
    private static final String TYPE_DELIM = ":";
    private static final String STATE = "state";
    private static final String TEMP = "temps";
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
                    jsonResp = Boolean.toString(true);
                }
                jsonResp = Boolean.toString(state.isOn());
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
                result = updateTemp((Temperature) thermostat);
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
    
    private static String decodePeriod() {
        Calendar time = Calendar.getInstance();
        int hour = time.get(Calendar.HOUR_OF_DAY);
        if (hour >= 18) {
            return "EVENING";
        } else if (hour >= 12) {
            return "AFTERNOON";
        } else {
            return "MORNING";
        }
    }
    
    private static Temperature getTemperatureSetting() {
        String timeofday = decodePeriod();
        return JDBCConnection.getTemperatureSetting(timeofday);
    }

    private static String handleTemperatureChange(Report reportedTemp) {
        Temperature setting = getTemperatureSetting();
        State currentState = JDBCConnection.getState();
        
        if (currentState.isOn()) {
            System.out.println("ON " + reportedTemp.getTemp() + " " + setting.getTemp2());
            if (reportedTemp.getTemp() > setting.getTemp2()) {
                System.out.println("TURN OFF");
                return JDBCConnection.updateState(false);
            }
        } else {
            System.out.println("OFF " + reportedTemp.getTemp() + " " + setting.getTemp());
            if (reportedTemp.getTemp() < setting.getTemp()) {
                System.out.println("TURN ON");
                return JDBCConnection.updateState(true);
            }
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

    // temp post requirement example, temp is low, temp2 is high
    // 1,temp,temp2 (morning)
    // 2,temp,temp2 (afternoon)
    // 3,temp,temp2 (evening)
    private static Thermostat parseRouteParams(String input, String route) {
        if (route.equals(TEMP)) {
            String[] values = input.split(DELIM);
            String id = values[0];
            int temp = Integer.parseInt(values[1]);
            int temp2 = Integer.parseInt(values[2]);
            return new Temperature(temp, temp2, id);
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
