package com.savvy.talya.Models;

import java.io.Serializable;

public class BluePrint implements Serializable {
    String id, name, image, maps_url, note, plot_total, plot_rest, plot_sold;

    public String getPlot_rest() {
        return plot_rest;
    }

    public void setPlot_rest(String plot_rest) {
        this.plot_rest = plot_rest;
    }

    public String getPlot_sold() {
        return plot_sold;
    }

    public void setPlot_sold(String plot_sold) {
        this.plot_sold = plot_sold;
    }

    public String getPlot_total() {
        return plot_total;
    }

    public void setPlot_total(String plot_total) {
        this.plot_total = plot_total;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getMaps_url() {
        return maps_url;
    }

    public void setMaps_url(String maps_url) {
        this.maps_url = maps_url;
    }
}
