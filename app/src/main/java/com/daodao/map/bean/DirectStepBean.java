package com.daodao.map.bean;

/**
 * Created by lzd on 2016/11/6.
 */

public class DirectStepBean {

    private TextValueBean distance;

    private TextValueBean duration;

    private LatLngBean end_location;

    private String html_instructions;

    private DirectPolylineBean polyline;

    private LatLngBean start_location;

    private String travel_mode;

    public TextValueBean getDistance() {
        return distance;
    }

    public void setDistance(TextValueBean distance) {
        this.distance = distance;
    }

    public TextValueBean getDuration() {
        return duration;
    }

    public void setDuration(TextValueBean duration) {
        this.duration = duration;
    }

    public LatLngBean getEnd_location() {
        return end_location;
    }

    public void setEnd_location(LatLngBean end_location) {
        this.end_location = end_location;
    }

    public String getHtml_instructions() {
        return html_instructions;
    }

    public void setHtml_instructions(String html_instructions) {
        this.html_instructions = html_instructions;
    }

    public DirectPolylineBean getPolyline() {
        return polyline;
    }

    public void setPolyline(DirectPolylineBean polyline) {
        this.polyline = polyline;
    }

    public LatLngBean getStart_location() {
        return start_location;
    }

    public void setStart_location(LatLngBean start_location) {
        this.start_location = start_location;
    }

    public String getTravel_mode() {
        return travel_mode;
    }

    public void setTravel_mode(String travel_mode) {
        this.travel_mode = travel_mode;
    }
}
