package com.example.nikhileshwar.savems;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Debug;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {


    Button sendButton;
    Button saveButton;
    public static EditText recepientText,msgText;
    EditText number;
    public static  SharedPreferences sharedPreferences;
    public static String defaultNumber;
    public static FusedLocationProviderClient client;
    public static Geocoder geocoder;
    public static boolean sending ;
    public static int turn;
    public static LocationManager locationManager;
    public static LocationListener locationListener;
    public static LocationCallback locationCallback;
    String message;
    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //locationManager=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
        sharedPreferences= getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        turn=sharedPreferences.getInt("turn",0);

        defaultNumber=sharedPreferences.getString("defaultNumber",null);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        client= LocationServices.getFusedLocationProviderClient(this);
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)!= PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)!= PackageManager.PERMISSION_GRANTED  ||
                ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED
                )
        {

            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.SEND_SMS,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.ACCESS_FINE_LOCATION},1);
        }

        if(turn==0)
        {
            message="Hi User,thanks for downloading SaveMS.After you set a valid mobile number in the DEFAULT NUMBER," +
                    "starting from the next time you open the app,the app starts sending your location(every 5 seconds) to the default number every alternate time you open the app" +
                    "(NOTE:during this time the app seems to close itself once you tap on it just to make sure no one knows that you are sending locations to your friend) ." +
                    " In between these turns you can choose to change the default number(NOTE:during this time the app opens itself when you tap on it and you can choose to change the default " +
                    "number and this cycle continues from the next time you open the app.You can manually stop sending the locations by clicking on the STOP button during this time). And finally," +
                    "Be safe :)";
            appInfoToUsers(message);
        }
        if(turn==1  || turn==0)
        {
            editor.putInt("turn", 2);

        }
        else
        {
            editor.putInt("turn",1);


        }
        Log.i("value :",Integer.toString(turn));
        editor.commit();

        geocoder=new Geocoder(getApplicationContext(), Locale.getDefault());



        recepientText= (EditText) findViewById(R.id.recepient);
        msgText=(EditText)findViewById(R.id.message);
        number=(EditText)findViewById(R.id.number);

        if(defaultNumber!=null &&turn==1)
        {
            sending=true;
            locationManager=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
            client= LocationServices.getFusedLocationProviderClient(this);

            // onDefaultNumberSaved(saveButton);
            startService(new Intent(this,BackgroundOperation.class));
            finish();
        }

    }
  private void appInfoToUsers(String message)
  {
      AlertDialog.Builder alertBox=new AlertDialog.Builder(this);
      alertBox.setTitle("welcome to SaveMS :)").setMessage(message);
      alertBox.setPositiveButton("OK", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialogInterface, int i) {
              Log.i("status","accepted");

          }
      });

      AlertDialog dialog=alertBox.create();
      dialog.show();
  }
    public void stopService(View view)
    {
            stopService(new Intent(this,BackgroundOperation.class));
         //   finish();
    }


    @SuppressLint("MissingPermission")
    public  void sendData(View view)
    {
Long min=Long.parseLong("7000000000");
            try {
                if (recepientText.getText().toString().length() == 10 && Long.parseLong(recepientText.getText().toString())>=min) {

//trying to validate number
                    String SENT = "SMS_SENT";

                    final PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,
                            new Intent(SENT), 0);
                    registerReceiver(new BroadcastReceiver(){


                        @Override
                        public void onReceive(Context arg0, Intent arg1) {
                            switch (getResultCode())
                            {
                                case Activity.RESULT_OK:
                                    Toast.makeText(getBaseContext(), "SMS sent",
                                            Toast.LENGTH_SHORT).show();
                                    break;
                                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                                    Toast.makeText(getBaseContext(), "enter a valid recepient mobile", Toast.LENGTH_LONG).show();

                                    break;
                                case SmsManager.RESULT_ERROR_NO_SERVICE:
                                    Toast.makeText(getBaseContext(), "no service", Toast.LENGTH_LONG).show();

                                    break;
                                case SmsManager.RESULT_ERROR_NULL_PDU:
                                    Toast.makeText(getBaseContext(), "enter a valid recepient mobile", Toast.LENGTH_LONG).show();

                                    break;
                                case SmsManager.RESULT_ERROR_RADIO_OFF:
                                    Toast.makeText(getBaseContext(), "enter a valid recepient mobile", Toast.LENGTH_LONG).show();

                                    break;
                            }
                        }

                    }, new IntentFilter(SENT));

                    client.getLastLocation().addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                SmsManager sms = SmsManager.getDefault();
                                try {
                                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                    if (addresses != null && addresses.size() > 0) {
                                        sms.sendTextMessage(recepientText.getText().toString(), null,
                                                        msgText.getText().toString(),
                                                sentPI, null);

                                    }

                                } catch (IOException e) {
                                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                }


                            }
                        }

                    });

                    //Toast.makeText(this, "success", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "enter a valid recepient mobile", Toast.LENGTH_LONG).show();

                }

            } catch (Exception e) {
                Toast.makeText(this, "failed to send", Toast.LENGTH_LONG).show();

            }
        }


    @SuppressLint({"MissingPermission", "RestrictedApi"})
    public void onDefaultNumberSaved(View view)
    {
        try
        {//&& number.getText().toString().length() == 10   //change condition
            Long min=Long.parseLong("7000000000");

            if (number.getText().toString() != null  &&  number.getText().toString().length() == 10 &&Long.parseLong(number.getText().toString().toString())>=min)
            {
                    alertUser();



            }
            else
            {
                Toast.makeText(this, "enter a valid default number!!", Toast.LENGTH_LONG).show();

            }
//            else if (defaultNumber != null)
//            {
//
//
//                client.requestLocationUpdates(new LocationRequest().setInterval(10000).setFastestInterval(5000),new LocationCallback(){
//                @Override
//                public void onLocationResult(LocationResult locationResult)
//                {
//                    super.onLocationResult(locationResult);
//                    SmsManager sms = SmsManager.getDefault();
//
//                    try {
//                        List<Address>addresses = geocoder.getFromLocation(locationResult.getLastLocation().getLatitude(),locationResult.getLastLocation().getLongitude(),1);
//                        if(addresses!=null && addresses.size()>0) {
//                            sms.sendTextMessage(defaultNumber, null,
//                                    "lat : " + locationResult.getLastLocation().getLatitude() + "\nlon : " + locationResult.getLastLocation().getLongitude() + "\n\naddress : "+
//                                            addresses.get(0).getAddressLine(0) + "\n\nmessage : " +
//                                            msgText.getText().toString(),
//                                    null, null);
//                            Toast.makeText(MainActivity.this, "sent to "+defaultNumber, Toast.LENGTH_SHORT).show();
//                        }
//                        }
//                    catch (IOException e)
//                    {
//                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//
//                }
//            }, Looper.myLooper());
//
//
//
//            }

        }
        catch (Exception e)
        {
            Toast.makeText(this, "failed to send", Toast.LENGTH_LONG).show();

        }

        }

        private void alertUser()
        {

            //trying to validate number
            String SENT = "SMS_SENT";

            final PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,
                    new Intent(SENT), 0);
            registerReceiver(new BroadcastReceiver(){


                @Override
                public void onReceive(Context arg0, Intent arg1) {
                    switch (getResultCode())
                    {
                        case Activity.RESULT_OK:
                            Toast.makeText(getBaseContext(), "SMS sent",
                                    Toast.LENGTH_SHORT).show();
                            Log.i("status","accepted");
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("defaultNumber", number.getText().toString());  // Saving string
                            editor.commit();
                            defaultNumber = sharedPreferences.getString("defaultNumber", null);
                            // SharedPreferences.Editor editor = sharedPreferences.edit();
                            sending=true;
                            if(turn==1 || turn==0)
                            {

                                editor.putInt("turn",1);  // Saving string
                                editor.commit();

                            }

                            break;
                        case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                            Toast.makeText(getBaseContext(), "enter a valid default mobile", Toast.LENGTH_LONG).show();

                            break;
                        case SmsManager.RESULT_ERROR_NO_SERVICE:
                            Toast.makeText(getBaseContext(), "no service", Toast.LENGTH_LONG).show();

                            break;
                        case SmsManager.RESULT_ERROR_NULL_PDU:
                            Toast.makeText(getBaseContext(), "enter a valid default mobile", Toast.LENGTH_LONG).show();

                            break;
                        case SmsManager.RESULT_ERROR_RADIO_OFF:
                            Toast.makeText(getBaseContext(), "enter a valid default mobile", Toast.LENGTH_LONG).show();

                            break;
                    }
                }

            }, new IntentFilter(SENT));
            AlertDialog.Builder alertBox=new AlertDialog.Builder(this);
            alertBox.setTitle("sure about this??").setMessage("Your location will be forwarded to "+number.getText().toString()+" only. Choose carefully!!!. " +
                    "If accepted the given number  will receive a test message from SaveMS..");
            alertBox.setPositiveButton("Im sure", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {



                    SmsManager sms = SmsManager.getDefault();
                    sms.sendTextMessage(number.getText().toString(), null, "Hi from SaveMS,Be safe :)", sentPI, null);

                }
            });
            alertBox.setNegativeButton("choose again", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Log.i("status","choosing again");

                }
            });
            AlertDialog dialog=alertBox.create();
            dialog.show();
        }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length>2 && grantResults[0]==PackageManager.PERMISSION_GRANTED &&
                grantResults[1]==PackageManager.PERMISSION_GRANTED &&
                grantResults[2] == PackageManager.PERMISSION_GRANTED
                )
        {
           //do nothing here

        }
        else
            finish();
    }
}
