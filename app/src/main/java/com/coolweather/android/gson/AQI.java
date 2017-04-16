package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by chen on 2017/4/15.
 */

public class AQI {

    public AQICity city;

    public class AQICity {

        @SerializedName("aqi")
        public String aqi;

        @SerializedName("pm25")
        public String pm25;
    }
}
