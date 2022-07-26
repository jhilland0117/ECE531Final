package com.hilland.domain;

import java.sql.Timestamp;

/**
 *
 * @author jhilland
 */
public class State implements Thermostat {

    // true is on, false is off
    private boolean on;
    private Timestamp date;

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
