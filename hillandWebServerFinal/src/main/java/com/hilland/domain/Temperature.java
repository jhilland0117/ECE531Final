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
    private int temp2;
    private Timestamp date;
    private String setting;

    public Temperature() {
    }

    public Temperature(int temp, int temp2, String setting) {
        this.temp = temp;
        this.temp2 = temp2;
        this.setting = setting;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getTemp() {
        return temp;
    }

    public void setTemp(int temp) {
        this.temp = temp;
    }

    public int getTemp2() {
        return temp2;
    }

    public void setTemp2(int temp2) {
        this.temp2 = temp2;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

    public String getSetting() {
        return setting;
    }

    public void setSetting(String setting) {
        this.setting = setting;
    }

}
