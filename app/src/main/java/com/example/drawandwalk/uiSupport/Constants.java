package com.example.drawandwalk.uiSupport;

public class Constants {

    /**
     * Plot Types
     */
    public static final String FARM = "farm";
    public static final String TANK = "tank";
    public static final String POLYHOUSE = "`polyhouse`";
    public static final String GARDEN = "garden";


    /**
     * location services
     */
    public static final String ACTION_START_LOCATION_SERVICE = "startLocationService";
    public static final String ACTION_STOP_LOCATION_SERVICE = "stopLocationService";


    /**
     * Provider to access photos
     */
    public static final String AUTHORITY = "com.satyukt.myfarmapp.provider";


    /**
     * total no of images to show in image advisory
     */
    public static final int TOTAL_IMG_TO_SHOW = 50;


    /**
     * one day in miliseconds
     */
    final public static long ONE_DAY_IN_MILLISECONDS = 1000 * 60 * 60 * 24;


    /**
     * 1000000 sqmtr = 100 Hectare.... give value in sqmtr      //changed to 250Hectare 10-3-23
     * Max area of farm,polyhouse  while adding
     * allowed 100 Hectare only
     * 1Hectare = 10000 sqmtr
     */
    final public static double MAX_AREA_OF_FARM = 2500000;


    /**
     * 1000000 sqmtr = 100 Hectare.... give value in sqmtr
     * Max area of Tank while adding
     * allowed 200 Hectare only
     */
    final public static double MAX_AREA_OF_TANK = 10000000;


    /**
     * Distance between two coordinates while adding farm in Walk in meter
     */
    final public static int DISTANCE_BETWEEN_COOD = 2;


    /**
     * Area conversions
     */
    public static final double ONE_SQMTR_TO_ACRE = 0.00024710538146717;
    public static final double ONE_HECTARE_TO_SQMTR = 10000;
    public static final double ONE_HECTARE_TO_PUCCHABHIGA = 3.953686105;
    public static final double ONE_HECTARE_TO_KACCHABHIGA = 11.86105832;
    public static final double ONE_SQMTR_TO_GUNTHA = 0.009884205693296602;
    public static final double ONE_SQMTR_TO_SQUAREYARD = 1.19599005;
    public static final String INRSymbol = "₹";


    /**
     * firebase bucket for audio
     */
    public static String newBucket = "gs://sat2farm2.appspot.com/";
}
