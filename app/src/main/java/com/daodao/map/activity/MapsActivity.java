package com.daodao.map.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daodao.map.helper.PopViewHelper;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.SupportStreetViewPanoramaFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.StreetViewPanoramaLocation;
import com.daodao.map.R;
import com.daodao.map.view.CustomProgressDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;



public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener,
        StreetViewPanorama.OnStreetViewPanoramaChangeListener, GoogleMap.OnMarkerDragListener {

    private static final String[] LOCATION_PERMS ={
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private static final int INITIAL_REQUEST = 1337;
//    private static final int CAMERA_REQUEST=INITIAL_REQUEST+1;
//    private static final int CONTACTS_REQUEST=INITIAL_REQUEST+2;
    private static final int LOCATION_REQUEST = INITIAL_REQUEST + 3;
    private static final int MY_LOCATION_REQUEST = INITIAL_REQUEST + 4;

    private AdView adView;

    private GoogleMap mMap;
    private ImageView ivRoute;
    private EditText edtSearch;
    private ImageView ivSearch, ivLocateMyPosition, ivMapTypeChange, ivTrafficState, ivMenu;

    /*
    * 地图类型  1：普通   2：街景
    */
    private int mapType = 1;
    private SupportMapFragment mapFragment;

    private SupportStreetViewPanoramaFragment streetViewPanoramaFragment;

    private StreetViewPanorama panoramaStreetView;

    // George St, Sydney
    private static final LatLng SYDNEY = new LatLng(-33.87365, 151.20689);
    private static final String MARKER_POSITION_KEY = "MarkerPosition";
    private Marker mMarker;
    private LatLng markerPosition;

    private CustomProgressDialog progressDialog;

    private LinearLayout lltMenu;
    private LinearLayout lltMenuSatellite, lltMenuFlatten, lltMenuEyeView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        if (savedInstanceState == null) {
            markerPosition = SYDNEY;
        } else {
            markerPosition = savedInstanceState.getParcelable(MARKER_POSITION_KEY);
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        streetViewPanoramaFragment =
                (SupportStreetViewPanoramaFragment)
                        getSupportFragmentManager().findFragmentById(R.id.streetview_panorama);
        streetViewPanoramaFragment.getStreetViewPanoramaAsync(
                new OnStreetViewPanoramaReadyCallback() {
                    @Override
                    public void onStreetViewPanoramaReady(StreetViewPanorama panorama) {
                        // Only set the panorama to SYDNEY on startup (when no panoramas have been
                        // loaded which is when the savedInstanceState is null).
//                        if (savedInstanceState == null) {
//                            panorama.setPosition(SYDNEY);
//                        }
                        panoramaStreetView = panorama;

                        panoramaStreetView.setOnStreetViewPanoramaChangeListener(MapsActivity.this);
                    }
                });

        mapFragment.setUserVisibleHint(false);
        streetViewPanoramaFragment.setUserVisibleHint(true);


        initView();

    }

    private void initView() {
        adView = (AdView) findViewById(R.id.act_map_ad_view);
        ivRoute = (ImageView) findViewById(R.id.iv_route);
        edtSearch = (EditText) findViewById(R.id.act_map_search_edt);
        ivSearch = (ImageView) findViewById(R.id.act_map_search_iv);
        ivLocateMyPosition = (ImageView) findViewById(R.id.act_map_locate_my_position);
        ivMapTypeChange = (ImageView) findViewById(R.id.act_map_type_change);
        ivTrafficState = (ImageView) findViewById(R.id.act_map_traffic);
        ivMenu = (ImageView) findViewById(R.id.act_map_menu_iv);

        lltMenu = (LinearLayout) findViewById(R.id.act_map_menu);
        lltMenuSatellite = (LinearLayout) findViewById(R.id.act_map_menu_satellite);
        lltMenuFlatten = (LinearLayout) findViewById(R.id.act_map_menu_flatten);
        lltMenuEyeView = (LinearLayout) findViewById(R.id.act_map_menu_eye_view);

        edtSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    doSearch();
                    return true;
                }
                return false;
            }
        });

        edtSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList addresses = getHistorySearchAddress();
                if (addresses != null && addresses.size() > 0) {
                    PopViewHelper.showHistoryAddressPopWindow(MapsActivity.this, edtSearch, addresses, new PopViewHelper.OnHistoryAddressSelectListener() {

                        @Override
                        public void onHistoryAddressSelect(String address) {
                            edtSearch.setText(address);
                            doSearch();
                        }

                        @Override
                        public void onHistoryAddressDelete(String address) {
                            deleteHistorySearchAddress(address);
                        }
                    });
                }
            }
        });

        ivRoute.setOnClickListener(this);
        ivSearch.setOnClickListener(this);
        ivLocateMyPosition.setOnClickListener(this);
        ivMapTypeChange.setOnClickListener(this);
        ivTrafficState.setOnClickListener(this);
        ivMenu.setOnClickListener(this);

        lltMenuSatellite.setOnClickListener(this);
        lltMenuFlatten.setOnClickListener(this);
        lltMenuEyeView.setOnClickListener(this);

        AdRequest adRequest = new AdRequest.Builder().addTestDevice(getString(R.string.banner_ad_map_unit_id)).build();
