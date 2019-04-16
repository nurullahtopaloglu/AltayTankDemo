package com.uren.altaytankdemo.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class QrResponse implements Serializable {

    private String QRdata;
    private String returnCode;
    private String returnDesc;

    public String getQRdata ()
    {
        return QRdata;
    }

    public void setQRdata (String QRdata)
    {
        this.QRdata = QRdata;
    }

    public String getReturnCode ()
    {
        return returnCode;
    }

    public void setReturnCode (String returnCode)
    {
        this.returnCode = returnCode;
    }

    public String getReturnDesc ()
    {
        return returnDesc;
    }

    public void setReturnDesc (String returnDesc)
    {
        this.returnDesc = returnDesc;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [QRdata = "+QRdata+", returnCode = "+returnCode+", returnDesc = "+returnDesc+"]";
    }
}
