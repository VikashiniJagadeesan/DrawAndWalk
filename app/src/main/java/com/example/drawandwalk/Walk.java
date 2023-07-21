package com.example.drawandwalk;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.drawandwalk.uiSupport.Constants;
import com.example.drawandwalk.uiSupport.DisableNavigation;
import com.example.drawandwalk.uiSupport.GoogleLocationService;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class Walk extends AppCompatActivity  implements OnMapReadyCallback {
    public static String FarmName = "", farmerNumber = "";//, cityName , stateName;
    public static ArrayList<LatLng> totalLatLng;
    public static ArrayList<Marker> totalMarker;
    public static List<LatLng> markerlatLngs_area;
    public static List<Polyline> markerpolylines;
    final String TAG = "info";
    private final ArrayList<ArrayList<Double>> testCoord = new ArrayList<ArrayList<Double>>();
    private final boolean i = false;
    double areaofFarm = 0;
    private String sowingDate = "";
    private GoogleMap mMap;
//    private DatabaseHelper dbHelper;
    private LocationManager locationManager;
    private double latitude, longitude;
    private BitmapDescriptor userPositionMarkerBitmapDescriptor;
    private boolean initLoad = false;
    private boolean locRecord = false;
    private boolean nxtClicked = false;
    private double MyAccuracy;
    private RelativeLayout relativeLayout;
    private ImageButton recordLocation;
    private Button nextBtn;
    private TextView farmName, areaOfFarmTextView, startstoptxt, crop_agetxt;
    private Spinner cropTypeSpinner;
    private String getFarmName, cropAge = "-1", cropType = "";
    private ProgressDialog progress;
    private DisableNavigation dv;
    private Polyline polyline;
    private EditText farmernumtxt;
    private Switch switchfarm;
    private LatLng oldLatLon = null;
    private BroadcastReceiver broadcastReceiver;
    private CameraPosition cameraPosition;

    @Override
    protected void onStart() {
        super.onStart();
        StartLocationService();
        registerReceiver(broadcastReceiver, new IntentFilter("LOCATION_UPDATE"));

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        StopLocationService();
        if (broadcastReceiver != null) {
            try {
                unregisterReceiver(broadcastReceiver);
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        StartLocationService();
        if (broadcastReceiver != null) {
            try {
                registerReceiver(broadcastReceiver, new IntentFilter("LOCATION_UPDATE"));
            } catch (Exception e) {
                System.out.println(e);
            }
        }
        try {
            ClearMap();
            LoadMarkers();
        } catch (Exception ignored) {
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
//        StopLocationService();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
//        StopLocationService();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walk);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(Walk.this);

        progress = new ProgressDialog(Walk.this);
        progress.setCancelable(false);

        totalLatLng = new ArrayList<LatLng>();
        totalMarker = new ArrayList<Marker>();
        markerlatLngs_area = new ArrayList<>();
        markerpolylines = new ArrayList<>();


        dv = new DisableNavigation(this.getWindow().getDecorView());
        relativeLayout = findViewById(R.id.farmDetails);
        recordLocation = findViewById(R.id.record);
        nextBtn = findViewById(R.id.nxt_btn);
        farmName = findViewById(R.id.farm_name);
//        cropTypeSpinner = findViewById(R.id.crop_type);
        // cropAgeSpinner = (Spinner) findViewById(R.id.crop_age);
        areaOfFarmTextView = findViewById(R.id.areaOfFarm);
        startstoptxt = findViewById(R.id.startstoptxt);

        crop_agetxt = findViewById(R.id.crop_agetxt);

        switchfarm = findViewById(R.id.switchfarm);
        farmernumtxt = findViewById(R.id.farmernumtxt);


        switchfarm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    farmernumtxt.setVisibility(View.VISIBLE);
                } else {
                    farmernumtxt.setVisibility(View.GONE);
                    HideKeyboard();
                }
            }
        });

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
                        System.out.println("Walk: Lat= " + latitude + "  Lon= " + longitude + "  Accuracy= " + MyAccuracy);

                        if (MyAccuracy != 0 & MyAccuracy < 40) {                //min accuracy
                            if (progress != null && progress.isShowing()) {
                                progress.dismiss();
                            }
                            LatLng latLng = new LatLng(latitude, longitude);

                            if (!initLoad && !locRecord) {
                                userPositionMarkerBitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.user_position_point);
                                initLoad = true;
                            }
                            cameraPosition = new CameraPosition.Builder().target(new LatLng(latitude, longitude)).zoom(20).build();
                            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                            if (locRecord) {

                                System.out.println("distance :" + GetDistance(oldLatLon, latLng));
                                if (oldLatLon != null) {
                                    if (GetDistance(oldLatLon, latLng) > Constants.DISTANCE_BETWEEN_COOD) {               //min distance between two coordinates
                                        markerlatLngs_area.add(new LatLng(latitude, longitude));
                                        GetAreaOfFarm();

                                        addPolylineAuto(oldLatLon, latLng);
                                        totalLatLng.add(latLng);

                                        // Toast.makeText(getApplicationContext(), latitude + ", " + longitude, Toast.LENGTH_SHORT).show();
                                        userPositionMarkerBitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.inaccurate_location_marker);
                                        MarkerOptions options = new MarkerOptions().position(latLng).title(String.valueOf(totalLatLng.size())).icon(userPositionMarkerBitmapDescriptor);
                                        mMap.addMarker(options);

                                        totalMarker.add(mMap.addMarker(options));
                                        System.out.println("totalMarker.add(mMap.addMarker(options)): " + totalMarker.size());
                                        oldLatLon = latLng;
                                    }
                                } else {
                                    oldLatLon = latLng;
                                    totalLatLng.add(latLng);
                                }

                            } else {
                                if (nxtClicked) {
                                    mMap.clear();
                                    userPositionMarkerBitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.user_position_point);
                                    MarkerOptions options = new MarkerOptions().position(latLng).title("I am here!").icon(userPositionMarkerBitmapDescriptor);
                                    mMap.addMarker(options);
                                }
                            }
                        } else {
                            if (nxtClicked) {
                                ShowLoadingAccuracy();
                            }
                        }
                    }
                }else {
                    System.out.println("broadddd");
                }
            }
        };

