package com.example.emergencyapplication;

import static android.Manifest.permission.CALL_PHONE;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final Location TODO = null;
    Button b1, b2, danger, accident, hindi, english;
    TextView tv_restAppBar;
    private FusedLocationProviderClient client;
    DatabaseHandler myDB;
    private static final int REQUEST_CHECK_CODE = 8989;
    private LocationSettingsRequest.Builder builder;
    String x = "", y = "";
    private static final int REQUEST_LOCATION = 1;

    LocationManager locationManager;
    Intent mIntent;

    LocationManager mLocationManager;

    public static int i = 0;

    ArrayList<String> hindiList = new ArrayList<>();
    ArrayList<String> englishList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        b1 = findViewById(R.id.button);
        b2 = findViewById(R.id.button2);
        myDB = new DatabaseHandler(this);
        hindi = findViewById(R.id.hindi);
        english = findViewById(R.id.english);
        tv_restAppBar = findViewById(R.id.tv_restAppBar);

        hindiList.add("संपर्क जोड़ें");
        hindiList.add("आपातकालीन चोरी");
        hindiList.add("आपातकालीन खतरा");
        hindiList.add("आपातकालीन दुर्घटना");
        hindiList.add("महिला सुरक्षा");

        englishList.add("Add Contact");
        englishList.add("Emergency Theft");
        englishList.add("Emergency Danger");
        englishList.add("Emergency Accident");
        englishList.add("Women Safety");

        hindi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLanguage(MainActivity.this, hindiList);
            }
        });

        english.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLanguage(MainActivity.this, englishList);
            }
        });

        danger = findViewById(R.id.danger);
        accident = findViewById(R.id.accident);

        final MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.mixkit_retro_emergency_notification_alarm_2970);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            onGPS();
        } else {
            startTrack();
        }

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), Register.class);
                startActivity(i);
            }
        });
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                theft();
            }
        });

        danger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                danger();
            }
        });

        accident.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                accident();
            }
        });
    }

    private void accident() {
        loadData(101);
    }

    private void danger() {
        loadData(100);
    }

    private void theft() {
        loadData(100);
    }

    private void loadData(int call) {
        ArrayList<String> thelist = new ArrayList<>();
        Cursor data = myDB.getListContents();
        if (data.getCount() == 0) {
            Toast.makeText(MainActivity.this, "no content to show", Toast.LENGTH_SHORT).show();
        } else {
            String msg = "I need Help Latitude : " + x + " Longitude : " + y;
            String number = "";
            while (data.moveToNext()) {
                thelist.add(data.getString(1));
                number = number + data.getString(1) + (data.isLast() ? "" : ";");
                call(call);
            }
            if (!thelist.isEmpty()) {
                sendSMS(number, msg, true);
            }
        }
    }

    private void sendSMS(String number, String msg, boolean b) {

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);

        SmsManager sms = SmsManager.getDefault();
        for (String s : loadContact()) {
            sms.sendTextMessage(s, null, msg, pi, null);
        }

    }

    private void call(int call) {
        Intent i = new Intent(Intent.ACTION_CALL);
        i.setData(Uri.parse("tel:" + call));
        if (ContextCompat.checkSelfPermission(getApplicationContext(), CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            startActivity(i);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{CALL_PHONE}, 1);
            }
        }
    }

    public void setLanguage(Activity activity, ArrayList<String> language) {
        b1.setText(language.get(0));
        b2.setText(language.get(1));
        danger.setText(language.get(2));
        accident.setText(language.get(3));
        tv_restAppBar.setText(language.get(4));
    }

    private void startTrack() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        } else {
            FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location locationGPS) {
                            // GPS location can be null if GPS is switched off
                            if (locationGPS != null) {
                                double lat = locationGPS.getLatitude();
                                double lon = locationGPS.getLongitude();
                                x = String.valueOf(lat);
                                y = String.valueOf(lon);
                            } else {
                                Toast.makeText(MainActivity.this, "UNABLE TO FIND LOCATION", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("MapDemoActivity", "Error trying to get last GPS location");
                            e.printStackTrace();
                        }
                    });
        }
    }

    private void onGPS() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Enable GPS").setCancelable(false).setPositiveButton("yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    private ArrayList<String> loadContact() {
        ArrayList<String> theList = new ArrayList<>();
        Cursor data = myDB.getListContents();
        if (data.getCount() == 0) {
            Toast.makeText(MainActivity.this, "There is no content", Toast.LENGTH_SHORT).show();
        } else {
            while (data.moveToNext()) {
                theList.add(data.getString(1));
            }
        }
        return theList;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_POWER) {
            i++;
            if (i == 2) {
                //do something
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + "102301"));
                startActivity(intent);
                i = 0;
            }

        }
        return super.onKeyDown(keyCode, event);
    }

}