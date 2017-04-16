package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by chen on 2017/4/15.
 */

public class Suggestion {

    @SerializedName("comf")
    public Comfort comfort;

    @SerializedName("cw")
    public CardWash cardWash;

    @SerializedName("sport")
    public Sport sport;

    public class Comfort {

        @SerializedName("txt")
        public String info;
    }

    public class CardWash {

        @SerializedName("txt")
        public String info;
    }

    public class Sport {

        @SerializedName("txt")
        public String info;
    }
}
