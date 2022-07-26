package com.hilland.domain;

import java.sql.Timestamp;

/**
 *
 * @author jhilland
 */
public class State implements Thermostat {

    // true is on, false is off
    private Timestamp date;
    private String value;

    public static State buildState(String value) {
        State state = new State();
        state.setValue(value);
        return state;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

}
