package com.daodao.map.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.daodao.map.adapter.RouteSearchResultAdapter;
import com.daodao.map.helper.PopViewHelper;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Random;


import com.daodao.map.R;
import com.daodao.map.bean.DirectLegBean;
import com.daodao.map.bean.DirectPolylineBean;
import com.daodao.map.bean.DirectResultBean;
import com.daodao.map.bean.DirectRoutBean;
import com.daodao.map.bean.DirectStepBean;
import com.daodao.map.bean.LatLngBean;
import com.daodao.map.view.CustomProgressDialog;

public class RouteActivity extends FragmentActivity implements View.OnClickListener, OnMapReadyCallback {

    private static final String Google_Direction_Base_Url = "https://maps.googleapis.com/maps/api/directions/json?";

    private static final String[] LOCATION_PERMS ={
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private String [] colors = {"#FF83FA", "#FF3E96", "#B03060", "#B8860B", "#142b70"};

    private static final int INITIAL_REQUEST = 1337;
    //    private static final int CAMERA_REQUEST=INITIAL_REQUEST+1;
//    private static final int CONTACTS_REQUEST=INITIAL_REQUEST+2;
    private static final int LOCATION_REQUEST = INITIAL_REQUEST + 3;
    private static final int MY_LOCATION_REQUEST = INITIAL_REQUEST + 4;

    public static final String MODE_DRIVING = "driving";
    public static final String MODE_WALKING = "walking";
    public static final String MOD_TRANSIT = "transit";
    private String mode = MODE_DRIVING;

    private AdView adView;

    private ImageView ivBack;
    private FrameLayout fltCar, fltRun, fltBus;
    private View tabLineCar, tabLineRun, tabLineBus;
    private ImageView ivExchangeStartEndPoint;

    private EditText edtStartPoint, edtEndPoint;
    private LinearLayout lltRouteResultContainer;
    private TextView tvRouteNoResult;
    private ListView lstRouteResult;

    private GoogleMap mMap;
    private SupportMapFragment mapFragment;

    private CustomProgressDialog progressDialog;

    private RouteSearchResultAdapter adapter;
    private ArrayList<DirectRoutBean> routesResultList = new ArrayList<DirectRoutBean>();


    /**
     * driving（默认值）表示使用道路网的标准驾车路线。
     walking 请求经由步道和人行道（如有）的步行路线。
     bicycling 请求经由自行车道和首选街道（如有）的骑行路线。
     transit 请求经由公共交通线路（如有）的路线。如果您将该模式设置为 transit，
     作为可选步骤，您可以指定 departure_time 或 arrival_time。
     如果两个时间均未指定，则 departure_time 默认使用 now 值（即，出发时间默认为当前时间）。
     作为可选步骤，您还可提供 transit_mode 和/或 transit_routing_preference。
     * */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_route);
        mapFragment.getMapAsync(this);

