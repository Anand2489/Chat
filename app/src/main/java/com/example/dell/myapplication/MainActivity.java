package com.example.dell.myapplication;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.content.DialogInterface;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.internal.http.RequestHeaders;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;


public class MainActivity extends ActionBarActivity {

    TextView editText_user_name;
    TextView editText_email;
    Button button_login;


    static final String TAG = "pavan";

    TextView mDisplay;
    GoogleCloudMessaging gcm;
    AtomicInteger msgId = new AtomicInteger();
    SharedPreferences prefs;
    Context context1;
    Context context;
    String regid;
    String msg;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context1 = this;
        context = getApplicationContext();


        String user_name = getUserName(context);
        if (!user_name.isEmpty()) {

            Intent chatActivity = new Intent(MainActivity.this, ChatActivity.class);
            chatActivity.putExtra("user_id", user_name);
            startActivity(chatActivity);

            finish();

        } else {
            editText_user_name = (TextView) findViewById(R.id.editText_user_name);
            editText_email = (TextView) findViewById(R.id.editText_email);
            button_login = (Button) findViewById(R.id.button_login);

            button_login.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    sendRegistrationIdToBackend();

                }
            });


            // Check device for Play Services APK. If check succeeds, proceed with
            //  GCM registration.
          //  Log.d("pavan", "in oncreate");

          //  Toast.makeText(context, "in oncreate", Toast.LENGTH_LONG).show();
            if (checkPlayServices()) {
                gcm = GoogleCloudMessaging.getInstance(this);
                regid = getRegistrationId(context);

                if (regid.isEmpty()) {
                    registerInBackground();
                }


            } else {
                Log.i("pavan", "No valid Google Play Services APK found.");
            }

        }
    }

    // You need to do the Play Services APK check here too.
    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        Util.PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(Util.PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing registration ID is not guaranteed to work with
        // the new app version.
        int registeredVersion = prefs.getInt(Util.PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }


    private String getUserName(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String User_name = prefs.getString(Util.USER_NAME, "");
        Log.d("pavan", "username in main " + User_name);
        return User_name;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * Substitute you own sender ID here. This is the project number you got
     * from the API Console, as described in "Getting Started."
     */


    private void registerInBackground() {
        new AsyncTask() {

            @Override
            protected String doInBackground(Object[] params) {


                try {

                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(MainActivity.this);
                    }
                    regid = gcm.register(Util.SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;


                    // You should send the registration ID to your server over HTTP,
                    //GoogleCloudMessaging gcm;/ so it can use GCM/HTTP or CCS to send messages to your app.
                    // The request to your server should be authenticated if your app
                    // is using accounts.
                     sendRegistrationIdToBackend();

                    // For this demo: we don't need to send it because the device
                    // will send upstream messages to a server that echo back the
                    // message using the 'from' address in the message.

                    // Persist the registration ID - no need to register again.
                    storeRegistrationId(context, regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;


            }
        }.execute();

    }


    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Util.PROPERTY_REG_ID, regId);
        editor.putInt(Util.PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    private void storeUserDetails(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Util.EMAIL, editText_email.getText().toString());
        editor.putString(Util.USER_NAME, editText_user_name.getText().toString());
        editor.commit();
    }

    private SharedPreferences getGCMPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the registration ID in your app is up to you.
        return getSharedPreferences(MainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }

    //   LOOK MORE ON THIS

    private RequestQueue mRequestQueue;

    private void sendRegistrationIdToBackend() {
        // Your implementation here.

        mRequestQueue = Volley.newRequestQueue(MainActivity.this);
        Log.d("pavan", "gcm id " + regid);
        // Toast.makeText(context,"in volley "+regid,Toast.LENGTH_LONG).show();


        new smppLogin().execute();

// Access the RequestQueue through your singleton class.
        // AppController.getInstance().addToRequestQueue(jsObjRequest, "jsonRequest");

    }

//
//    private class SendGcmToServer extends AsyncTask<String, Void, String> {
//
//        @Override
//        protected void onPreExecute() {
//            // TODO Auto-generated method stub
//            super.onPreExecute();
//
//
//        }
//
//        @Override
//        protected String doInBackground(String... params) {
//            // TODO Auto-generated method stub
//
//            String url = Util.register_url + "?name=" + editText_user_name.getText().toString() + "&email=" + editText_email.getText().toString() + "&regId=" + regid;
//            Log.i("pavan", "url" + url);
//
//            OkHttpClient client_for_getMyFriends = new OkHttpClient();
//            ;
//
//            String response = null;
//            // String response=Utility.callhttpRequest(url);
//
//            try {
//                url = url.replace(" ", "%20");
//                response = callOkHttpRequest(new URL(url),
//                        client_for_getMyFriends);
//                Log.d("pavan", "response " + response);
//                for (String subString : response.split("<script", 2)) {
//                    response = subString;
//                    break;
//                }
//            } catch (MalformedURLException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            } catch (IOException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//
//
//            return response;
//        }
//
//        @Override
//        protected void onPostExecute(String result) {
//            // TODO Auto-generated method stub
//            super.onPostExecute(result);
//            //Toast.makeText(context,"response "+result,Toast.LENGTH_LONG).show();
//
//            if (result != null) {
//                if (result.equals("success")) {
//
//                    storeUserDetails(context);
//                    startActivity(new Intent(MainActivity.this, ChatActivity.class));
//                    finish();
//
//                } else {
//
//                    Toast.makeText(context, "Try Again" + result, Toast.LENGTH_LONG).show();
//                }
//
//
//            } else {
//
//                Toast.makeText(context, "Check net connection ", Toast.LENGTH_LONG).show();
//            }
//
//        }
//
//
//    }


    // Http request using OkHttpClient
    String callOkHttpRequest(URL url, OkHttpClient tempClient)
            throws IOException, JSONException {
        boolean userExist = checkForUser(url, tempClient);
        if (userExist == false) {
            HttpURLConnection connection = tempClient.open(url);
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", Util.XMPP_SECREAT_KEY);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setConnectTimeout(4000);
            JSONObject userD = new JSONObject();
            try {
//            userD.put("username","test3");
                userD.put("username", editText_user_name.getText().toString());
                userD.put("password", Util.XMPP_PASSWORD);
                userD.put("email", "gfashbas");
            } catch (JSONException e) {
                System.out.println("Json error");
            }
            OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
            out.write(userD.toString());
            out.close();
            InputStream in = null;
            try {
                // Read the response.
                in = connection.getInputStream();
                byte[] response = readFully(in);
                return new String(response, "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            } finally {
                if (in != null)
                    in.close();
            }
        } else
            return "userExist";
    }

    byte[] readFully(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        for (int count; (count = in.read(buffer)) != -1; ) {
            out.write(buffer, 0, count);
        }
        return out.toByteArray();
    }

    Boolean checkForUser(URL url, OkHttpClient tempClient)
            throws IOException {
        url = new URL(url.toString() + "/" + editText_user_name.getText().toString());
        HttpURLConnection connection = tempClient.open(url);
        connection.setUseCaches(false);
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", Util.XMPP_SECREAT_KEY);
        //  connection.setRequestProperty("Content-Type", "application/json");
        // connection.setConnectTimeout(4000);
        int response = connection.getResponseCode();
        if (response == HttpURLConnection.HTTP_OK)
            return true;
        else
            return false;
    }

    private class smppLogin extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();


        }

        @Override
        protected String doInBackground(String... arg0) {

//            String VerifyUserURL = "http://" + Util.SERVER + ":9090/plugins/userService/userservice?type=add&secret=" + Util.XMPP_SECREAT_KEY + "&"
//                    + "username="
//                    + editText_user_name.getText().toString()
//                    + "&password=" + Util.XMPP_PASSWORD + "&name="
//                    + editText_user_name.getText().toString()
//                    + "&password" +
//                    "="
//                    + editText_email.getText().toString();
            String VerifyUserURL = "http://" + Util.SERVER + ":9090/plugins/restapi/v1/users";
            VerifyUserURL = VerifyUserURL.replace(" ", "%20");


            OkHttpClient client_send_code = new OkHttpClient();
            String response = null;
            try {
                response = callOkHttpRequest(new URL(VerifyUserURL), client_send_code);
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            if (response != null && response != "userExist") {
                return response;
            } else if (response == "userExist") {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context1);
                        alertDialogBuilder.setTitle("ALERT");
                        alertDialogBuilder.setMessage("User already exist")
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                    }
                });

            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                    }

                });
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {


            Log.d("pavan", "server said: " + result);

            if (result != null) {

                storeUserDetails(context);
                Intent chatActivity = new Intent(MainActivity.this, ChatActivity.class);
                chatActivity.putExtra("user_id", editText_user_name.getText().toString());
                startActivity(chatActivity);

            } else {

                Toast.makeText(MainActivity.this, "error", Toast.LENGTH_LONG).show();
            }

        }

    }


}
