/*
 * Satyukt Analytics Pvt. Ltd. | Copyright (c) 2022-2023.
 * Created by chetan on 20/01/23, 2:54 PM
 * Last modified 29/11/22, 10:56 AM
 */

package com.example.drawandwalk.uiSupport;

import android.os.Build;
import android.view.View;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class DisableNavigation {

    public DisableNavigation(View decorView) {
        /*System.out.println("***DisableNavigation***");*/
        System.out.println("Build.VERSION.SDK_INT: " + Build.VERSION.SDK_INT);
        //for new api versions.
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
    }

    public boolean exists(URL url) throws IOException {
        HttpURLConnection huc = (HttpURLConnection) url.openConnection();
        huc.setRequestMethod("GET");  //OR  huc.setRequestMethod ("HEAD");
        huc.connect();
        int code = huc.getResponseCode();
        System.out.println(code);

        return code == 200;
    }

}
