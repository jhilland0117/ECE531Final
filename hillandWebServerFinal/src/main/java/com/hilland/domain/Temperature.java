package com.hilland.domain;

import java.sql.Timestamp;

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
    private Timestamp date;

    public Temperature() {
    }

    public Temperature(int temp, int time) {
        this.temp = temp;
    }

    public Temperature(int temp) {
        this.temp = temp;
    }

    public Long getId() {
        return id;
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

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

}
