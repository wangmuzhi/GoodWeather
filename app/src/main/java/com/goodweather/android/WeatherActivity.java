package com.goodweather.android;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.goodweather.android.gson.Forecast;
import com.goodweather.android.gson.Weather;
import com.goodweather.android.util.HttpUtil;
import com.goodweather.android.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

	private ScrollView weatherLayout;

	private TextView titleCity;

	private TextView titleUpdate;

	private TextView degreeText;

	private TextView weatherInfoText;

	private LinearLayout forecastLayout;

	private TextView aqiText;

	private TextView pm25Text;

	private TextView comfortText;

	private TextView carWashText;

	private TextView sportText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_weather);

		weatherLayout = (ScrollView) findViewById(R.id.weather_layout);
		titleCity = (TextView) findViewById(R.id.title_city);
		titleUpdate = (TextView) findViewById(R.id.update_time);
		degreeText = (TextView) findViewById(R.id.degree_text);
		weatherInfoText = (TextView) findViewById(R.id.weather_info_text);
		forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
		aqiText = (TextView) findViewById(R.id.aqi_text);
		pm25Text = (TextView) findViewById(R.id.pm25_text);
		comfortText = (TextView) findViewById(R.id.comfort_text);
		carWashText = (TextView) findViewById(R.id.car_wash_text);
		sportText = (TextView) findViewById(R.id.sport_text);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String weatherString = prefs.getString("weather", null);
		if (weatherString != null) {
			Weather weather = Utility.handleWeatherResponse(weatherString);
			showWeatherInfo(weather);
		} else {
			String weatherId = getIntent().getStringExtra("weather_id");
			weatherLayout.setVisibility(View.INVISIBLE);
			requestWeather(weatherId);
		}

	}

	private void requestWeather(final String weatherId) {
		String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=bc0418b57b2d4918819d3974ac1285d9";
		HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {

			@Override
			public void onResponse(Call call, Response response) throws IOException {

				final String responseText = response.body().string();
				final Weather weather = Utility.handleWeatherResponse(responseText);
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (weather != null && "ok".equals(weather.status)) {
							SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
							editor.putString("weather", responseText);
							editor.apply();
							showWeatherInfo(weather);
						} else {
							Toast.makeText(WeatherActivity.this, "获取天气信息失败",Toast.LENGTH_SHORT).show();
						}
					}
				});

			}
			@Override
			public void onFailure(Call call, IOException e) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(WeatherActivity.this, "获取天气信息失败",Toast.LENGTH_SHORT).show();
					}
				});

			}


		});
	}

	private void showWeatherInfo(Weather weather) {
		String cityName = weather.basic.cityName;
		String updateTime = weather.basic.update.updateTime.split(" ")[1];
		String temperature = weather.now.temperature + "℃";
		String weatherInfo = weather.now.more.info;
		titleCity.setText(cityName);
		titleUpdate.setText(updateTime);
		degreeText.setText(temperature);
		weatherInfoText.setText(weatherInfo);
		forecastLayout.removeAllViews();
		for (Forecast forecast : weather.forecastList) {
			View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);
			TextView dataText = view.findViewById(R.id.date_text);
			TextView infoText = view.findViewById(R.id.info_text);
			TextView maxText = view.findViewById(R.id.max_text);
			TextView minText = view.findViewById(R.id.min_text);
			dataText.setText(forecast.date);
			infoText.setText(forecast.more.info);
			maxText.setText(forecast.temperature.max);
			minText.setText(forecast.temperature.min);
			forecastLayout.addView(view);
		}
		if (weather.aqi != null) {
			aqiText.setText(weather.aqi.city.aqi);
			pm25Text.setText(weather.aqi.city.pm25);
		}

		String comfort = "舒适度：" + weather.suggestion.comfort.info;
		String carWash = "洗车系数： " + weather.suggestion.carWash.info;
		String sport = "运动建议：" + weather.suggestion.sport.info;
		comfortText.setText(comfort);
		carWashText.setText(carWash);
		sportText.setText(sport);
		weatherLayout.setVisibility(View.VISIBLE);

	}
}
