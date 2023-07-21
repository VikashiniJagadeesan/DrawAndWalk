package com.example.drawandwalk;

import static com.example.drawandwalk.uiSupport.Constants.FARM;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.drawandwalk.uiSupport.Constants;
import com.example.drawandwalk.uiSupport.DisableNavigation;
import com.example.drawandwalk.uiSupport.GoogleLocationService;
import com.example.drawandwalk.uiSupport.ShowDialogErrorSuccess;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;


import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Draw extends AppCompatActivity implements  OnMapReadyCallback {
    private LocationManager locationManager;
    public static Context context;

    private CameraPosition cameraPosition;
    private List<Polyline> markerpolylines;
    private Polyline lastFirst;
    private Polygon polygon;
    private Polyline polyline;
    double MAX_AREA = Constants.MAX_AREA_OF_FARM;
    private int MarkerIndex = 0;

    private RelativeLayout relativeLayout;

    private Button nextBtn, finishBtn;
    private TextView farmName, lat, lng, areaOfFarmTextView, note;
    private Toast toast;
    private EditText farmernumtxt;
    private final String surveyNo = "";
    private String isIrrigated = "No";

    private double areaofFarm = 0;

    private ArrayList<Marker> totalMarker;
    private Marker mapmarker;


    private String coordinates, coordinatesToPass, cropType = "", cropAge = "-1", farmerNumber = "", cityName = "", stateName = "";

    private ImageButton locaitonBtn, clear, getLatLng, undoimg;

    private final int maxloccount = 1;          //no of times to get location update

    private BroadcastReceiver broadcastReceiver;
    private double latitude = 0, longitude = 0, MyAccuracy = 0;
    private Marker mycurrentpos = null, lastClicked = null, Searchmycurrentpos = null;
    private GoogleMap mMap;
    private LatLng oldLatLon = null, currentlocation = null;
    private boolean HoldMapLocation = false;
    private int countlocupdate = 0;
    private boolean NextClicked = false, locationClicked = false;
    private String farmCategory = "", language = "en";
    private ProgressDialog progress;
    Button btnAbout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);
        btnAbout = (Button) findViewById(R.id.back);

        btnAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Draw.this, MainActivity.class);
                startActivity(intent);
            }
        });
        progress = new ProgressDialog(Draw.this);
        progress.setCancelable(false);
        totalMarker = new ArrayList<>();
        markerpolylines = new ArrayList<>();
        locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent != null) {
                    final String action = intent.getAction();
                    if ("LOCATION_UPDATE".equals(action)) {
                        latitude = intent.getDoubleExtra("latitude", 0);
                        longitude = intent.getDoubleExtra("longitude", 0);
                        MyAccuracy = intent.getDoubleExtra("accuracy", 0);
                        System.out.println("Draw: Lat= " + latitude + "  Lon= " + longitude);

                        if (mycurrentpos != null) {
                            mycurrentpos.remove();
                        }
                        mycurrentpos = mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(latitude, longitude))
                                .title("You are here")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.user_position_point)));

                        getCityState(latitude, longitude);
                        currentlocation = new LatLng(latitude, longitude);
                        if (!HoldMapLocation && countlocupdate < maxloccount) {
                            // if (MyAccuracy != 0 & MyAccuracy < 100) {
                            cameraPosition = new CameraPosition.Builder().target(new LatLng(latitude, longitude)).zoom(18).build();
                            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                        } else if (countlocupdate > maxloccount) {
                            locationClicked = false;
//                            StopLocationService();
                        }

                        countlocupdate++;
                    }
                }
            }
        };

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) this.getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
        DisableNavigation dv = new DisableNavigation(this.getWindow().getDecorView());
        locaitonBtn = findViewById(R.id.curr_location);
        relativeLayout = findViewById(R.id.farmDetails);
        undoimg = findViewById(R.id.undoimg);
        clear = findViewById(R.id.clear_map);
        nextBtn = findViewById(R.id.nxt_btn);
        finishBtn = findViewById(R.id.finish);
        getLatLng = findViewById(R.id.get_lat_lng);
        farmName = findViewById(R.id.farm_name);
        lat = findViewById(R.id.lat);
        lng = findViewById(R.id.lng);
        areaOfFarmTextView = findViewById(R.id.areaOfFarm1);