        initView();

//        getDirection();
    }

    private void initView() {

        adView = (AdView) findViewById(R.id.act_route_ad_view);
        ivBack = (ImageView) findViewById(R.id.act_route_back);

        fltCar = (FrameLayout) findViewById(R.id.act_route_tab_car);
        fltRun = (FrameLayout) findViewById(R.id.act_route_tab_run);
        fltBus = (FrameLayout) findViewById(R.id.act_route_tab_bus);
        tabLineCar = findViewById(R.id.act_route_tab_car_bottom);
        tabLineRun = findViewById(R.id.act_route_tab_run_bottom);
        tabLineBus = findViewById(R.id.act_route_tab_bus_bottom);
        ivExchangeStartEndPoint = (ImageView) findViewById(R.id.act_route_exchange_start_end_point);

        edtStartPoint = (EditText) findViewById(R.id.act_route_start_point);
        edtEndPoint = (EditText) findViewById(R.id.act_route_end_point);

        lltRouteResultContainer = (LinearLayout) findViewById(R.id.act_route_result_container);
        tvRouteNoResult = (TextView) findViewById(R.id.act_route_no_result);
        lstRouteResult = (ListView) findViewById(R.id.act_route_result_list);
        adapter = new RouteSearchResultAdapter(RouteActivity.this, routesResultList);
        lstRouteResult.setAdapter(adapter);

        ivBack.setOnClickListener(this);
        fltCar.setOnClickListener(this);
        fltRun.setOnClickListener(this);
        fltBus.setOnClickListener(this);
        ivExchangeStartEndPoint.setOnClickListener(this);

        edtStartPoint.setOnEditorActionListener(new TextView.OnEditorActionListener(){

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                        actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_GO) {
                    getDirection();
                    return true;
                }
                return false;
            }
        });

        edtEndPoint.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    getDirection();
                    return true;
                }
                return false;
            }
        });

        edtStartPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList addresses = getHistoryRouteAddress();
                if (addresses != null && addresses.size() > 0) {
                    PopViewHelper.showHistoryAddressPopWindow(RouteActivity.this, edtStartPoint, addresses, new PopViewHelper.OnHistoryAddressSelectListener() {
                        @Override
                        public void onHistoryAddressSelect(String address) {
                            edtStartPoint.setText(address);
                            if (TextUtils.isEmpty(address)) {
                                return;
                            }
                            String endAddress = edtEndPoint.getText().toString();
                            if (TextUtils.isEmpty(endAddress)) {
                                return;
                            }
                            getDirection();
                        }

                        @Override
                        public void onHistoryAddressDelete(String address) {
                            deleteHistoryRouteAddress(address);
                        }
                    });
                }
            }
        });

        edtEndPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList addresses = getHistoryRouteAddress();
                if (addresses != null && addresses.size() > 0) {
                    PopViewHelper.showHistoryAddressPopWindow(RouteActivity.this, edtEndPoint, addresses, new PopViewHelper.OnHistoryAddressSelectListener() {

                        @Override
                        public void onHistoryAddressSelect(String address) {
                            edtEndPoint.setText(address);
                            if (TextUtils.isEmpty(address)) {
                                return;
                            }
                            String startAddress = edtStartPoint.getText().toString();
                            if (TextUtils.isEmpty(startAddress)) {
                                return;
                            }
                            getDirection();
                        }

                        @Override
                        public void onHistoryAddressDelete(String address) {
                            deleteHistoryRouteAddress(address);
                        }
                    });
                }
            }
        });

        lstRouteResult.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                lltRouteResultContainer.setVisibility(View.GONE);
                mMap.clear();
                DirectRoutBean routeBean = (DirectRoutBean) adapter.getItem(position);
                if (routeBean == null) {
                    return;
                }
                ArrayList<LatLng> legsArr = new ArrayList<LatLng>();
                ArrayList<DirectLegBean> legs = routeBean.getLegs();
                if (legs != null && legs.size() > 0) {
                    for (DirectLegBean leg : legs) {
                        if (leg != null) {
                            LatLngBean startLatLng = leg.getStart_location();
                            LatLngBean endLatLng = leg.getEnd_location();
                            if (startLatLng != null) {
                                LatLng latLngStart = new LatLng(startLatLng.getLat(), startLatLng.getLng());
//                                            Marker mMarker =
                                mMap.addMarker(new MarkerOptions()
                                        .position(latLngStart).title(leg.getStart_address()).icon(BitmapDescriptorFactory.fromResource(R.mipmap.icon_start)));
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngStart, 12));
                            }

                            if (endLatLng != null) {
                                LatLng latLngEnd = new LatLng(endLatLng.getLat(), endLatLng.getLng());
//                                            Marker mMarker =
                                mMap.addMarker(new MarkerOptions()
                                        .position(latLngEnd).title(leg.getEnd_address()).icon(BitmapDescriptorFactory.fromResource(R.mipmap.icon_final)));
                            }

                            ArrayList<DirectStepBean> steps = leg.getSteps();
                            if (steps != null && steps.size() > 0) {


                                for (DirectStepBean step : steps) {

                                    if (step != null) {
                                        DirectPolylineBean directPolylineBean = step.getPolyline();
                                        if (directPolylineBean != null) {
                                            String pointsStr = directPolylineBean.getPoints();
                                            if (pointsStr != null) {
                                                ArrayList<LatLng> legsArrTemp = (ArrayList<LatLng>) PolyUtil.decode(pointsStr);
                                                legsArr.addAll(legsArrTemp);
                                            }
                                        }
                                    }
                                }

                            }
                        }
                    }
                }

                PolylineOptions options = new PolylineOptions();
                int colorPosition = new Random().nextInt(colors.length);
                options.color(Color.parseColor(colors[colorPosition]));
                options.addAll(legsArr);
                mMap.addPolyline(options);
            }
        });

        AdRequest adRequest = new AdRequest.Builder().addTestDevice(getString(R.string.banner_ad_map_unit_id)).build();
