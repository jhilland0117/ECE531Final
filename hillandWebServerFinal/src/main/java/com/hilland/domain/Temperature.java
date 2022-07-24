package com.hilland.domain;

/**
 *
 * @author jhilland
 */
public class Temperature {

    private Long id;
    private int temp;

    public Temperature() {
    }

    public Temperature(int temp) {
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

}