//        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        googleMap.moveCamera(CameraUpdateFactory.zoomTo(14));

        mMarker = mMap.addMarker(new MarkerOptions()
                .position(markerPosition)
//                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.pegman))
                .draggable(true));


        mMap.setOnMarkerDragListener(MapsActivity.this);

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                closeMenu();
            }
        });


//        mMap.getUiSettings().setMapToolbarEnabled(true);
//        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        setMyLocationEnabled();
        locateCurPosition();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mMarker != null) {
            outState.putParcelable(MARKER_POSITION_KEY, mMarker.getPosition());
        }
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_route:
                Intent intent = new Intent(MapsActivity.this, RouteActivity.class);
                startActivity(intent);
                break;
            case R.id.act_map_search_iv:
                // TODO search
                doSearch();
                break;
            case R.id.act_map_locate_my_position:
                locateCurPosition();
                closeMenu();
                break;
            case R.id.act_map_type_change:
                changeMapType();
                break;
            case R.id.act_map_traffic:
                if (mMap != null) {
                    boolean isTrafficEnabled = mMap.isTrafficEnabled();
                    if (isTrafficEnabled) {
                        ivTrafficState.setImageResource(R.mipmap.btn_transport);
                    } else {
                        ivTrafficState.setImageResource(R.mipmap.btn_transport_hl);
                    }
                    mMap.setTrafficEnabled(!isTrafficEnabled);
                }
                break;
            case R.id.act_map_menu_iv:
                openMenu();
                break;
            case R.id.act_map_menu_satellite:
                if (mMap != null) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                    lltMenuSatellite.setBackgroundResource(R.drawable.shape_map_menu_item);
                    lltMenuFlatten.setBackgroundColor(Color.parseColor("#00000000"));
                    lltMenuEyeView.setBackgroundColor(Color.parseColor("#00000000"));
                }
                break;
            case R.id.act_map_menu_flatten:
                if (mMap != null) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    lltMenuSatellite.setBackgroundColor(Color.parseColor("#00000000"));
                    lltMenuFlatten.setBackgroundResource(R.drawable.shape_map_menu_item);
                    lltMenuEyeView.setBackgroundColor(Color.parseColor("#00000000"));
                }
                break;
            case R.id.act_map_menu_eye_view:
                if (mMap != null) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                    lltMenuSatellite.setBackgroundColor(Color.parseColor("#00000000"));
                    lltMenuFlatten.setBackgroundColor(Color.parseColor("#00000000"));
                    lltMenuEyeView.setBackgroundResource(R.drawable.shape_map_menu_item);
                }
                break;
        }
    }

    private void openMenu() {
        lltMenu.setVisibility(View.VISIBLE);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.menu_right_translate_in);
        LayoutAnimationController inController = new LayoutAnimationController(animation);
        lltMenu.setLayoutAnimation(inController);
        lltMenu.startAnimation(animation);
        lltMenu.startLayoutAnimation();
    }

    private void closeMenu() {
        if (lltMenu.getVisibility() == View.VISIBLE) {
            lltMenu.setVisibility(View.GONE);
            Animation animation = AnimationUtils.loadAnimation(this, R.anim.menu_right_translate_out);
            LayoutAnimationController outController = new LayoutAnimationController(animation);
            lltMenu.setLayoutAnimation(outController);
            lltMenu.startAnimation(animation);
            lltMenu.startLayoutAnimation();
        }
    }

    private void changeMapType() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if (mapType == 1) {  // 普通
            // 切换为街景
            mapType = 2;
            if (mapFragment != null) {
                fragmentTransaction.hide(mapFragment);
            }
            if (streetViewPanoramaFragment != null) {
                fragmentTransaction.show(streetViewPanoramaFragment);
                if (mMarker != null) {
                    if (panoramaStreetView != null) {
                        panoramaStreetView.setPosition(mMarker.getPosition());
                    }
                }
            }
            ivMapTypeChange.setImageResource(R.mipmap.btn_camera_hl);
        } else {   // 街景
            // 切换为显示普通地图
            mapType = 1;
            if (mapFragment != null) {
                fragmentTransaction.show(mapFragment);
            }
            if (streetViewPanoramaFragment != null) {
                fragmentTransaction.hide(streetViewPanoramaFragment);
            }
            ivMapTypeChange.setImageResource(R.mipmap.btn_camera);
        }
        fragmentTransaction.commit();
    }

    private void setMyLocationEnabled() {
        if (canAccessLocation()) {
            if (mMap != null) {
                try {
                    mMap.setMyLocationEnabled(true);
                    mMap.getUiSettings().setCompassEnabled(false);

//                    mMap.getUiSettings().setMapToolbarEnabled(true);
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
        new AlertDialog.Builder(MapsActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case LOCATION_REQUEST:
                if (canAccessLocation()) {
                    locateCurPosition();
                }
                break;
            case MY_LOCATION_REQUEST:
                setMyLocationEnabled();
                break;

        }

    }

    private void doSearch() {
        if (!isNetworkAvailable(this)) {
            Toast.makeText(this, "Please check your networks", Toast.LENGTH_SHORT).show();
            return;
        }
        String searchStr = edtSearch.getText().toString();
        if (TextUtils.isEmpty(searchStr)) {
            Toast.makeText(this, "no search content", Toast.LENGTH_SHORT).show();
            return;
        }

        saveHistorySearchAddress(searchStr);

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
        GeocoderTask geocoderTask = new GeocoderTask();
        geocoderTask.execute(searchStr);
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

    @Override
    public void onStreetViewPanoramaChange(StreetViewPanoramaLocation streetViewPanoramaLocation) {
        if (streetViewPanoramaLocation != null && mMarker != null) {
            mMarker.setPosition(streetViewPanoramaLocation.position);
        }
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {

        panoramaStreetView.setPosition(marker.getPosition(), 150);
    }

    private class GeocoderTask extends AsyncTask<String, Void, LatLng> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (progressDialog == null) {
                progressDialog = new CustomProgressDialog(MapsActivity.this);
            }
            if (!progressDialog.isShowing()) {
                progressDialog.show();
            }

        }

        @Override
        protected LatLng doInBackground(String... locationName) {
            // Creating an instance of Geocoder class
//            Log.d("--->", "background");
            Geocoder geocoder = new Geocoder(getBaseContext(), Locale.ENGLISH);
            List<Address> addresses = null;
            String loc = locationName[0];
            LatLng position = null;

            try {
                // Getting a maximum of 3 Address that matches the input text
                addresses = geocoder.getFromLocationName(locationName[0], 3);
//                Log.d("bump", "background try ");
                if (addresses != null && !addresses.isEmpty()) {
                    Address first_address = addresses.get(0);
                    position = new LatLng(first_address.getLatitude(),
                            first_address.getLongitude());
                    return position;

                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            if (position == null) {

                try {
                    URL url = new URL("http://maps.google.com/maps/api/geocode/json?address=" +
                             loc +"&sensor=true");
                    URLConnection urlConnection = url.openConnection();
                    urlConnection.setConnectTimeout(30000);
                    urlConnection.setReadTimeout(30000);

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
//                    webResult = webResult.replaceAll(" ", "");
                     Log.d("--->", webResult);
                    JSONObject jsonObject = new JSONObject(webResult);
                    if (jsonObject.getString("status").equals("OK")) {
                        jsonObject = jsonObject.getJSONArray("results")
                                .getJSONObject(0);
                        jsonObject = jsonObject.getJSONObject("geometry");
                        jsonObject = jsonObject.getJSONObject("location");
                        String lat = jsonObject.getString("lat");
                        String lng = jsonObject.getString("lng");
                        position = new LatLng(Double.valueOf(lat),
                                Double.valueOf(lng));
                    }

                } catch (JSONException e) {
//                    Log.e("--->", e.getMessage(), e);
                }

                } catch (MalformedURLException e) {

                } catch (IOException e) {

                } finally {

                }

            }
            return position;
        }

        @Override
        protected void onPostExecute(LatLng result) {
//            Log.d("bump", "post");
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            if (result != null) {

                if (mMarker != null) {
                    mMarker.setPosition(result);
                }

                if (mMap != null) {
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(result);
                    markerOptions.draggable(true);
                    if (mMarker == null) {
                        mMarker = mMap.addMarker(markerOptions);
                    }
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(result));
                }

                if (panoramaStreetView != null) {
                    panoramaStreetView.setPosition(result);
//                    StreetViewPanoramaCamera StreetViewPanoramaCamera = new StreetViewPanoramaCamera(15, 1, 360);
//                    panoramaStreetView.animateTo(StreetViewPanoramaCamera, 1000);
                }
            } else {
                Toast.makeText(MapsActivity.this, "Sorry, no result!", Toast.LENGTH_SHORT).show();
            }

        }



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

    public void saveHistorySearchAddress(String address) {

        if (TextUtils.isEmpty(address)) {
            return;
        }
        SharedPreferences sp = getSharedPreferences("HistorySearchAddress", MODE_PRIVATE);
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

    public void deleteHistorySearchAddress(String address) {
        if (TextUtils.isEmpty(address)) {
            return;
        }

        SharedPreferences sp = getSharedPreferences("HistorySearchAddress", MODE_PRIVATE);
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

    private ArrayList<String> getHistorySearchAddress() {
        SharedPreferences sp = getSharedPreferences("HistorySearchAddress", MODE_PRIVATE);
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