//        cropTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                Configuration confTmp = new Configuration(getResources().getConfiguration());
//                confTmp.locale = new Locale("en");
//                DisplayMetrics metrics = new DisplayMetrics();
//                getWindowManager().getDefaultDisplay().getMetrics(metrics);
//                Resources resources = new Resources(getAssets(), metrics, confTmp);
////                String[] CropArray = getResources().getStringArray(R.array.crop_type);
////
////                cropType = CropArray[position];
//                System.out.println("SelectedCrop: " + cropType);
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//            }
//        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast toast;
                HideKeyboard();

                System.out.println("CropTyoe: " + cropType);
                System.out.println("CropAge: " + cropAge);
                getFarmName = farmName.getText().toString().trim();
                FarmName = getFarmName;
                if (farmernumtxt.getVisibility() == View.VISIBLE) {
                    farmerNumber = farmernumtxt.getText().toString();
                }

                if (getFarmName.equals("")) {

                    toast = Toast.makeText(Walk.this, R.string.no_name_error, Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
//                    TextView toastMessage = (TextView) toast.getView().findViewById(android.R.id.message);
//                    toastMessage.setTextColor(Color.RED);
                    toast.show();
                } else if (farmernumtxt.getVisibility() == View.VISIBLE && farmernumtxt.getText().toString().length() != 10 && farmernumtxt.getText().toString().length() != 0) {

                    toast = Toast.makeText(Walk.this, "Enter correct phone number", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
//                    TextView toastMessage = (TextView) toast.getView().findViewById(android.R.id.message);
//                    toastMessage.setTextColor(Color.RED);
                    toast.show();
                } else {
                    nxtClicked = true;
                    boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                    if (!isGPSEnabled && nxtClicked) {
                        buildAlertMessageNoGps();
                    }
                    if (nxtClicked && isGPSEnabled) {
                        ShowLoadingAccuracy();
                    }
                    //getIndexCropAge = cropAgeSpinner.getSelectedItemPosition();
                    //getIndexCropType = cropTypeSpinner.getSelectedItemPosition();

//                    dbHelper = new DatabaseHelper(AddFarmWalk.this, null, null, DatabaseHelper.DATABASE_VERSION);
//                    int count = dbHelper.checkAvailability("farmName", getFarmName, "userUpload");
//                    System.out.println("count: " + count);
                      int count=0;
                    if (count <= 0) {


                        relativeLayout.setVisibility(View.GONE);
                        System.out.println("Next click, layout visible?: " + (relativeLayout.getVisibility() == View.GONE));
                        System.out.println("CropTyoe: " + cropType);

                    } else {

                        toast = Toast.makeText(Walk.this, R.string.farm_exist_error, Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);

                        toast.show();

                    }
                }
            }
        });

        recordLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // startstoptxt.setText("Stop");

                System.out.println("nxtClicked: " + nxtClicked);
                if (nxtClicked) {
                    boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                    if (isGPSEnabled) {

                        ShowLoadingAccuracy();
                        System.out.println("MyAccuracy=" + MyAccuracy);
                        if ((int) MyAccuracy < 100 && (int) MyAccuracy > 0) {

                            HideLoadingAccuracy();

                            if (!locRecord) {
                                locRecord = true;
                                startstoptxt.setText("Stop");
                                recordLocation.setImageResource(R.drawable.run_stop_button);
                            } else {
                                //  int i=0;

                                System.out.println("Sizeeeee: " + totalLatLng.size() + " " + totalLatLng.size());
                                for (int i = 0; i < (totalMarker.size()); i++) {
                                    System.out.println("latlon: " + totalMarker.get(i).getPosition());
                                }
                                if (areaofFarm < Constants.MAX_AREA_OF_FARM) {

                                    if (totalMarker.size() < 4) {
                                        ShowErrorDialog(getString(R.string.fourcoodreqd), getString(R.string.totalpointsadded) + " " + totalMarker.size());
                                    } else {

                                        startstoptxt.setText("Start");
                                        HideLoadingAccuracy();

                                        locRecord = false;
                                        recordLocation.setEnabled(false);
                                        recordLocation.setImageResource(R.drawable.run_stop_button_disabled);

                                        startActivity(new Intent(getApplicationContext(), EditMapBoundary.class));

                                    }
                                } else {
                                    ShowErrorDialog(getString(R.string.farmareatoolarge), getString(R.string.farmsizelessthan));
                                }
                            }
                        } else {
                            if (progress != null && progress.isShowing()) {
                                progress.show();
                            }
                            Toast.makeText(Walk.this, "Wait for Accurate Location", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        buildAlertMessageNoGps();
                        Toast.makeText(Walk.this, "Please Enable GPS", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

//        cropTypeSpinner.setOnTouchListener(new View.OnTouchListener() {
//
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                HideKeyboard();
//                return false;
//            }
//        });

        crop_agetxt.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance(TimeZone.getDefault());

                DatePickerDialog datePicker = new android.app.DatePickerDialog(Walk.this, 0, new android.app.DatePickerDialog.OnDateSetListener() {
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

    }

    private void addPolylineAuto(LatLng oldlatlon, LatLng newlatlon) {
        polyline = mMap.addPolyline(new PolylineOptions().add(oldlatlon, newlatlon)
                .width(10)
                .color(Color.GREEN));
        markerpolylines.add(polyline);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        System.out.println("onMapReady");
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        LatLng latLng = new LatLng(latitude, longitude);
        if (!initLoad) {
            userPositionMarkerBitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.user_position_point);
            initLoad = true;
        } else
            userPositionMarkerBitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.inaccurate_location_marker);

        MarkerOptions options = new MarkerOptions().position(latLng).title(String.valueOf(totalLatLng.size())).icon(userPositionMarkerBitmapDescriptor);
        googleMap.addMarker(options);
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

    private void StartLocationService() {
        if (!isLocationServiceRunning()) {
            Intent intent = new Intent(getApplicationContext(), GoogleLocationService.class);
            intent.setAction(Constants.ACTION_START_LOCATION_SERVICE);
            startService(intent);
            System.out.println("Location service started...");
        }
    }

    private void StopLocationService() {
        try {
            // if (isLocationServiceRunning()) {
            Intent intent = new Intent(getApplicationContext(), GoogleLocationService.class);
            intent.setAction(Constants.ACTION_STOP_LOCATION_SERVICE);
            startService(intent);
            System.out.println("Location service stopped...");
        } catch (Exception ignore) {
        }
        // }
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

    private double GetDistance(LatLng old, LatLng new1) {
        if (old != null) {
            return SphericalUtil.computeDistanceBetween(old, new1);
        }
        return 0;
    }

    private void GetAreaOfFarm() {
        DecimalFormat precision = new DecimalFormat("0.000");
        areaofFarm = SphericalUtil.computeArea(markerlatLngs_area);

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

    private void ShowLoadingAccuracy() {
        if (progress != null && progress.isShowing()) {
            progress.dismiss();
        }
        progress = new ProgressDialog(Walk.this);
        progress.setTitle(getString(R.string.gettingaccurateloc) + "\n" + getString(R.string.youraccuracyis) + " " + (int) MyAccuracy + " m");
        progress.setMessage(getString(R.string.pleasewait));
        progress.setCancelable(false);
        if (progress != null && !progress.isShowing()) {
            progress.show();
        }
    }

    private void HideLoadingAccuracy() {
        if (progress != null && progress.isShowing()) {
            progress.dismiss();
        }
    }

    private void HideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = getCurrentFocus();
        if (view == null) {
            view = new View(getApplicationContext());
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private String GetDays(String sowingdate) throws ParseException {
        Date userDob = new SimpleDateFormat("dd/MM/yyyy").parse(sowingdate);
        Date today = new Date();
        long diff = today.getTime() - userDob.getTime();
        int numOfDays = (int) (diff / (1000 * 60 * 60 * 24));
        System.out.println("GetDays: " + numOfDays);
        return String.valueOf(numOfDays);
    }

    private void ClearMap() {
        mMap.clear();
    }

    private void LoadMarkers() {

        userPositionMarkerBitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.inaccurate_location_marker);
        for (int i = 0; i < totalLatLng.size(); i++) {

            MarkerOptions options = new MarkerOptions()
                    .position(totalLatLng.get(i))
                    .title(String.valueOf(totalLatLng.size()))
                    .icon(userPositionMarkerBitmapDescriptor);
            mMap.addMarker(options);

            if (i != 0) {
                addPolyline(totalLatLng.get(i - 1), totalLatLng.get(i), i - 1);
            }
        }
    }

    private void addPolyline(LatLng oldLatLon1, LatLng newLatLong1, int index) {
        if (index < totalMarker.size() - 1) {
            polyline = mMap.addPolyline(new PolylineOptions().add(oldLatLon1, newLatLong1)
                    .width(10)
                    .color(Color.GREEN));
            markerpolylines.add(index, polyline);
        }
    }
}