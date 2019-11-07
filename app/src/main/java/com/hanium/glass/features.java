package com.hanium.glass;

import org.json.JSONArray;

import java.util.ArrayList;

class Geometry {
    private Double longitude;
    private Double latitude;

    public Double getLongitude() {
        return longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }
}

public class features {
    private String type;
    private Geometry coordinates = new Geometry();

    public String getType() {
        return type;
    }

    public Geometry getCoordinates() {
        return coordinates;
    }

    public void setType(String type) {
        this.type = type;
    }


    public void setCoordinates(Geometry coordinates) {
        this.coordinates = coordinates;
    }

    public void setCoordinates(String geoString) {
        geoString = geoString.replace("[", "");
        geoString = geoString.replace("]", "");

        String geoArr[] = geoString.split(",");

        this.coordinates.setLongitude(Double.parseDouble(geoArr[0]));
        this.coordinates.setLatitude(Double.parseDouble(geoArr[1]));
    }
}