//        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

    }


    @Override
    protected void onPause() {
        if (adView != null) {
            adView.pause();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adView != null) {
            adView.resume();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (adView != null) {
            adView.destroy();
        }
    }

    @Override
    public void onBackPressed() {
        if (lltRouteResultContainer.getVisibility() == View.VISIBLE) {
            lltRouteResultContainer.setVisibility(View.GONE);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.act_route_back:
                finish();
                break;
            case R.id.act_route_tab_car:
                if (mode.equals(MODE_DRIVING)) {
                    return;
                }
                tabLineCar.setVisibility(View.VISIBLE);
                tabLineRun.setVisibility(View.GONE);
                tabLineBus.setVisibility(View.GONE);
                mode = MODE_DRIVING;
                String startAddressCar = edtStartPoint.getText().toString().trim();
                String endAddressCar = edtEndPoint.getText().toString().trim();
                if (!TextUtils.isEmpty(startAddressCar) && !TextUtils.isEmpty(endAddressCar)) {
                    getDirection();
                }
                break;
            case R.id.act_route_tab_run:
                if (mode.equals(MODE_WALKING)) {
                    break;
                }
                tabLineCar.setVisibility(View.GONE);
                tabLineRun.setVisibility(View.VISIBLE);
                tabLineBus.setVisibility(View.GONE);
                mode = MODE_WALKING;
                String startAddressWalking = edtStartPoint.getText().toString().trim();
                String endAddressWalking = edtEndPoint.getText().toString().trim();
                if (!TextUtils.isEmpty(startAddressWalking) && !TextUtils.isEmpty(endAddressWalking)) {
                    getDirection();
                }
                break;
            case R.id.act_route_tab_bus:
                if (mode.equals(MOD_TRANSIT)) {
                    return;
                }
                tabLineCar.setVisibility(View.GONE);
                tabLineRun.setVisibility(View.GONE);
                tabLineBus.setVisibility(View.VISIBLE);
                mode = MOD_TRANSIT;
                String startAddressBus = edtStartPoint.getText().toString().trim();
                String endAddressBus = edtEndPoint.getText().toString().trim();
                if (!TextUtils.isEmpty(startAddressBus) && !TextUtils.isEmpty(endAddressBus)) {
                    getDirection();
                }
                break;
            case R.id.act_route_exchange_start_end_point:
                String startAddress = edtStartPoint.getText().toString().trim();
                String endAddress = edtEndPoint.getText().toString().trim();
                edtStartPoint.setText(endAddress);
                edtEndPoint.setText(startAddress);
                if (!TextUtils.isEmpty(startAddress) && !TextUtils.isEmpty(endAddress)) {
                    getDirection();
                }
                break;
        }
    }

    private void getDirection() {

        if (!isNetworkAvailable(this)) {
            Toast.makeText(this, "Please check your networks", Toast.LENGTH_SHORT).show();
            return;
        }

        String startAddress = edtStartPoint.getText().toString().trim();
        String endAddress = edtEndPoint.getText().toString().trim();
        if (TextUtils.isEmpty(startAddress)) {
            Toast.makeText(RouteActivity.this, "no start point", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(endAddress)) {
            Toast.makeText(RouteActivity.this, "no end point", Toast.LENGTH_SHORT).show();
            return;
        }

        saveHistoryRouteAddress(startAddress);
        saveHistoryRouteAddress(endAddress);

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);

        DirectionTask directionTask = new DirectionTask();
        directionTask.execute(startAddress, endAddress);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        setMyLocationEnabled();

        mMap.moveCamera(CameraUpdateFactory.zoomTo(14));
        locateCurPosition();
    }

    private void locateCurPosition() {
        if (canAccessLocation()) {
            LocationManager locMgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);//获得
            try {
                Location location = locMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location == null) {
                    location = locMgr.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }
                if (location != null) {
                    LatLng curLatLng = new LatLng(location.getLatitude(), location.getLongitude());
//                    mMap.addMarker(new MarkerOptions().position(curLatLng).title(location.toString()));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(curLatLng));
//                    Log.i("--->", "Latitude == " + location.getLatitude());
//                    Log.i("--->", "Longitude == " + location.getLongitude());
                } else {
//                    Log.i("--->", "定位失败");
                }
            } catch (SecurityException e) {
//                Log.i("--->", e.getMessage());
            }

        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(LOCATION_PERMS, LOCATION_REQUEST);
            }
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
//            case LOCATION_REQUEST:
//                if (canAccessLocation()) {
//                    locateCurPosition();
//                }
//                break;
            case MY_LOCATION_REQUEST:
                setMyLocationEnabled();
                break;

        }

    }

    private class DirectionTask extends AsyncTask<String, Void, DirectResultBean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (progressDialog == null) {
                progressDialog = new CustomProgressDialog(RouteActivity.this);
            }
            if (!progressDialog.isShowing()) {
                progressDialog.show();
            }
        }

        @Override
        protected DirectResultBean doInBackground(String... params) {
            // driving  walking  transit
            try {
                if (params == null || params.length < 2) {
                    return null;
                }
                String startAddress = params[0];
                String endAddress = params[1];
                String directionUrlStr = Google_Direction_Base_Url + "origin=" + URLEncoder.encode(startAddress, "utf-8") + "&" +
                        "destination=" + URLEncoder.encode(endAddress, "utf-8") + "&" + "key=AIzaSyC93NEIR6N-VuNFy8fqeMG8MPMBSYRhU2w" +
                        "&mode=" + mode + "&alternatives=true";
                URL url = new URL(directionUrlStr);
                URLConnection urlConnection = url.openConnection();

                HttpURLConnection httpUrlConnection = (HttpURLConnection) urlConnection;

                InputStream inputStream = httpUrlConnection.getInputStream();

                // 取得输入流，并使用Reader读取
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        inputStream));
                StringBuilder sb = new StringBuilder();
                String lines;
                while ((lines = reader.readLine()) != null) {
                    sb.append(lines);
                }
                inputStream.close();
                try {
                    String webResult = sb.toString();

                    webResult = webResult.replaceAll(" ", "");
//                    Log.d("--->", webResult);
                    JSONObject jsonObject = new JSONObject(webResult);
                    if (jsonObject.getString("status").equals("OK")) {
                        DirectResultBean directResultBean = JSON.parseObject(webResult, DirectResultBean.class);
                        return directResultBean;
                    }
                } catch (JSONException e) {

                }
            } catch (UnsupportedEncodingException e) {

            } catch (MalformedURLException e) {

            } catch (IOException e) {

            }
            return null;
        }


        @Override
        protected void onPostExecute(DirectResultBean directResultBean) {
            super.onPostExecute(directResultBean);

            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            mMap.clear();
            if (directResultBean != null) {
                if (directResultBean.getStatus().equalsIgnoreCase("OK")) {
                    ArrayList<DirectRoutBean> routes = directResultBean.getRoutes();
                    if (routes != null && routes.size() > 0) {
                        for (DirectRoutBean route : routes) {

                            ArrayList<LatLng> legsArr = new ArrayList<LatLng>();
                            ArrayList<DirectLegBean> legs = route.getLegs();
                            if (legs != null && legs.size() > 0) {
                                for (DirectLegBean leg : legs) {
                                    if (leg != null) {
                                        LatLngBean startLatLng = leg.getStart_location();
                                        LatLngBean endLatLng = leg.getEnd_location();
                                        if (startLatLng != null) {
                                            LatLng latLngStart = new LatLng(startLatLng.getLat(), startLatLng.getLng());
//                                            Marker mMarker =
                                                    mMap.addMarker(new MarkerOptions()
                                                    .position(latLngStart).title(leg.getStart_address()).icon(BitmapDescriptorFactory.fromResource(R.mipmap.icon_start)));
                                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngStart, 12));
                                        }

                                        if (endLatLng != null) {
                                            LatLng latLngEnd = new LatLng(endLatLng.getLat(), endLatLng.getLng());
//                                            Marker mMarker =
                                                    mMap.addMarker(new MarkerOptions()
                                                    .position(latLngEnd).title(leg.getEnd_address()).icon(BitmapDescriptorFactory.fromResource(R.mipmap.icon_final)));
                                        }

                                        ArrayList<DirectStepBean> steps = leg.getSteps();
                                        if (steps != null && steps.size() > 0) {


                                            for (DirectStepBean step : steps) {

                                                if (step != null) {
                                                    DirectPolylineBean directPolylineBean = step.getPolyline();
                                                    if (directPolylineBean != null) {
                                                        String pointsStr = directPolylineBean.getPoints();
                                                        if (pointsStr != null) {
                                                            ArrayList<LatLng> legsArrTemp = (ArrayList<LatLng>) PolyUtil.decode(pointsStr);
                                                            legsArr.addAll(legsArrTemp);
                                                        }
                                                    }
                                                }
                                            }

                                        }
                                    }
                                }
                            }

                            PolylineOptions options = new PolylineOptions();
                            int colorPosition = new Random().nextInt(colors.length);
                            options.color(Color.parseColor(colors[colorPosition]));
                            options.addAll(legsArr);
                            mMap.addPolyline(options);

                        }
                    }

                    if (routesResultList == null) {
                        routesResultList = new ArrayList<>();
                    }
                    lltRouteResultContainer.setVisibility(View.VISIBLE);
                    tvRouteNoResult.setVisibility(View.GONE);
                    lstRouteResult.setVisibility(View.VISIBLE);

                    routesResultList.clear();
                    routesResultList.addAll(routes);
                    adapter.notifyDataSetChanged();
                }

            } else {
                tvRouteNoResult.setVisibility(View.VISIBLE);
                lstRouteResult.setVisibility(View.GONE);
//                Toast.makeText(RouteActivity.this, "sorry, has no result", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setMyLocationEnabled() {
        if (canAccessLocation()) {
            if (mMap != null) {
                try {
                    mMap.setMyLocationEnabled(true);
                    mMap.getUiSettings().setCompassEnabled(false);
                } catch (SecurityException e) {

                }
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                if (!shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    showMessageOKCancel("You need to allow access to Location",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        requestPermissions(LOCATION_PERMS, LOCATION_REQUEST);
                                    }
                                }
                            }
                    );
                } else {
                    requestPermissions(LOCATION_PERMS, LOCATION_REQUEST);
                }
            }
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(RouteActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private boolean canAccessLocation() {
        return hasPermission(Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private boolean hasPermission(String perm) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return PackageManager.PERMISSION_GRANTED == checkSelfPermission(perm);
        }
        return true;
    }

    public boolean isNetworkAvailable(Activity activity) {
        Context context = activity.getApplicationContext();
        // 获取手机所有连接管理对象（包括对wi-fi,net等连接的管理）
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager == null)
        {
            return false;
        }
        else
        {
            // 获取NetworkInfo对象
            NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();

            if (networkInfo != null && networkInfo.length > 0)
            {
                for (int i = 0; i < networkInfo.length; i++)
                {
                    System.out.println(i + "===状态===" + networkInfo[i].getState());
                    System.out.println(i + "===类型===" + networkInfo[i].getTypeName());
                    // 判断当前网络状态是否为连接状态
                    if (networkInfo[i].getState() == NetworkInfo.State.CONNECTED)
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void saveHistoryRouteAddress(String address) {

        synchronized (this) {
            if (TextUtils.isEmpty(address)) {
                return;
            }
            SharedPreferences sp = getSharedPreferences("HistoryRouteAddress", MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();


            int addressSize = sp.getInt("Address_Size", 0);
            if (addressSize > 0) {
                ArrayList<String> addresses = new ArrayList<String>();
                for(int i = 0; i < addressSize; i++) {
                    addresses.add(sp.getString("Status_" + i, null));
                }
                if (addresses.contains(address)) {
                    return;
                } else {
                    editor.putString("Status_" + addresses.size(), address);
                    editor.putInt("Address_Size", addresses.size() + 1).commit();
                }
            } else {
                editor.putString("Status_0", address);
                editor.putInt("Address_Size", 1).commit();
            }
        }
    }

    public void deleteHistoryRouteAddress(String address) {
        if (TextUtils.isEmpty(address)) {
            return;
        }
        SharedPreferences sp = getSharedPreferences("HistoryRouteAddress", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        int addressSize = sp.getInt("Address_Size", 0);
        if (addressSize > 0) {
            ArrayList<String> addresses = new ArrayList<String>();
            for(int i = 0; i < addressSize; i++) {
                addresses.add(sp.getString("Status_" + i, null));
            }
            if (addresses.contains(address)) {
                addresses.remove(address);
                int size = addresses.size();
                for (int i = 0; i < size; i++) {
                    editor.putString("Status_" + i, addresses.get(i));
                }
                editor.putInt("Address_Size", size).commit();
            }
        }

    }

    private ArrayList<String> getHistoryRouteAddress() {
        SharedPreferences sp = getSharedPreferences("HistoryRouteAddress", MODE_PRIVATE);
        int addressSize = sp.getInt("Address_Size", 0);
        if (addressSize <= 0) {
            return null;
        } else {
            ArrayList<String> addresses = new ArrayList<String>();
            for(int i = 0; i < addressSize; i++) {
                addresses.add(sp.getString("Status_" + i, null));
            }
            return addresses;
        }
    }
}


