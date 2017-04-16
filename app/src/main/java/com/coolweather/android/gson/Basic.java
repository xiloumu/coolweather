package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by chen on 2017/4/15.
 */

public class Basic {
    @SerializedName("city")  //@SerializedName注解的方式来让JSON字段和Java字段之间建立映射关系
    public String cityName;

    @SerializedName("id")
    public String weatherId;

    public Update update;

    public class Update {
        @SerializedName("loc")
        public String updateTime;
    }
}
