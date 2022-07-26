package com.hilland.domain;

import java.sql.Timestamp;

/**
 *
 * @author jhilland
 */
public class State implements Thermostat {

    // true is on, false is off
    private boolean on = true;
    private Timestamp date;

    public static State buildState(boolean value) {
        State state = new State();
        state.setOn(value);
        return state;
    }
    
    public boolean isOn() {
        return on;
    }

    public void setOn(boolean on) {
        this.on = on;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

}
