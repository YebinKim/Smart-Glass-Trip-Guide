package com.hanium.glass;

import org.json.JSONArray;

import java.util.ArrayList;

public class features {
    private ArrayList geometry;
    private String type;
    private ArrayList coordinates;
    private String longitude;
    private String latitude;

    public String getType() {
        return type;
    }

    public ArrayList getCoordinates() {
        return coordinates;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setCoordinates(ArrayList coordinates) {
        this.coordinates = coordinates;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }
}
