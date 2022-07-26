package com.hilland;

import com.google.gson.Gson;
import com.hilland.domain.State;
import com.hilland.domain.Temperature;
import com.hilland.domain.Thermostat;
import fi.iki.elonen.NanoHTTPD;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.sql.Timestamp;

import static fi.iki.elonen.NanoHTTPD.MIME_PLAINTEXT;
import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

/**
 *
 * @author jhilland
 */
public final class CurlCommandsUtil {

    private static final String DELIM = ",";
    private static final String TYPE_DELIM = ":";
    private static final String STATE = "state";
    private static final String TEMP = "temp";
    private static final String REPORT = "report";

    private CurlCommandsUtil() {
    }

    public static NanoHTTPD.Response performGet(NanoHTTPD.IHTTPSession session) {
        String jsonResp = null;
        String route = getRoute(session.getUri());
        String param = getIndex(session.getUri());
        System.out.println("route: " + route + " param " + param + "\n");
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
            }

            return newFixedLengthResponse(jsonResp);
        }
        return failedAttempt("improper get url path");
    }

    public static NanoHTTPD.Response performPost(NanoHTTPD.IHTTPSession session) {
        try {
            session.parseBody(new HashMap<>());
            Thermostat thermostat = parseTempParams(session.getQueryParameterString());

            if (thermostat == null) {
                return newFixedLengthResponse("temp or time values unsupported");
            }
            String result = JDBCConnection.handleType(thermostat);
            return newFixedLengthResponse(result);
        } catch (IOException | NanoHTTPD.ResponseException e) {
            return failedAttempt("unable to commit post");
        }
    }

    public static NanoHTTPD.Response performDelete(NanoHTTPD.IHTTPSession session) {
        String result = JDBCConnection.deleteTemp(getIndex(session.getUri()));
        return newFixedLengthResponse(result);
    }

    public static NanoHTTPD.Response failedAttempt(String message) {
        return newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_FOUND, MIME_PLAINTEXT,
                message);
    }

    // expected input is state:true|false or temp:time,temp
    private static Thermostat parseTempParams(String input) {
        String[] typeSeparation = input.split(TYPE_DELIM);
        String type = typeSeparation[0];
        String params = typeSeparation[1];

        System.out.println("TYPE: " + type + ", params: " + params + "\n");

        if (type.equals(STATE)) {
            State state = new State();
            state.setOn(Boolean.parseBoolean(params));
        } else if (type.equals(TEMP)) {
            String[] values = params.split(DELIM);
            int time = Integer.parseInt(values[0]);
            int temp = Integer.parseInt(values[1]);
            if (time > 24 || time < 0 || temp < 0) {
                return null;
            }
            return new Temperature(temp, time);
        } else if (type.equals(REPORT)) {

        }
        return null;
    }

    private static String getIndex(String param) {
        return param.replaceAll("[^0-9]", "");
    }

    private static String getRoute(String param) {
        if (param.contains(TEMP)) {
            return TEMP;
        } else if (param.contains(STATE)) {
            return STATE;
        }
        return null;
    }
}
