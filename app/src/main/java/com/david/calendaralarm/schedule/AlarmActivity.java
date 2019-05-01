package com.david.calendaralarm.schedule;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import com.david.calendaralarm.R;
import com.david.calendaralarm.data.AlarmDAO;
import com.david.calendaralarm.utils.AlarmContentUtils;
import com.david.calendaralarm.utils.Const;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Display this page when the alarm clock rings
 */
public class AlarmActivity extends AppCompatActivity {

    private AlarmDAO alarmDAO;
    private AlarmController alarmController;
    private long ringDurationMs;

    protected TextView currentHourTextView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupWindowFlags();
        setAppTheme();

        setContentView(R.layout.activity_alarm);

        String url = "http://dataservice.accuweather.com/currentconditions/v1/3497808/?apikey=mSrRYbkQmojoxxqCj81hcC4L3uiGwOwj";
        AsyncHttpClient httpClient = new AsyncHttpClient();
        httpClient.get(url, null, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
            }

            @Override
            public void onSuccess(int statusCode, String content) {
                if(statusCode==200)
                {
                    try {
                        // return json data according to the interface to display the weather, temperature
                        JSONArray data = new JSONArray(content);
                        JSONObject info = data.getJSONObject(0);
                        String weatherText = info.getString("WeatherText");
                        TextView tv1 = findViewById(R.id.weather1);
                        tv1.setText(weatherText);
                        int tem = info.getJSONObject("Temperature").getJSONObject("Imperial").getInt("Value");
                        TextView tv2 = findViewById(R.id.weather2);
                        tv2.setText(tem + "°F");
                        TextView tv0 = findViewById(R.id.weathericon);
                        int weatherIcon = info.getInt("WeatherIcon");
                        switch (weatherIcon){
                            case 1:
                            case 2:
                                tv0.setBackground(getResources().getDrawable(R.drawable.sunny));
                                break;
                            case 3:
                                tv0.setBackground(getResources().getDrawable(R.drawable.partly_cloudy));
                                break;
                            case 4:
                            case 5:
                                tv0.setBackground(getResources().getDrawable(R.drawable.cloudy_s_sunny));
                                break;
                            case 6:
                            case 7:
                            case 8:
                            case 33:
                            case 34:
                            case 35:
                            case 36:
                            case 37:
                            case 38:
                                tv0.setBackground(getResources().getDrawable(R.drawable.cloudy));
                                break;
                            case 11:
                            case 25:
                                tv0.setBackground(getResources().getDrawable(R.drawable.fog));
                                break;
                            case 12:
                            case 18:
                                tv0.setBackground(getResources().getDrawable(R.drawable.rain));
                                break;
                            case 13:
                            case 14:
                            case 16:
                            case 17:
                                tv0.setBackground(getResources().getDrawable(R.drawable.sunny_s_rain));
                                break;
                            case 15:
                            case 41:
                            case 42:
                                tv0.setBackground(getResources().getDrawable(R.drawable.thunderstorms));
                                break;
                            case 19:
                                tv0.setBackground(getResources().getDrawable(R.drawable.snow_light));
                                break;
                            case 20:
                            case 21:
                            case 43:
                            case 44:
                                tv0.setBackground(getResources().getDrawable(R.drawable.sleet));
                                break;
                            case 22:
                            case 23:
                                tv0.setBackground(getResources().getDrawable(R.drawable.snow));
                                break;
                            case 26:
                            case 29:
                                tv0.setBackground(getResources().getDrawable(R.drawable.snow_s_rain));
                                break;
                            case 32:
                                tv0.setBackground(getResources().getDrawable(R.drawable.windy));
                                break;
                            case 39:
                            case 40:
                                tv0.setBackground(getResources().getDrawable(R.drawable.rain_s_cloudy));
                                break;
                            default:
                                tv0.setBackground(getResources().getDrawable(R.drawable.cloudy));
                                break;
                        }
                    }catch (Exception ex){
                    }
                }
            }

        });


        currentHourTextView = findViewById(R.id.activityAlarmCurrentHour);
        findViewById(R.id.activityAlarmLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        alarmDAO = new AlarmDAO();
        alarmController = new AlarmController(getApplicationContext());

//        removeExecutedAlarmFromDatabase();
        showCurrentHour();
        setupRingDuration();
        countDownRingDuration();

    }

    /**
     * Setting full screen of mobile phone
     */
    private void setupWindowFlags() {
        if (Build.VERSION.SDK_INT >= 27) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /**
     * Setting mobile theme
     */
    private void setAppTheme() {
        int themeId = AppCompatDelegate.MODE_NIGHT_NO;
        getDelegate().setLocalNightMode(themeId);
    }

    /**
     * Set the current time
     */
    private void showCurrentHour() {
        String currentHour = AlarmContentUtils.getTitle(DateTime.now());
        currentHourTextView.setText(currentHour);
    }

    /**
     * Get the alarm clock pause time interval
     */
    private void setupRingDuration() {
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        String ringDurationString = preferences.getString(getString(R.string.key_ring_duration),
                String.valueOf(Const.DEFAULTS.RING_DURATION_MS));
        ringDurationMs = Long.parseLong(ringDurationString);
    }

    /**
     * Close this page in a certain time
     */
    private void countDownRingDuration() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, ringDurationMs);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void onUserLeaveHint() {
        super.onUserLeaveHint();
        finish();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        alarmController.dismissCurrentlyPlayingAlarm();
        alarmDAO.cleanUp();
    }
}
