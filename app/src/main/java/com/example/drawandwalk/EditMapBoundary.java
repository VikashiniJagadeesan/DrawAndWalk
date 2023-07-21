package com.example.drawandwalk;

import static com.example.drawandwalk.Walk.FarmName;
import static com.example.drawandwalk.Walk.farmerNumber;
import static com.example.drawandwalk.Walk.totalMarker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.drawandwalk.uiSupport.Constants;
import com.example.drawandwalk.uiSupport.ShowDialogErrorSuccess;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.maps.android.SphericalUtil;

import org.json.JSONObject;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class EditMapBoundary extends AppCompatActivity implements OnMapReadyCallback {
    private final String surveyNo = "";
    private final Marker mycurrentpos = null;
    double areaofFarm = 0;
    private String sowingDate = "";
    private String isIrrigated = "No";
    private GoogleMap mMap;
    private String coordToStore = "", coordToSend = "";
//    private DatabaseHelper dbHelper;
    private ArrayList<Marker> totalMarker2;
    private ArrayList<Marker> FinalTotalMarker;        //in meters
    //    final double maxAreaOfFarm = 1000000;        //1000000 sqmtr = 100 Hectare.... give value in sqmtr
    private List<Polyline> markerpolylines;
    private Map<Marker, Integer> mMarkerMap;
    private ProgressDialog progress;
    private Button saveBtn;
    private LatLng oldLatLon = null;
    private Marker lastClicked = null;
    private int MarkerIndex = 0;
    private Polygon polygon;
    private Polyline polyline, lastFirst;
    private String cityName = "", stateName = "", cropType = "", cropAge = "-1";
    private TextView areaOfFarmTextView;
    private String farmCategory = "", organicInorganic = "inorganic";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_map_boundary);
        totalMarker2 = new ArrayList<>();
        markerpolylines = new ArrayList<>();
        FinalTotalMarker = new ArrayList<>();
        saveBtn = findViewById(R.id.saveFarm);
        areaOfFarmTextView = findViewById(R.id.areaOfFarm);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            if (bundle.containsKey("category")) {
                farmCategory = bundle.getString("category");

            }

        }
        mMarkerMap = new HashMap<Marker, Integer>();
        if (totalMarker != null) {

            totalMarker2 = Walk.totalMarker;

            try {
                if (totalMarker.size() != 0) {
                    getCityState(totalMarker.get(0).getPosition().latitude, totalMarker.get(0).getPosition().longitude);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        progress = new ProgressDialog(EditMapBoundary.this);
        progress.setTitle("Saving Farm.");
        progress.setMessage("Please Wait...");
        progress.setCancelable(false);
        System.out.println("totalMarker2.size(): " + totalMarker2.size());
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mMap);
        mapFragment.getMapAsync(this);

    }

    @SuppressLint("PotentialBehaviorOverride")
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(totalMarker2.get(0).getPosition(), 19));
//        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(11.0, 21.0), 18));
//        Mk(new LatLng(11.0, 21.0));
//        Mk(new LatLng(11.1, 21.1));
//        Mk(new LatLng(11.2, 21.3));
//        Mk(new LatLng(11.4, 21.2));
//        Mk(new LatLng(11.5, 21.1));
//        mMap.clear();

        loadMarker();

        GetAreaOfFarm();
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                if (lastClicked != null && lastClicked.equals(marker)) {
                    lastClicked = null;
                    marker.hideInfoWindow();
                    return true;
                } else {
                    lastClicked = marker;
                    return false;
                }
            }
        });


        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(@NonNull Marker marker) {
                try {
                    MarkerIndex = mMarkerMap.get(marker);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //Toast.makeText(EditMapBoundary.this, "" + MarkerIndex, Toast.LENGTH_SHORT).show();
                System.out.println("index: " + MarkerIndex);
            }

            @Override
            public void onMarkerDrag(@NonNull Marker marker) {


                removePolyline(MarkerIndex);
                if (MarkerIndex < FinalTotalMarker.size() - 1) {
                    addPolyline(marker.getPosition(), FinalTotalMarker.get(MarkerIndex + 1).getPosition(), MarkerIndex);
                }

                removePolyline(MarkerIndex - 1);
                if (MarkerIndex != 0) {
                    addPolyline(FinalTotalMarker.get(MarkerIndex - 1).getPosition(), marker.getPosition(), MarkerIndex - 1);
                } else {
                    addLastFirst(FinalTotalMarker.get(0).getPosition(), FinalTotalMarker.get(FinalTotalMarker.size() - 1).getPosition());
                }
                if (MarkerIndex == FinalTotalMarker.size() - 1) {
                    addLastFirst(FinalTotalMarker.get(0).getPosition(), FinalTotalMarker.get(FinalTotalMarker.size() - 1).getPosition());
                }
            }

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onMarkerDragEnd(@NonNull Marker marker) {

                System.out.println("FinalTotalMarker.size():" + FinalTotalMarker.size());
                if (MarkerIndex < FinalTotalMarker.size()) {
                    FinalTotalMarker.set(MarkerIndex, marker);
                    mMarkerMap.replace(marker, MarkerIndex);
                }
                oldLatLon = FinalTotalMarker.get(FinalTotalMarker.size() - 1).getPosition();
                marker.setAlpha(1.0f);
                GetAreaOfFarm();
            }
        });

        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Nullable
            @Override
            public View getInfoWindow(@NonNull Marker marker) {
                View view = null;

                if (!marker.equals(mycurrentpos)) {
                    if (FinalTotalMarker.size() > 4) {
                        view = View.inflate(EditMapBoundary.this, R.layout.item_delete, null);
                    }
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
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onInfoWindowClick(@NonNull Marker marker) {
                if (FinalTotalMarker.size() > 1) {
                    MarkerIndex = FinalTotalMarker.indexOf(marker);
                    if (MarkerIndex == FinalTotalMarker.size() - 1) {
                        if (lastFirst != null) {
                            lastFirst.remove();
                        }
                        removePolyline(MarkerIndex - 1);
                        FinalTotalMarker.remove(FinalTotalMarker.size() - 1);
                        mMarkerMap.remove(marker);
                        reloadMarkerIndex(FinalTotalMarker.size());
                        marker.remove();
                        addLastFirst(FinalTotalMarker.get(0).getPosition(), FinalTotalMarker.get(FinalTotalMarker.size() - 1).getPosition());
                        oldLatLon = FinalTotalMarker.get(FinalTotalMarker.size() - 1).getPosition();
                    } else if (MarkerIndex == 0) {
                        if (lastFirst != null) {
                            lastFirst.remove();
                        }
                        FinalTotalMarker.remove(0);
                        mMarkerMap.remove(marker);

                        reloadMarkerIndex(0);
                        marker.remove();
                        addLastFirst(FinalTotalMarker.get(0).getPosition(), FinalTotalMarker.get(FinalTotalMarker.size() - 1).getPosition());
                        removePolyline(0);

                    } else {
                        removePolyline(MarkerIndex);

                        int i = FinalTotalMarker.indexOf(marker);
                        FinalTotalMarker.remove(marker);
                        mMarkerMap.remove(marker);

                        removePolyline(MarkerIndex - 1);
                        reloadMarkerIndex(i);
                        addPolyline(FinalTotalMarker.get(MarkerIndex - 1).getPosition(), FinalTotalMarker.get(MarkerIndex).getPosition(), MarkerIndex - 1);
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
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GetAreaOfFarm();
                if (areaofFarm > Constants.MAX_AREA_OF_FARM) {
                    ShowErrorDialog(getString(R.string.farmareatoolarge), getString(R.string.farmsizelessthan));

                } else {

                    ShowAddDetailsDialog();
                }
            }
        });

    }


    private void removePolyline(int index) {

        try {
            if (index != -1 && index < mMarkerMap.size() - 1) {
                Polyline pTemp = markerpolylines.remove(index);
                pTemp.remove();
            }
            //  hideFinishButton();
            addPloyFillColor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addLastFirst(LatLng latLng, LatLng latLng2) {
        try {
            if (FinalTotalMarker.size() > 1) {
                if (lastFirst != null) {
                    lastFirst.remove();
                }
                lastFirst = mMap.addPolyline(new PolylineOptions().add(latLng, latLng2)
                        .width(10)
                        .color(Color.GREEN));
                addPloyFillColor();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addPloyFillColor() {
        try {
            if (FinalTotalMarker.size() > 2) {
                ArrayList<LatLng> test = new ArrayList<>();
                for (int i = 0; i < FinalTotalMarker.size(); i++) {
                    test.add(FinalTotalMarker.get(i).getPosition());

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

    private void addPolyline(LatLng oldLatLon1, LatLng newLatLong1, int index) {
        System.out.println("MarkerIndex: " + index + " " + FinalTotalMarker.size() + " " + markerpolylines.size());
        if (index < FinalTotalMarker.size() - 1) {
            polyline = mMap.addPolyline(new PolylineOptions().add(oldLatLon1, newLatLong1)
                    .width(10)
                    .color(Color.GREEN));
            markerpolylines.add(index, polyline);
        }
    }

    private void ClearMap() {
        mMap.clear();
    }

    private void drawMarker(LatLng point, int positionInLocationList) {

        System.out.println("positionInLocationList: " + positionInLocationList);
        MarkerOptions markerOptions = new MarkerOptions().draggable(true);
        markerOptions.position(point);

        Marker marker = mMap.addMarker(markerOptions);

        mMarkerMap.put(marker, positionInLocationList);
        FinalTotalMarker.add(marker);

        if (FinalTotalMarker.size() > 1) {
            addPolyline(FinalTotalMarker.get(positionInLocationList - 1).getPosition(),
                    FinalTotalMarker.get(positionInLocationList).getPosition(), positionInLocationList - 1);
        }

    }

    private void loadMarker() {
        mMarkerMap = new HashMap<>();
        for (int i = 0; i < totalMarker2.size(); i++) {
            drawMarker(totalMarker2.get(i).getPosition(), i);
        }
        addLastFirst(totalMarker2.get(0).getPosition(), totalMarker2.get(totalMarker2.size() - 1).getPosition());

        if (progress != null && progress.isShowing()) {
            progress.dismiss();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void reloadMarkerIndex(int index) {
        System.out.println("sizzezzzz: " + FinalTotalMarker.size() + "  " + mMarkerMap.size() + " " + FinalTotalMarker.size());
        if (index != 0) {
            for (int i = 0; i < FinalTotalMarker.size(); i++) {
                boolean greater = false;
                System.out.println("totalMarker2-: " + FinalTotalMarker.get(i));

                for (int j = 0; j < mMarkerMap.size(); j++) {
                    System.out.println("mMarkerMap-: " + mMarkerMap.get(FinalTotalMarker.get(i)));

                    if (mMarkerMap.containsKey(FinalTotalMarker.get(i))) {
                        if (mMarkerMap.get(FinalTotalMarker.get(i)) > index) {
                            greater = true;
                            break;
                        }
                    }
                }
                if (greater) {
                    System.out.println("mMarkerMap Old: " + mMarkerMap.get(FinalTotalMarker.get(i)) + " " + FinalTotalMarker.get(i));
                    if ((mMarkerMap.get(FinalTotalMarker.get(i)) - 1) != 0) {
                        mMarkerMap.replace(FinalTotalMarker.get(i), mMarkerMap.get(FinalTotalMarker.get(i)) - 1);
                        System.out.println("mMarkerMap New: " + mMarkerMap.get(FinalTotalMarker.get(i)) + " " + FinalTotalMarker.get(i));
                    }
                }
            }
        } else {
            for (int i = 0; i < FinalTotalMarker.size(); i++) {
                boolean contains = false;
                for (int j = 0; j < mMarkerMap.size(); j++) {

                    if (mMarkerMap.containsKey(FinalTotalMarker.get(i))) {
                        contains = true;
                        break;
                    }
                }
                if (contains) {
                    mMarkerMap.replace(FinalTotalMarker.get(i), mMarkerMap.get(FinalTotalMarker.get(i)) - 1);
                }
            }
        }
    }

    private void Mk(LatLng point) {
        MarkerOptions markerOption = new MarkerOptions()
                .draggable(true)
                .position(point);
        Marker m = mMap.addMarker(markerOption);
        totalMarker2.add(m);
    }

    private void SaveFarm() {

        if (!progress.isShowing()) {
            progress.show();
        }
        coordToStore = "";
        coordToSend = "";
        //https://micro.satyukt.com/postjson2?key=L4TMHHrQDRWvlR-hGH6r4yuTsAqUirDQ25xehKijTg8=&name=grmt2&coordinates=[[16.001593043146997,74.47079703211784],[16.0020513326432,74.47328746318817],[16.00270460284333,74.47332266718149],[16.002155430603068,74.47061229497194],[16.00213190382432,74.470578096807],[16.001903726155113,74.47067733854055],[16.001593043146997,74.47079703211784]]&croptype=Tomato&cropage=34
        for (int i = 0; i < FinalTotalMarker.size(); i++) {
            //coordToSend in internal data(correct)
            coordToSend += "[" + FinalTotalMarker.get(i).getPosition().longitude + ",";
            coordToSend += FinalTotalMarker.get(i).getPosition().latitude + "],";


            //coordToStore in internal data(reversed)
            coordToStore += "[" + FinalTotalMarker.get(i).getPosition().latitude + ",";
            coordToStore += FinalTotalMarker.get(i).getPosition().longitude + "],";
        }
        coordToSend += "[" + FinalTotalMarker.get(0).getPosition().longitude + ",";
        coordToSend += FinalTotalMarker.get(0).getPosition().latitude + "]";

        coordToStore += "[" + FinalTotalMarker.get(0).getPosition().latitude + ",";
        coordToStore += FinalTotalMarker.get(0).getPosition().longitude + "]";

        coordToSend.replaceAll(" ", "");
        coordToStore.replaceAll(" ", "");

        System.out.println("coordToStore: " + coordToStore);
        System.out.println("coordToSend: " + coordToSend);


        try {

            String sts = postRequest(coordToSend, coordToStore);
            //String status = postRequest("[" + coordToSend + "]", "[" + coordToStore + "]");

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    protected String postRequest(final String coord, final String storeCoord) throws IOException {

        System.out.println("coord " + coord);
        System.out.println("storeCoord " + storeCoord);
        final String[] res = new String[1];
//        final String url = "https://micro.satyukt.com/postjson2?key=" + SessionManagement.getApiKey(EditMapBoundary.this) + "&name=" + FarmName + "&coordinates=" + coord + "&croptype="+cropType+"&cropage="+cropAge+"&phone_num=" + farmerNumber + "&District=" + cityName + "&State=" + stateName;
        String url = "";

//        if (!SessionManagement.getUserName(this).equals(getString(R.string.admintestnum))) {
//            System.out.println("if --- storeCoord " + storeCoord);
//            url = getString(R.string.baseUrl) + "postjson2?key=" + SessionManagement.getApiKey(EditMapBoundary.this) + "&name=" + FarmName + "&coordinates=[" + coord + "]&croptype=" + cropType.replace(" ", "_") + "&cropage=" + cropAge + "&phone_num=" + farmerNumber + "&District=" + cityName + "&State=" + stateName + "&category=" + farmCategory;
//        } else {
//            System.out.println("else --- storeCoord " + storeCoord);
//            url = getString(R.string.baseUrl) + "postjson2?key=" + SessionManagement.getApiKey(EditMapBoundary.this) + "&name=" + FarmName + "&coordinates=[" + coord + "]&croptype=" + cropType.replace(" ", "_") + "&cropage=" + cropAge + "&phone_num=" + surveyNo + "_" + isIrrigated + "&District=" + cityName + "&State=" + stateName + "&category=" + farmCategory;
//
//        }
        url = "https://micro.satyukt.com/postjson2?key=LPBKIPdyLT3ps0gg2221PTSjRpMC6DRzdJ56HcDjfuY=&name=" + FarmName + "&coordinates=[" + coord + "]&croptype=" + cropType.replace(" ", "_") + "&cropage=" + cropAge + "&phone_num=" + surveyNo + "_" + isIrrigated + "&District=" + cityName + "&State=" + stateName + "&category=" + farmCategory;

        System.out.println("url: " + url);

        final JSONObject[] jsonArray = new JSONObject[1];
        RequestQueue queue = Volley.newRequestQueue(EditMapBoundary.this);
        String finalUrl = url;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            System.out.println("In postResponse: " + response);
                            jsonArray[0] = new JSONObject(response);
                            System.out.println("Center: " + jsonArray[0].getString("Center") + " ID: " + jsonArray[0].getString("ID"));
                            String center = jsonArray[0].getString("Center");
                            String farmId = jsonArray[0].getString("ID");
                            res[0] = "1";
                            System.out.println("Json Center: " + center);
                            String newCenter = center.split(",")[0].replace("]", "");
                            newCenter = newCenter + "," + center.split(",")[1].replace("[", "");
                            System.out.println("newCenter: " + newCenter);
                            //                            addFarmToDb(MainActivity.apiKey, farmId, getFarmName, center, storeCoord);
                            System.out.println("StoreCoord: " + storeCoord + "\nCoord: " + coord);
//                            addFarmToDb(SessionManagement.getApiKey(EditMapBoundary.this), farmId, FarmName, newCenter, coordToStore, finalUrl, res[0]);
                            //finish();

                        } catch (Exception e) {
//                            e.printStackTrace();
//                            System.out.println("Didnt work");
//                            res[0] = "0";
//                            int defFarmId = SessionManagement.getDefaultFarmId(getApplicationContext());
//                            SessionManagement.setDefaultFarmId(++defFarmId, getApplicationContext());
//
//                            String dummyCenter = "[" + totalMarker2.get(0).getPosition().longitude + ",";
//                            dummyCenter += totalMarker2.get(0).getPosition().latitude + "]";
//                            System.out.println("dummyCenter: " + dummyCenter);
//                            addFarmToDb(SessionManagement.getApiKey(EditMapBoundary.this), String.valueOf(++defFarmId), FarmName, dummyCenter, coordToStore, finalUrl, res[0]);

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
//                String dummyCenter = "[" + totalMarker2.get(0).getPosition().longitude + ",";
//                dummyCenter += totalMarker2.get(0).getPosition().latitude + "]";
//                System.out.println("dummyCenter: " + dummyCenter);
//                addFarmToDb(SessionManagement.getApiKey(EditMapBoundary.this), String.valueOf(++defFarmId), FarmName, dummyCenter, coordToStore, finalUrl, res[0]);

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


//    protected void addFarmToDb(String apiKey, String farmID, String farmName, String center, String coordinates, String apiLink, String reqStatus) {
//
////        coordinates = coordinates.substring(0, coordinates.length() - 2);
////        System.out.println("In addFarmToDb, coordinates: " + coordinates + ", \nCenter:" + center);
////        dbHelper = new DatabaseHelper(EditMapBoundary.this, null, null, DatabaseHelper.DATABASE_VERSION);
////        boolean tableExist = dbHelper.tableExist;
////
////        if (!tableExist) {
////            FarmDetailsForDB farmDetails;
////            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
////            farmDetails = new FarmDetailsForDB(apiKey, farmID, farmName, center, coordinates, apiLink, reqStatus, timestamp.toString(), cropType.replace(" ", "_"), cropAge, sowingDate, farmCategory);
////            dbHelper.addHandler(farmDetails, "userUpload");
////            String s = dbHelper.loadHandler("userUpload");
////            System.out.println("db updated: " + s);
////            boolean t = dbHelper.checkForTableExists("userUpload");
////            System.out.println("Table exists");
////            String st = dbHelper.loadHandler("userUpload");
////            System.out.println("st: " + st);
////            goToHome();
//
//        } else
////            System.out.println("tableExist: " + tableExist);
//    }

    private void goToHome() {

        try {
//            NewhomeFragment.refreshFarms = true;
        } catch (Exception ignore) {
        }
        if (progress.isShowing()) {
            progress.dismiss();
        }
//        Intent intent = new Intent(getApplicationContext(), NavigationDrawerActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        startActivity(intent);

    }

    @Override
    public void onBackPressed() {
        AlertDialog alertbox = new AlertDialog.Builder(EditMapBoundary.this)
                .setMessage("Farm not saved, go back?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        goToHome();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        arg0.dismiss();
                    }
                })
                .show();
    }

    private void getCityState(double lat, double lon) {

        Thread thread = new Thread() {
            @Override
            public void run() {
                Geocoder geocoder = new Geocoder(EditMapBoundary.this, Locale.ENGLISH);
                List<Address> addresses = null;

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

    private void GetAreaOfFarm() {

        areaOfFarmTextView.setVisibility(View.GONE);
        if (FinalTotalMarker.size() > 2) {
            areaOfFarmTextView.setVisibility(View.VISIBLE);
            DecimalFormat precision = new DecimalFormat("0.000");

            List<LatLng> areaList = new ArrayList<>();

            for (int i = 0; i < FinalTotalMarker.size(); i++) {
                areaList.add(FinalTotalMarker.get(i).getPosition());
            }
            areaofFarm = SphericalUtil.computeArea(areaList);

            if (areaofFarm > 0 && areaofFarm < Constants.MAX_AREA_OF_FARM) {
                areaOfFarmTextView.setVisibility(View.VISIBLE);
                areaOfFarmTextView.setText(getString(R.string.selectedarea) + precision.format(areaofFarm / 10000) + " " + getString(R.string.hectare));
                areaOfFarmTextView.setTextColor(Color.WHITE);
            } else if (areaofFarm > Constants.MAX_AREA_OF_FARM) {
                areaOfFarmTextView.setVisibility(View.VISIBLE);
                areaOfFarmTextView.setText(getString(R.string.selectedarea) + precision.format(areaofFarm / 10000) + " " + getString(R.string.hectare));
                areaOfFarmTextView.setTextColor(Color.RED);
            } else if (areaofFarm == 0) {
                areaOfFarmTextView.setVisibility(View.GONE);
            }
        }
    }

    private void ShowErrorDialog(String titletxt, String desctxt) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.my_dialog);
        View mV = getLayoutInflater().inflate(R.layout.dailog_show_error, null);

        Button btnok = mV.findViewById(R.id.okbtn);
        TextView title = mV.findViewById(R.id.dialog_title);
        TextView desc = mV.findViewById(R.id.dailog_desc);
        title.setText(titletxt);
        desc.setText(desctxt);
        builder.setView(mV);

        final AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        if (!dialog.isShowing()) {
            dialog.show();
            dialog.getWindow().setLayout((int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        btnok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    private void ShowAddDetailsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.my_dialog);
        View mV = getLayoutInflater().inflate(R.layout.popup_addfarmdetails, null);

        EditText surveyNum = mV.findViewById(R.id.surveyNo);
        LinearLayout surveyNoll = mV.findViewById(R.id.surveyNoll);
//        Spinner crop_type = mV.findViewById(R.id.crop_type);
        TextView crop_agetxt = mV.findViewById(R.id.crop_agetxt);
        LinearLayout isIrrigatedLayout = mV.findViewById(R.id.isIrrigatedLayout);
        SwitchMaterial switchfarmirrigated = mV.findViewById(R.id.switchfarmirrigated);

        RadioGroup grpOrganic = mV.findViewById(R.id.grpOrganic);
        organicInorganic = "inorganic";
        grpOrganic.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (i == R.id.btnInOrganic) {
                    organicInorganic = "inorganic";
                } else {
                    organicInorganic = "organic";
                }
                System.out.println("organicInorganic: " + organicInorganic);
            }
        });

        isIrrigated = "No";
        switchfarmirrigated.setText("No");
//        if (!SessionManagement.getUserName(this).equals(getString(R.string.admintestnum))) {
//            surveyNoll.setVisibility(View.GONE);
//            isIrrigatedLayout.setVisibility(View.GONE);
//        }

        Button nxt_btn = mV.findViewById(R.id.nxt_btn);
        ImageView closeDialog = mV.findViewById(R.id.closeDialog);
        builder.setView(mV);

        final AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        if (!dialog.isShowing()) {
            dialog.show();
            dialog.getWindow().setLayout((int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        crop_agetxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance(TimeZone.getDefault());
                DatePickerDialog datePicker = new android.app.DatePickerDialog(EditMapBoundary.this, 0, new android.app.DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        sowingDate = dayOfMonth + "/" + (month + 1) + "/" + year;
                        crop_agetxt.setText(sowingDate);
                        try {
                            cropAge = GetDays(dayOfMonth + "/" + (month + 1) + "/" + year);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
                datePicker.setCancelable(false);
                datePicker.getDatePicker().setMaxDate(System.currentTimeMillis());
                datePicker.show();
            }
        });

        switchfarmirrigated.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    isIrrigated = "Yes";
                    buttonView.setText("Yes");
                } else {
                    isIrrigated = "No";
                    buttonView.setText("No");
                }
            }
        });

        closeDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

//        crop_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                if (position == 0) {
//                    cropType = "";
//                } else {
//                    Configuration confTmp = new Configuration(getResources().getConfiguration());
//                    confTmp.locale = new Locale("en");
//                    DisplayMetrics metrics = new DisplayMetrics();
//                    getWindowManager().getDefaultDisplay().getMetrics(metrics);
//                    Resources resources = new Resources(getAssets(), metrics, confTmp);
////                    String[] CropArray2 = getResources().getStringArray(R.array.crop_type);
////                    cropType = CropArray2[position];
//                    System.out.println("cropType en: " + cropType);
//                    loadLocale();
//
//                    System.out.println("cropType: " + cropType);
//                }
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//                cropType = "";
//            }
//        });


        nxt_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HideKeyboard(dialog);

                SaveFarm();
//                boolean a = false;
//                if (SessionManagement.getUserName(EditMapBoundary.this).equals(getString(R.string.admintestnum))) {
//                    if (surveyNum.getText().toString().length() == 0) {
//                        surveyNum.setError("Please enter survey number.");
//                    } else {
//                        a = true;
//                    }
//                } else {
//                    a = true;
//                }
//                if (cropType.equals("")) {
//                    Toast.makeText(getApplicationContext(), "Please select a Crop.", Toast.LENGTH_SHORT).show();
//                } else if (cropAge.equals("-1")) {
//                    crop_agetxt.setError("Please enter survey number.");
//                } else {
//                    surveyNo = surveyNum.getText().toString().trim();
//                    if (a) {
////                        SaveCoodGranted();
//                        SaveFarm();
//                        dialog.dismiss();
//                    }
//                }

            }
        });
    }

    private void HideKeyboard(AlertDialog dialog) {
        InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        im.hideSoftInputFromWindow(dialog.getWindow().getDecorView().getWindowToken(), 0);
    }

    private void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
        editor.putString("My_Lang", lang);
        editor.apply();
    }

    public void loadLocale() {
        System.out.println("In localLocale");
        SharedPreferences prefs = getSharedPreferences("Settings", MODE_PRIVATE);
        String language = prefs.getString("My_Lang", "");
        setLocale(language);
    }

    private String GetDays(String sowingdate) throws ParseException {
        Date userDob = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(sowingdate);
        Date today = new Date();
        long diff = today.getTime() - userDob.getTime();
        int numOfDays = (int) (diff / (1000 * 60 * 60 * 24));
        System.out.println("GetDays: " + numOfDays);
        return String.valueOf(numOfDays);
    }

}