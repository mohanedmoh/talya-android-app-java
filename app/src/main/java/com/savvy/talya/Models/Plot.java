package com.savvy.talya.Models;

import java.io.Serializable;
import java.util.ArrayList;

public class Plot implements Serializable {
    String id, plot_no, plot_area, cat, blueprint_id, meter_price, price_sdg, price_usd, status, blueprint_name, emp_action, emp_follower, current_price, current_meter_price, basic, total, extra_amount, extra_quantity, status_id, status_description, allowOffer, isRegistered;
    String client_id, client_name;
    ArrayList<PlotPayament> plotPayament;
    String investments;

    public Plot() {
        allowOffer = "0";
    }

    public String getIsRegistered() {
        return isRegistered;
    }

    public void setIsRegistered(String isRegistered) {
        this.isRegistered = isRegistered;
    }

    public String getAllowOffer() {
        return allowOffer;
    }

    public void setAllowOffer(String allowOffer) {
        this.allowOffer = allowOffer;
    }

    public String getStatus_id() {
        return status_id;
    }

    public void setStatus_id(String status_id) {
        this.status_id = status_id;
    }

    public String getStatus_description() {
        return status_description;
    }

    public void setStatus_description(String status_description) {
        this.status_description = status_description;
    }

    public String getBasic() {
        return basic;
    }

    public void setBasic(String basic) {
        this.basic = basic;
    }

    public String getExtra_amount() {
        return extra_amount;
    }

    public void setExtra_amount(String extra_amount) {
        this.extra_amount = extra_amount;
    }

    public String getExtra_quantity() {
        return extra_quantity;
    }

    public void setExtra_quantity(String extra_quantity) {
        this.extra_quantity = extra_quantity;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getCurrent_meter_price() {
        return current_meter_price;
    }

    public void setCurrent_meter_price(String current_meter_price) {
        this.current_meter_price = current_meter_price;
    }

    public String getCurrent_price() {
        return current_price;
    }

    public void setCurrent_price(String current_price) {
        this.current_price = current_price;
    }

    public String getEmp_action() {
        return emp_action;
    }

    public void setEmp_action(String emp_action) {
        this.emp_action = emp_action;
    }

    public String getEmp_follower() {
        return emp_follower;
    }

    public void setEmp_follower(String emp_follower) {
        this.emp_follower = emp_follower;
    }

    public String getInvestments() {
        return investments;
    }

    public void setInvestments(String investments) {
        this.investments = investments;
    }

    public String getClient_id() {
        return client_id;
    }

    public void setClient_id(String client_id) {
        this.client_id = client_id;
    }

    public String getClient_name() {
        return client_name;
    }

    public void setClient_name(String client_name) {
        this.client_name = client_name;
    }

    public String getBlueprint_name() {
        return blueprint_name;
    }

    public void setBlueprint_name(String blueprint_name) {
        this.blueprint_name = blueprint_name;
    }

    public ArrayList<PlotPayament> getPlotPayament() {
        return plotPayament;
    }

    public void setPlotPayament(ArrayList<PlotPayament> plotPayament) {
        this.plotPayament = plotPayament;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPrice_sdg() {
        return price_sdg;
    }

    public void setPrice_sdg(String price_sdg) {
        this.price_sdg = price_sdg;
    }

    public String getPrice_usd() {
        return price_usd;
    }

    public void setPrice_usd(String price_usd) {
        this.price_usd = price_usd;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBlueprint_id() {
        return blueprint_id;
    }

    public void setBlueprint_id(String blueprint_id) {
        this.blueprint_id = blueprint_id;
    }

    public String getCat() {
        return cat;
    }

    public void setCat(String cat) {
        this.cat = cat;
    }

    public String getPlot_area() {
        return plot_area;
    }

    public void setPlot_area(String plot_area) {
        this.plot_area = plot_area;
    }

    public String getPlot_no() {
        return plot_no;
    }

    public void setPlot_no(String plot_no) {
        this.plot_no = plot_no;
    }

    public String getMeter_price() {
        return meter_price;
    }

    public void setMeter_price(String meter_price) {
        this.meter_price = meter_price;
    }
}
