package com.savvy.talya.Models;

import java.io.Serializable;

public class PlotPayament implements Serializable {
    String payment_id, payment_no, payment_date, op_id, payment_amount, chaque_no, chaque_bank, paid, recipt, recipt_date, notification, reason, canceled, op_type, rateID;

    public String getCanceled() {
        return canceled;
    }

    public void setCanceled(String canceled) {
        this.canceled = canceled;
    }

    public String getChaque_bank() {
        return chaque_bank;
    }

    public void setChaque_bank(String chaque_bank) {
        this.chaque_bank = chaque_bank;
    }

    public String getPayment_id() {
        return payment_id;
    }

    public void setPayment_id(String payment_id) {
        this.payment_id = payment_id;
    }

    public String getPayment_no() {
        return payment_no;
    }

    public void setPayment_no(String payment_no) {
        this.payment_no = payment_no;
    }

    public String getOp_id() {
        return op_id;
    }

    public void setOp_id(String op_id) {
        this.op_id = op_id;
    }

    public String getPayment_amount() {
        return payment_amount;
    }

    public void setPayment_amount(String payment_amount) {
        this.payment_amount = payment_amount;
    }

    public String getPayment_date() {
        return payment_date;
    }

    public void setPayment_date(String payment_date) {
        this.payment_date = payment_date;
    }

    public String getChaque_no() {
        return chaque_no;
    }

    public void setChaque_no(String chaque_no) {
        this.chaque_no = chaque_no;
    }

    public String getNotification() {
        return notification;
    }

    public void setNotification(String notification) {
        this.notification = notification;
    }

    public String getOp_type() {
        return op_type;
    }

    public void setOp_type(String op_type) {
        this.op_type = op_type;
    }

    public String getPaid() {
        return paid;
    }

    public void setPaid(String paid) {
        this.paid = paid;
    }

    public String getRateID() {
        return rateID;
    }

    public void setRateID(String rateID) {
        this.rateID = rateID;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getRecipt() {
        return recipt;
    }

    public void setRecipt(String recipt) {
        this.recipt = recipt;
    }

    public String getRecipt_date() {
        return recipt_date;
    }

    public void setRecipt_date(String recipt_date) {
        this.recipt_date = recipt_date;
    }
}