//        cropTypeSpinner = findViewById(R.id.crop_type);
//        crop_agetxt = findViewById(R.id.crop_agetxt);
        // cropStageSpinner = (Spinner) findViewById(R.id.crop_stage);
        note = findViewById(R.id.note);
//        switchfarm = findViewById(R.id.switchfarm);
        farmernumtxt = findViewById(R.id.farmernumtxt);
        /*relativeLayout.setVisibility(View.GONE);*/
        System.out.println("relativeLayout.setEnabled: " + relativeLayout.getVisibility());
        TextView textView = findViewById(R.id.textView6);


        locaitonBtn.setOnClickListener(v -> {
            if (NextClicked) {
                if (lat.getVisibility() == View.VISIBLE) {
                    lat.setVisibility(View.GONE);
                    lng.setVisibility(View.GONE);
                }
                HoldMapLocation = false;
                boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                if (!isGPSEnabled) {
                    buildAlertMessageNoGps();
                } else {
                    locationClicked = true;
                    countlocupdate = 0;
                    StartLocationService();
                    if (latitude != 0 && longitude != 0 && currentlocation != null) {
                        CameraPosition cameraP = new CameraPosition.Builder().target(currentlocation).zoom(18).build();
                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraP));
                    } else {
                        countlocupdate = 0;
                    }
                }
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        CameraPosition camerapos = new CameraPosition.Builder().target(new LatLng(13, 77)).zoom(15).build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(camerapos));

        undoimg.setOnClickListener(v -> {
            hideFinishButton();

            if (lastFirst != null) {
                lastFirst.remove();
            }
            if (polygon != null) {
                polygon.remove();
            }
            OneStepUndo();
            if (totalMarker.size() > 1) {
                addLastFirst(totalMarker.get(0).getPosition(), totalMarker.get(totalMarker.size() - 1).getPosition());
            }
        });

        nextBtn.setOnClickListener(v -> {
            HideKeyboard();

            System.out.println("chosenAge: " + cropAge);
            String farmNAme = farmName.getText().toString().trim();
            System.out.println("farmNAme: " + farmNAme);

            if (farmNAme.equals("")) {
                toast = Toast.makeText(Draw.this, "Give a name to your farm!", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);

                toast.show();
            } else if (farmernumtxt.getVisibility() == View.VISIBLE && farmernumtxt.getText().toString().length() != 10 && farmernumtxt.getText().toString().length() != 0) {

                toast = Toast.makeText(Draw.this, "Enter correct phone number", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();

            } else {
                NextClicked = true;
                boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                if (!isGPSEnabled && NextClicked) {
                    buildAlertMessageNoGps();
                }
//                dbHelper = new DatabaseHelper(AddFarm.this, null, null, DatabaseHelper.DATABASE_VERSION);
//                int count = dbHelper.checkAvailability("farmName", farmNAme, "userUpload");
                int count =0;
                System.out.println("count: " + count);

                if (count <= 0) {

                    if (farmernumtxt.getVisibility() == View.VISIBLE) {
                        farmerNumber = farmernumtxt.getText().toString();
                    }
                    System.out.println("CropppppAgeee: " + cropAge);

                    relativeLayout.setVisibility(View.GONE);
                    System.out.println("Next click, layout visible?: " + (relativeLayout.getVisibility() == View.GONE));
                    if (relativeLayout.getVisibility() == View.GONE) {

                        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                            @Override
                            public void onMapClick(LatLng latLng) {
                                if (NextClicked) {

                                    boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                                    if (isGPSEnabled) {
                                        HoldMapLocation = true;
                                        if (totalMarker.isEmpty()) {
                                            mapmarker = mMap.addMarker(new MarkerOptions()
                                                    .position(latLng)
                                                    .draggable(true)
                                                    .title("Start"));
                                        } else {
                                            mapmarker = mMap.addMarker(new MarkerOptions()
                                                    .position(latLng)
                                                    .draggable(true));
                                        }
                                        totalMarker.add(mapmarker);
                                        note.setVisibility(View.VISIBLE);

                                        //adding lines on map
                                        if (oldLatLon != null & totalMarker.size() != 1) {
                                            polyline = mMap.addPolyline(new PolylineOptions().add(oldLatLon, latLng)
                                                    .width(10)
                                                    .color(Color.GREEN));
                                            markerpolylines.add(polyline);
                                            oldLatLon = latLng;
                                            addLastFirst(latLng, totalMarker.get(0).getPosition());
                                        } else {
                                            oldLatLon = latLng;
                                        }

                                        //to get area of marked farm
                                        GetAreaOfFarm();
                                        if (totalMarker.size() >= 3) {
                                            finishBtn.setVisibility(View.VISIBLE);
                                        } else {
                                            finishBtn.setVisibility(View.GONE);
                                        }
                                    } else {
                                        buildAlertMessageNoGps();
                                    }
                                }
                            }
                        });

                        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                            @Override
                            public boolean onMarkerClick(Marker marker) {
                                if (NextClicked) {
                                    if (lastClicked != null && lastClicked.equals(marker)) {
                                        lastClicked = null;
                                        marker.hideInfoWindow();
                                        return true;
                                    } else {
                                        lastClicked = marker;
                                        return false;
                                    }
                                } else {
                                    return false;
                                }
                            }
                        });

                        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                            @Override
                            public void onMarkerDragStart(@NonNull Marker marker) {
                                for (int i = 0; i < totalMarker.size(); i++) {
                                    if (marker.equals(totalMarker.get(i))) {
                                        MarkerIndex = totalMarker.indexOf(totalMarker.get(i));
                                        System.out.println("index: " + MarkerIndex);
                                    }
                                }
                            }

                            @Override
                            public void onMarkerDrag(@NonNull Marker marker) {
                                try {
                                    removePolyline(MarkerIndex);
                                    if (MarkerIndex < totalMarker.size() - 1) {
                                        addPolyline(marker.getPosition(), totalMarker.get(MarkerIndex + 1).getPosition(), MarkerIndex);
                                    }
                                    removePolyline(MarkerIndex - 1);
                                    if (MarkerIndex != 0) {
                                        addPolyline(totalMarker.get(MarkerIndex - 1).getPosition(), marker.getPosition(), MarkerIndex - 1);
                                    } else {
                                        addLastFirst(totalMarker.get(0).getPosition(), totalMarker.get(totalMarker.size() - 1).getPosition());
                                    }
                                    if (MarkerIndex == totalMarker.size() - 1) {
                                        addLastFirst(totalMarker.get(0).getPosition(), totalMarker.get(totalMarker.size() - 1).getPosition());
                                    }
                                    GetAreaOfFarm();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onMarkerDragEnd(@NonNull Marker marker) {
                                try {
                                    totalMarker.set(MarkerIndex, marker);
                                    marker.setTitle(marker.getPosition().toString());
                                    marker.showInfoWindow();
                                    oldLatLon = totalMarker.get(totalMarker.size() - 1).getPosition();
                                    marker.setAlpha(1.0f);

                                    if (totalMarker.size() >= 3) {
                                        finishBtn.setVisibility(View.VISIBLE);
                                    } else {
                                        finishBtn.setVisibility(View.GONE);
                                    }
                                    GetAreaOfFarm();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                            @Nullable
                            @Override
                            public View getInfoWindow(@NonNull Marker marker) {
                                View view = null;
                                if (!marker.equals(mycurrentpos)) {
                                    view = View.inflate(Draw.this, R.layout.item_delete, null);
                                }
                                return view;
                            }

                            @Nullable
                            @Override
                            public View getInfoContents(@NonNull Marker marker) {
                                return null;
                            }
                        });

                        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                            @Override
                            public void onInfoWindowClick(@NonNull Marker marker) {
                                try {
                                    if (totalMarker.size() > 1) {
                                        MarkerIndex = totalMarker.indexOf(marker);
                                        if (MarkerIndex == totalMarker.size() - 1) {
                                            if (lastFirst != null) {
                                                lastFirst.remove();
                                            }
                                            removePolyline(MarkerIndex - 1);
                                            totalMarker.remove(totalMarker.size() - 1);
                                            marker.remove();
                                            addLastFirst(totalMarker.get(0).getPosition(), totalMarker.get(totalMarker.size() - 1).getPosition());
                                            oldLatLon = totalMarker.get(totalMarker.size() - 1).getPosition();
                                        } else if (MarkerIndex == 0) {
                                            if (lastFirst != null) {
                                                lastFirst.remove();
                                            }
                                            totalMarker.remove(0);
                                            marker.remove();
                                            addLastFirst(totalMarker.get(0).getPosition(), totalMarker.get(totalMarker.size() - 1).getPosition());
                                            removePolyline(0);

                                        } else {
                                            removePolyline(MarkerIndex);
                                            removePolyline(MarkerIndex - 1);
                                            totalMarker.remove(marker.getPosition());
                                            addPolyline(totalMarker.get(MarkerIndex - 1).getPosition(), totalMarker.get(MarkerIndex + 1).getPosition(), MarkerIndex - 1);
                                            totalMarker.remove(marker);
                                            marker.remove();
                                        }
                                    } else {
                                        ClearMap();
                                    }
                                    if (polygon != null) {
                                        polygon.remove();
                                    }
                                    addPloyFillColor();
                                    GetAreaOfFarm();
                                    if (totalMarker.size() >= 3) {
                                        finishBtn.setVisibility(View.VISIBLE);
                                    } else {
                                        finishBtn.setVisibility(View.GONE);
                                    }

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    } else
                        System.out.println("Layout visible");
                } else {
                    toast = Toast.makeText(Draw.this, "Farm name exist, give a unique name!", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            }
        });

        clear.setOnClickListener(v -> ClearMap());

        getLatLng.setOnClickListener(view -> {

            if (NextClicked) {
                System.out.println("lat.getVisibility(): " + (lat.getVisibility() == View.GONE));

                if (lat.getVisibility() == View.GONE) {
                    lat.setVisibility(View.VISIBLE);
                    lng.setVisibility(View.VISIBLE);
                    getLatLng.setImageResource(R.drawable.direct_selection1);
                } else {
                    if (!(lat.getText().toString().equals("") || lng.getText().toString().equals(""))) {
                        cameraPosition = new CameraPosition.Builder().target(new LatLng(Double.parseDouble(lat.getText().toString()), Double.parseDouble(lng.getText().toString()))).zoom(15).build();
                        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                        HoldMapLocation = true;
                        if (Searchmycurrentpos != null) {
                            Searchmycurrentpos.remove();
                        }
                        Searchmycurrentpos = mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(Double.parseDouble(lat.getText().toString()), Double.parseDouble(lng.getText().toString())))
                                .title("You are here")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.currentpos)));
                    }
                    lat.setVisibility(View.GONE);
                    lng.setVisibility(View.GONE);
                    try {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                        View view1 = getCurrentFocus();
                        if (view1 == null) {
                            view1 = new View(getApplicationContext());
                        }
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    } catch (Exception ignore) {
                    }
                }
            }
        });

        finishBtn.setOnClickListener(v -> SaveFarmCood());

    }


    protected String postRequest(final String coord, final String storeCoord) throws IOException {
        if (cityName.equals("") || stateName.equals("")) {
            getCityState(totalMarker.get(0).getPosition().latitude, totalMarker.get(0).getPosition().longitude);
        }
        String url = "";
        final String[] res = new String[1];
        //  final String url = "https://micro.satyukt.com/postjson2?key=" + SessionManagement.getApiKey(AddFarm.this) + "&name=" + farmName.getText() + "&coordinates=[" + storeCoord + "]&croptype=" + cropType.replace(" ", "_") + "&cropage=" + cropAge+"&phone_num="+farmerNumber;
//        if (!SessionManagement.getUserName(this).equals(getString(R.string.admintestnum))) {
//            url = getString(R.string.baseUrl) + "postjson2?key=" + SessionManagement.getApiKey(AddFarm.this) + "&name=" + farmName.getText() + "&coordinates=[" + storeCoord + "]&croptype=" + cropType.replace(" ", "_") + "&cropage=" + cropAge + "&phone_num=" + farmerNumber + "&District=" + cityName + "&State=" + stateName + "&category=" + farmCategory + "&language=" + language;
//        } else {
//            url = getString(R.string.baseUrl) + "postjson2?key=" + SessionManagement.getApiKey(AddFarm.this) + "&name=" + farmName.getText() + "&coordinates=[" + storeCoord + "]&croptype=" + cropType.replace(" ", "_") + "&cropage=" + cropAge + "&phone_num=" + surveyNo + "_" + isIrrigated + "&District=" + cityName + "&State=" + stateName + "&category=" + farmCategory + "&language=" + language;
//        }


        url =  "https://micro.satyukt.com/postjson2?key=LPBKIPdyLT3ps0gg2221PTSjRpMC6DRzdJ56HcDjfuY=&name=" + farmName.getText() + "&coordinates=[" + storeCoord + "]&croptype=" + cropType.replace(" ", "_") + "&cropage=" + cropAge + "&phone_num=" + surveyNo + "_" + isIrrigated + "&District=" + cityName + "&State=" + stateName + "&category=" + farmCategory + "&language=" + language;

        System.out.println("url: " + url);
        final JSONObject[] jsonArray = new JSONObject[1];
        RequestQueue queue = Volley.newRequestQueue(Draw.this);
        String finalUrl = url;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.contains("Area limt Exceeded")) {
                            ShowDialogErrorSuccess.ShowDialog(Draw.this, "Area limit Exceeded.", "Please contact Admin.", false);
                        } else {
                            try {
                                System.out.println("In postResponse: " + response);
                                jsonArray[0] = new JSONObject(response);
                                String farmId = jsonArray[0].getString("ID");
                                res[0] = "1";
                                //addFarmToDb(MainActivity.apiKey, farmId, getFarmName, center, storeCoord);
                                System.out.println("StoreCoord: " + storeCoord + "\nCoord: " + coord);
//                                addFarmToDb(SessionManagement.getApiKey(Draw.this), farmId, farmName.getText().toString(), "", coord, finalUrl, res[0]);

                                if (progress != null && progress.isShowing()) {
                                    progress.dismiss();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                System.out.println("Didnt work");
                                res[0] = "0";
//                                int defFarmId = SessionManagement.getDefaultFarmId(getApplicationContext());
//                                SessionManagement.setDefaultFarmId(++defFarmId, getApplicationContext());

                                String dummyCenter = "[" + totalMarker.get(0).getPosition().latitude + ",";
                                dummyCenter += totalMarker.get(0).getPosition().longitude + "]";
                                System.out.println("dummyCenter: " + dummyCenter + "\nstoreCoord: " + storeCoord);

//                                addFarmToDb(SessionManagement.getApiKey(AddFarm.this), String.valueOf(++defFarmId), farmName.getText().toString(), dummyCenter, coord, finalUrl, res[0]);
                                if (progress != null && progress.isShowing()) {
                                    progress.dismiss();
                                }
                            }
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("Didnt work");
                res[0] = "0";
//                int defFarmId = SessionManagement.getDefaultFarmId(getApplicationContext());
//                SessionManagement.setDefaultFarmId(++defFarmId, getApplicationContext());
//
//                String dummyCenter = "[" + totalMarker.get(0).getPosition().latitude + ",";
//                dummyCenter += totalMarker.get(0).getPosition().longitude + "]";
//                System.out.println("dummyCenter: " + dummyCenter + "\nstoreCoord: " + storeCoord);
//
//                addFarmToDb(SessionManagement.getApiKey(AddFarm.this), String.valueOf(++defFarmId), farmName.getText().toString(), dummyCenter, coord, finalUrl, res[0]);
//                if (progress != null && progress.isShowing()) {
//                    progress.dismiss();
//                }
            }
        });
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                60000,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(stringRequest);
        try {
//            NewhomeFragment.refreshFarms = true;
        } catch (Exception ignore) {
        }
        return res[0];
    }



    private void getCityState(double lat, double lon) {
        if (lat != 0) {
            Thread thread = new Thread() {
                @Override
                public void run() {
                    Geocoder geocoder = new Geocoder(Draw.this, Locale.ENGLISH);
                    List<Address> addresses = new ArrayList<>();
                    try {
                        addresses = geocoder.getFromLocation(lat, lon, 1);
                        if (addresses.size() != 0) {
                            cityName = addresses.get(0).getLocality();
                            stateName = addresses.get(0).getAdminArea();
                            if (cityName == null) {
                                cityName = "";
                            }
                            if (stateName == null) {
                                stateName = "";
                            }
                            System.out.println("getCityState: " + cityName + " " + stateName);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
            thread.start();
        }
    }


    private void StopLocationService() {
        try {
            Intent intent = new Intent(getApplicationContext(), GoogleLocationService.class);
            intent.setAction(Constants.ACTION_STOP_LOCATION_SERVICE);
            startService(intent);
            System.out.println("Location service stopped...");
        } catch (Exception ignore) {
        }
    }

    private void removePolyline(int index) {
        try {
            if (index != -1 && index < totalMarker.size() - 1) {
                Polyline pTemp = markerpolylines.remove(index);
                pTemp.remove();
            }
            hideFinishButton();
            addPloyFillColor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addLastFirst(LatLng latLng, LatLng latLng2) {
        try {
            if (totalMarker.size() > 1) {
                if (lastFirst != null) {
                    lastFirst.remove();
                }
                lastFirst = mMap.addPolyline(new PolylineOptions().add(latLng, latLng2)
                        .width(10)
                        .color(Color.GREEN));
                // Toast.makeText(getApplicationContext(), latitude + ", " + longitude, Toast.LENGTH_SHORT).show();
                addPloyFillColor();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addPloyFillColor() {
        try {
            if (totalMarker.size() > 2) {
                ArrayList<LatLng> test = new ArrayList<>();
                for (int i = 0; i < totalMarker.size(); i++) {
                    test.add(totalMarker.get(i).getPosition());
                }
                if (polygon != null) {
                    polygon.remove();
                }
                polygon = mMap.addPolygon(new PolygonOptions()
                        .addAll(test)
                        .strokeWidth(0)
                        .fillColor(Color.parseColor("#9bd823fa")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void OneStepUndo() {
        System.out.println("TotalMarker size on long press: " + totalMarker.size());
        if (totalMarker.size() != 0) {

            if (totalMarker.size() == 1) {
                ClearMap();
            } else {

                System.out.println("delete_one_loc:" + totalMarker.get(totalMarker.size() - 1).getPosition().latitude + " " + totalMarker.get(totalMarker.size() - 1).getPosition().longitude);
                totalMarker.get(totalMarker.size() - 1).remove();
                totalMarker.remove(totalMarker.size() - 1);
                GetAreaOfFarm();
            }
            if (totalMarker.size() >= 1) {

                Polyline pTemp = markerpolylines.remove(markerpolylines.size() - 1);
                pTemp.remove();
                oldLatLon = totalMarker.get(totalMarker.size() - 1).getPosition();
            }
        }
        System.out.println("In delete marker, size: " + totalMarker.size());
    }

    private void HideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = getCurrentFocus();
        if (view == null) {
            view = new View(getApplicationContext());
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void ClearMap() {
        if (NextClicked) {
            mMap.clear();
            totalMarker.clear();
            if (mapmarker != null) {
                mapmarker.remove();
            }
            System.out.println("size: " + totalMarker.size());
            finishBtn.setVisibility(View.GONE);
            note.setVisibility(View.GONE);
            lastClicked = null;
            markerpolylines.clear();
            lastFirst = null;
            if (lat.getVisibility() == View.VISIBLE) {
                lat.setVisibility(View.GONE);
                lng.setVisibility(View.GONE);
            }
            Searchmycurrentpos = null;
            GetAreaOfFarm();
        }
    }

    private void hideFinishButton() {
        if (totalMarker.size() > 3) {
            finishBtn.setVisibility(View.VISIBLE);
        } else {
            finishBtn.setVisibility(View.GONE);
        }
    }
    private boolean isLocationServiceRunning() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null) {
            for (ActivityManager.RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE)) {
                if (LocationServices.class.getName().equals(service.service.getClassName())) {
                    if (service.foreground) {
                        return true;
                    }
                }
            }
            return false;
        }
        return false;
    }
    private void StartLocationService() {
        try {
            if (!isLocationServiceRunning()) {
                Intent intent = new Intent(getApplicationContext(), GoogleLocationService.class);
                intent.setAction(Constants.ACTION_START_LOCATION_SERVICE);
                startService(intent);
                System.out.println("Location service started...");
            }
        } catch (Exception ignored) {
        }
    }

    private void addPolyline(LatLng oldLatLon1, LatLng newLatLong1, int index) {
        System.out.println("MarkerIndex: " + index + " " + totalMarker.size());
        if (index < totalMarker.size() - 1) {
            polyline = mMap.addPolyline(new PolylineOptions().add(oldLatLon1, newLatLong1)
                    .width(10)
                    .color(Color.GREEN));
            markerpolylines.add(index, polyline);
        }
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }


    private void GetAreaOfFarm() {
        areaOfFarmTextView.setVisibility(View.GONE);
        if (totalMarker.size() > 2) {
            areaOfFarmTextView.setVisibility(View.VISIBLE);
            DecimalFormat precision = new DecimalFormat("0.000");

//            List<LatLng> area = new ArrayList<>();
//            for (int i = 0; i < totalMarker.size(); i++) {
//                area.add(totalMarker.get(i).getPosition());
//            }
//            areaofFarm = SphericalUtil.computeArea(area);
//
//            if (areaofFarm > 0 && areaofFarm < MAX_AREA) {
//                areaOfFarmTextView.setVisibility(View.VISIBLE);
//                areaOfFarmTextView.setText(getString(R.string.selectedarea) + precision.format(areaofFarm / 10000) + " " + getString(R.string.hectare));
//                areaOfFarmTextView.setTextColor(Color.WHITE);
//            } else if (areaofFarm > MAX_AREA) {
//                areaOfFarmTextView.setVisibility(View.VISIBLE);
//                areaOfFarmTextView.setText(getString(R.string.selectedarea) + precision.format(areaofFarm / 10000) + " " + getString(R.string.hectare));
//                areaOfFarmTextView.setTextColor(Color.RED);
//            } else if (areaofFarm == 0) {
//                areaOfFarmTextView.setVisibility(View.GONE);
//            }
        }
    }

    private void SaveFarmCood() {
        if (areaofFarm < MAX_AREA) {
            if (farmCategory.equalsIgnoreCase(FARM)) {
//                ShowAddDetailsDialog();
            }
//            else if (farmCategory.equalsIgnoreCase(TANK)) {
                SaveCoodGranted();
//            }
//        } else if (totalMarker.size() < 3) {
//            ShowErrorDialog(getString(R.string.fourcoodreqd), getString(R.string.totalpointsadded) + " " + totalMarker.size());
//        } else {
//            ShowErrorDialog(getString(R.string.farmareatoolarge), getString(R.string.farmsizelessthan));
//        }
        }


    }
    private void SaveCoodGranted() {
        finishBtn.setEnabled(false);
        coordinates = "";
        String t = "";
        progress.setTitle("Saving...");
        progress.setMessage("Please wait...");
        if (progress != null && !progress.isShowing()) {
            progress.show();
        }

        for (int i = 0; i < totalMarker.size(); i++) {
            t = "";
            if (i > 0) {
                coordinates = coordinates + "," + totalMarker.get(i).getPosition().toString().replace("lat/lng:", "").replace("(", "[").replace(")", "]").replace(" ", "");
                t = totalMarker.get(i).getPosition().toString().replace("lat/lng:", "").replace("(", "[").replace(")", "]").replace(" ", "");
                coordinatesToPass = coordinatesToPass + ",[" + t.replace("[", "").replace("]", "").split(",")[1] + "," + t.replace("[", "").replace("]", "").split(",")[0] + "]";
                System.out.println("i: " + i + ", Coor: " + coordinates + " coorTOPass: " + coordinatesToPass);
            } else {
                coordinates = coordinates + totalMarker.get(i).getPosition().toString().replace("lat/lng:", "").replace("(", "[").replace(")", "]").replace(" ", "");
                t = totalMarker.get(i).getPosition().toString().replace("lat/lng:", "").replace("(", "[").replace(")", "]").replace(" ", "");

                coordinatesToPass = t.replace("[", "").replace("]", "").split(",")[1];
                coordinatesToPass = "[" + coordinatesToPass + "," + t.replace("[", "").replace("]", "").split(",")[0] + "]";
            }
            if (i == totalMarker.size() - 1) {
                coordinates = coordinates + "," + totalMarker.get(0).getPosition().toString().replace("lat/lng:", "").replace("(", "[").replace(")", "]").replace(" ", "");
                t = totalMarker.get(0).getPosition().toString().replace("lat/lng:", "").replace("(", "[").replace(")", "]").replace(" ", "");
                coordinatesToPass = coordinatesToPass + ",[" + t.replace("[", "").replace("]", "").split(",")[1] + "," + t.replace("[", "").replace("]", "").split(",")[0] + "]";
            }
        }
        System.out.println("coordinates: " + coordinates + "\n Right Coor: " + coordinatesToPass);

        context = getApplicationContext();
        try {
            postRequest(coordinates, coordinatesToPass);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


