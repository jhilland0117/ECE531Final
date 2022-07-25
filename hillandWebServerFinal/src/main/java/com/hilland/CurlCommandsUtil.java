package com.hilland;

import com.google.gson.Gson;
import com.hilland.domain.State;
import com.hilland.domain.Temperature;
import com.hilland.domain.Thermostat;
import fi.iki.elonen.NanoHTTPD;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

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

    private CurlCommandsUtil() {
    }

    public static NanoHTTPD.Response performGet(JDBCConnection connection, NanoHTTPD.IHTTPSession session) {
        String jsonResp = null;
        String param = getIndex(session.getUri());
        Gson gson = new Gson();
        
        // update this to include get from state and temp
        
        if (param != null && !param.equals("")) {
            Temperature temp = connection.getTemp(param);
            if (temp == null) {
                return failedAttempt("temp value was null");
            }
            jsonResp = gson.toJson(temp);
        } else {
            List<Temperature> temps = connection.getAllTemps();
            if (temps.isEmpty()) {
                return failedAttempt("get request has empty results");
            }
            jsonResp = gson.toJson(temps);
        }

        return newFixedLengthResponse(jsonResp);
    }

    public static NanoHTTPD.Response performPost(JDBCConnection connection, NanoHTTPD.IHTTPSession session) {
        try {
            session.parseBody(new HashMap<>());
            Thermostat thermostat = parseTempParams(session.getQueryParameterString());
            
            if (thermostat == null) {
                return newFixedLengthResponse("temp or time values unsupported");
            }
            String result = connection.handleType(thermostat);
            return newFixedLengthResponse(result);
        } catch (IOException | NanoHTTPD.ResponseException e) {
            return failedAttempt("unable to commit post");
        }
    }

    public static NanoHTTPD.Response performDelete(JDBCConnection connection, NanoHTTPD.IHTTPSession session) {
        String result = connection.deleteTemp(getIndex(session.getUri()));
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
            return new State(Boolean.parseBoolean(params));
        } else if (type.equals(TEMP)) {
            String[] values = params.split(DELIM);
            int time = Integer.parseInt(values[0]);
            int temp = Integer.parseInt(values[1]);
            System.out.println("VALUES" + time + " " + temp + "\n");
            if (time > 24 || time < 0 || temp < 0) {
                return null;
            }
            return new Temperature(temp, time);
        }
        return null;
    }

    private static String getIndex(String param) {
        return param.replaceAll("[^0-9]", "");
    }
}
