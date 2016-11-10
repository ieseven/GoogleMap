package com.daodao.map.bean;

import java.util.ArrayList;

/**
 * Created by lzd on 2016/11/6.
 */

public class DirectLegBean {

    private TextValueBean distance;

    private TextValueBean duration;

    private String end_address;

    private LatLngBean end_location;

    private String start_address;

    private LatLngBean start_location;

    private ArrayList<DirectStepBean> steps;

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

    public String getEnd_address() {
        return end_address;
    }

    public void setEnd_address(String end_address) {
        this.end_address = end_address;
    }

    public LatLngBean getEnd_location() {
        return end_location;
    }

    public void setEnd_location(LatLngBean end_location) {
        this.end_location = end_location;
    }

    public String getStart_address() {
        return start_address;
    }

    public void setStart_address(String start_address) {
        this.start_address = start_address;
    }

    public LatLngBean getStart_location() {
        return start_location;
    }

    public void setStart_location(LatLngBean start_location) {
        this.start_location = start_location;
    }

    public ArrayList<DirectStepBean> getSteps() {
        return steps;
    }

    public void setSteps(ArrayList<DirectStepBean> steps) {
        this.steps = steps;
    }
}
