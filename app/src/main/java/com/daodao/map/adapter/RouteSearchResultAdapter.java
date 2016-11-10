package com.daodao.map.adapter;

import android.content.Context;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.daodao.map.R;
import com.daodao.map.bean.DirectLegBean;
import com.daodao.map.bean.DirectRoutBean;
import com.daodao.map.bean.DirectStepBean;

import java.util.ArrayList;

/**
 * Created by lzd on 2016/11/9.
 */

public class RouteSearchResultAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<DirectRoutBean> routes;

    public RouteSearchResultAdapter(Context context, ArrayList<DirectRoutBean> routes) {
        this.context = context;
        this.routes = routes;
    }
    @Override
    public int getCount() {
        if (routes == null) {
            return 0;
        }
        return routes.size();
    }

    @Override
    public Object getItem(int position) {
        return routes.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.adapter_route_result_item, null);
        }
        TextView tvChoice = (TextView) convertView.findViewById(R.id.adapter_route_result_choice_tv);
        TextView tvPath = (TextView) convertView.findViewById(R.id.adapter_route_result_path_tv);
        tvChoice.setText("Choice " + position);

        DirectRoutBean routeBean = routes.get(position);
        if (routeBean != null) {
            StringBuilder sb = new StringBuilder();
            ArrayList<DirectLegBean> legs = routeBean.getLegs();
            if (legs != null) {
                for (DirectLegBean leg  : legs) {
                    ArrayList<DirectStepBean> steps = leg.getSteps();
                    if (steps != null) {
                        for (DirectStepBean step : steps) {
                            sb.append(step.getHtml_instructions() + "<br>");
                        }
                    }

                }
            }

            tvPath.setText(Html.fromHtml(sb.toString()));
        }


        return convertView;
    }
}
