package com.uren.altaytankdemo.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentResponse implements Serializable {

    private String posID;
    private int returnCode;
    private String sessionID;
    private String applicationID;
    private String returnDesc;

    public String getPosID() {
        return posID;
    }

    public void setPosID(String posID) {
        this.posID = posID;
    }

    public int getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(int returnCode) {
        this.returnCode = returnCode;
    }

    public String getSessionID() {
        return sessionID;
    }

    public void setSessionID(String sessionID) {
        this.sessionID = sessionID;
    }

    public String getApplicationID() {
        return applicationID;
    }

    public void setApplicationID(String applicationID) {
        this.applicationID = applicationID;
    }

    public String getReturnDesc() {
        return returnDesc;
    }

    public void setReturnDesc(String returnDesc) {
        this.returnDesc = returnDesc;
    }

    @Override
    public String toString() {
        return "ClassPojo [posID = " + posID + ", returnCode = " + returnCode + ", sessionID = " + sessionID + ", applicationID = " + applicationID + ", returnDesc = " + returnDesc + "]";
    }
}
