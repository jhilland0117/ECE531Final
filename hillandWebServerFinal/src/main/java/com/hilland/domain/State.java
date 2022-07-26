package com.hilland.domain;

import java.sql.Timestamp;

/**
 *
 * @author jhilland
 */
public class State implements Thermostat {

    // true is on, false is off
    private int on;
    private Timestamp date;

    public static State buildState(int value) {
        State state = new State();
        state.setOn(value);
        return state;
    }

    public int getOn() {
        return on;
    }

    public void setOn(int on) {
        this.on = on;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

}
