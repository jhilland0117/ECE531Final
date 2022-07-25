package com.hilland.domain;

import java.time.Instant;

/**
 *
 * @author jhilland
 */
public class State implements Thermostat {

    // true is on, false is off
    private boolean on;
    private Instant time;

    public State() {
    }

    public State(boolean on) {
        this.on = on;
        this.time = Instant.now();
    }

    public boolean isOn() {
        return on;
    }

    public void setOn(boolean on) {
        this.on = on;
    }

    public Instant getTime() {
        return time;
    }

    public void setTime(Instant time) {
        this.time = time;
    }

}
