package com.hilland.domain;

/**
 * create database thermostat;
 *
 * create table temps (id int unsigned not null auto_increment, temp int
 * unsigned not null, primary key(id));
 *
 *
 *
 * @author jhilland
 */
public class Temperature implements Thermostat {

    private Long id;
    private int temp;
    private int time;

    public Temperature() {
    }

    public Temperature(int temp, int time) {
        this.temp = temp;
    }

    public int getTemp() {
        return temp;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setTemp(int temp) {
        this.temp = temp;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

}
