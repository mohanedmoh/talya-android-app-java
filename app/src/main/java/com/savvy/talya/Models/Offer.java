package com.savvy.talya.Models;

public class Offer {
    String id, start_date, end_date, currency, currency_name, advance, added_value, month_no, plan_id, plan_name, type;

    public Offer() {
        added_value = "";
        month_no = "";
        advance = "";
        currency = "";
        currency_name = "";
        id = "";
    }

    public String getEnd_date() {
        return end_date;
    }

    public void setEnd_date(String end_date) {
        this.end_date = end_date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStart_date() {
        return start_date;
    }

    public void setStart_date(String start_date) {
        this.start_date = start_date;
    }

    public String getAdded_value() {
        return added_value;
    }

    public void setAdded_value(String added_value) {
        this.added_value = added_value;
    }

    public String getAdvance() {
        return advance;
    }

    public void setAdvance(String advance) {
        this.advance = advance;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCurrency_name() {
        return currency_name;
    }

    public void setCurrency_name(String currency_name) {
        this.currency_name = currency_name;
    }

    public String getMonth_no() {
        return month_no;
    }

    public void setMonth_no(String month_no) {
        this.month_no = month_no;
    }

    public String getPlan_id() {
        return plan_id;
    }

    public void setPlan_id(String plan_id) {
        this.plan_id = plan_id;
    }

    public String getPlan_name() {
        return plan_name;
    }

    public void setPlan_name(String plan_name) {
        this.plan_name = plan_name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
