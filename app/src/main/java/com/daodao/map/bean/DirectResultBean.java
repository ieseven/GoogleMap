package com.daodao.map.bean;

import java.util.ArrayList;

/**
 * Created by lzd on 2016/11/6.
 */

public class DirectResultBean {

    private String status;

    ArrayList<DirectRoutBean> routes;

    public ArrayList<DirectRoutBean> getRoutes() {
        return routes;
    }

    public void setRoutes(ArrayList<DirectRoutBean> routes) {
        this.routes = routes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
