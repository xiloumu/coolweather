package com.coolweather.android;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.coolweather.android.gson.Forecast;
import com.coolweather.android.gson.Weather;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    private ScrollView weatherLayout;

    private TextView titleCity;

    private TextView titleUpdateTime;

    private TextView tempDegree;

    private TextView weatherInfo;

    private LinearLayout forecastLayout;

    private TextView aqiText;

    private TextView pm25Text;

    private TextView comfortText;

    private TextView carWashText;

    private TextView sportText;

    private ImageView bingPicImg;

    public SwipeRefreshLayout swipeRefresh;

    private String mWeatherId;

    public DrawerLayout drawerLayout;

    private Button navButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        if (Build.VERSION.SDK_INT >= 21) { //判断系统是否是Android 5.0 及以上
            View decorView = getWindow().getDecorView();//调用getWindow().getDecorView()方法拿到当前活动的DecorView
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            //改变系统 UI 的显示，表示活动的布局会显示在状态栏上面
            getWindow().setStatusBarColor(Color.TRANSPARENT);//将状态栏设置成透明色
        }

        //初始化各控件
        weatherLayout = (ScrollView) findViewById(R.id.weather_layout);
        titleCity = (TextView) findViewById(R.id.title_city);
        titleUpdateTime = (TextView) findViewById(R.id.title_update_time);
        tempDegree = (TextView) findViewById(R.id.temp_degree);
        weatherInfo = (TextView) findViewById(R.id.weather_info);
        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        aqiText = (TextView) findViewById(R.id.aqi_text);
        pm25Text = (TextView) findViewById(R.id.pm25_text);
        comfortText = (TextView) findViewById(R.id.comfort_text);
        carWashText = (TextView) findViewById(R.id.car_wasj_text);
        sportText = (TextView) findViewById(R.id.sport_text);
        bingPicImg = (ImageView) findViewById(R.id.bing_pic_img);
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navButton = (Button) findViewById(R.id.nav_button);

        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);//从SharedPreferences中读取数据
        String weatherString = prefs.getString("weather", null);
        if (weatherString != null) {
            //有缓存时直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
            mWeatherId = weather.basic.weatherId;
            showWeatherInfo(weather);
        } else {
            //无缓存时去服务器查询天气
            mWeatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(mWeatherId);
        }

        //判断必应图片信息是否有缓存
        String bingString = prefs.getString("bing_pic", null);
        if (bingString != null) {
            Glide.with(this).load(bingString).into(bingPicImg);
        } else {
            loadBingPic();
        }

        //天气信息下拉刷新
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(mWeatherId);
            }
        });

        //切换城市按钮的点击事件
        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }

    /**
     * 根据天气id 请求城市天气信息
     */
    public void requestWeather(final String weatherId) {

        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId +
                "&key=334b163f648a45b8a198764a454de309";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            //向接口地址发起请求，服务器会将相应城市的天气信息以JSON格式返回
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);//表示刷新事件结束，并隐藏刷新进度条
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);//将返回的JSON数据转换成Weather对象
                runOnUiThread(new Runnable() { //将当前线程切换到主线程
                    @Override
                    public void run() {
                        if ( weather != null && "ok".equals(weather.status)) {
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather", responseText);
                            editor.apply();
                            //将天气信息数据存储到SharedPreferences中
                            showWeatherInfo(weather);
                        } else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        }
                        swipeRefresh.setRefreshing(false);//表示刷新事件结束，并隐藏刷新进度条
                    }
                });
            }
        });

        loadBingPic();
    }

    /**
     * 加载必应每日一图
     */
    private void loadBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
                //将图片信息数据存储到SharedPreferences中
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                    }
                });
            }
        });
    }


    /**
     *
     */


    /**
     * 处理并展示Weather实体类中的数据
     */
    private void showWeatherInfo(Weather weather) {
        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.temperature + "℃";
        String weatherInfoText = weather.now.more.info;
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        tempDegree.setText(degree);
        weatherInfo.setText(weatherInfoText);
        forecastLayout.removeAllViews();
        for (Forecast forecast : weather.forecastList) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);
            TextView dateText = (TextView) view.findViewById(R.id.date_text);
            TextView info_text = (TextView) view.findViewById(R.id.info_text);
            TextView max_text = (TextView) view.findViewById(R.id.max_text);
            TextView min_text = (TextView) view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            info_text.setText(forecast.more.info);
            max_text.setText(forecast.temperature.max);
            min_text.setText(forecast.temperature.min);
            forecastLayout.addView(view);
         }
         if ( weather.aqi != null) {
             aqiText.setText(weather.aqi.city.aqi);
             pm25Text.setText(weather.aqi.city.pm25);
         }
        String comfort = "舒适度：" + weather.suggestion.comfort.info;
        String carWash = "洗车指数：" + weather.suggestion.cardWash.info;
        String sport = "运动建议：" + weather.suggestion.sport.info;
        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);

    }

}
