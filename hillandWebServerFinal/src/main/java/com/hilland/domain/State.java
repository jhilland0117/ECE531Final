package com.hilland.domain;

import java.sql.Timestamp;

/**
 *
 * @author jhilland
 */
public class State implements Thermostat {

    // true is on, false is off
    private String on;
    private Timestamp date;

    public static State buildState(String value) {
        State state = new State();
        state.setOn(value);
        return state;
    }

    public String getOn() {
        return on;
    }

    public void setOn(String on) {
        this.on = on;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

}
