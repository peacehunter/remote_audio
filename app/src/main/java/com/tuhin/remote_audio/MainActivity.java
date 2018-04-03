package com.tuhin.remote_audio;

import android.app.ActivityManager;
import android.app.Dialog;
import android.app.IntentService;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;



public class MainActivity extends AppCompatActivity {
    AnalyticsApplication application;
    Tracker mTracker;
    InterstitialAd mInterstitialAd;
    TextView textView;
    ArrayList<HashMap<String, String>> list;
    static ArrayList<HashMap<String, String>> songsList;
    AssetManager assetManager;

    Boolean isWifiConnected = false;

    Button startServer;
    Button stopServer;
    public static Context baseContext;

    Boolean isRequestGranted = false;
    static String ip;

    public class WIFIBroadcastReceiver extends BroadcastReceiver {

        public WIFIBroadcastReceiver() {

        }

        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

            NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);

            isWifiConnected = networkInfo.isConnected();


            if (wifi.isConnected()) {
                final WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
                ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
                textView.setText(ip + ":8080");
                isWifiConnected = true;

                //  Intent intent2 = new Intent(getApplicationContext(), ServerService.class);

                //  startService(intent2);


                //  myServer.createServerRunnable(10000);
                // myServer.start(10000);


            } else {
                textView.setText("Not connected to a WIFI network . Please connect to a WIFI network first");
                isWifiConnected = false;
                startServer.setVisibility(View.INVISIBLE);
                stopServer.setVisibility(View.INVISIBLE);

                TextView ip = (TextView) findViewById(R.id.ip);
                ip.setVisibility(View.INVISIBLE);
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 458: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.


                    isRequestGranted = true;

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                    isRequestGranted = false;
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("SEE_YOUR_LOGCAT_TO_GET_YOUR_DEVICE_ID")
                .build();

        mInterstitialAd.loadAd(adRequest);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.text);

        final WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
        textView.setText(ip + ":8080");
        startServer = (Button) findViewById(R.id.Start);
        stopServer = (Button) findViewById(R.id.Stop);

        final LinearLayout linearLayout = (LinearLayout) findViewById(R.id.splashScreen);
        final LinearLayout mainLayout = (LinearLayout) findViewById(R.id.mainLayout);
        final Intent intent2 = new Intent(getApplicationContext(), ServerService.class);
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-7069979473754845/2606869019");


        final EditText editTextConf = new EditText(getApplicationContext());

        editTextConf.setHint("Confirm password");

        baseContext = getBaseContext();


        application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();
        mTracker.enableAutoActivityTracking(true);


        mTracker.send(new HitBuilders.ScreenViewBuilder().build());


        startServer.setVisibility(View.INVISIBLE);
        stopServer.setVisibility(View.INVISIBLE);


        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {

            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                // mInterstitialAd.show();
            }
        });

        requestNewInterstitial();


        new CountDownTimer(2000, 10) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {

                linearLayout.setVisibility(View.INVISIBLE);
                mainLayout.setVisibility(View.VISIBLE);

                requestNewInterstitial();

            }
        }.start();

        String[] permissions = {"android.permission.ACCESS_WIFI_STATE",
                "android.permission.INTERNET",
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.ACCESS_NETWORK_STATE"
        };

        try {
            requestPermissions(permissions, PackageManager.PERMISSION_GRANTED);
        } catch (NoSuchMethodError e) {

        }

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


        TextView scroll = (TextView) findViewById(R.id.scroll);

        scroll.setMovementMethod(new ScrollingMovementMethod());
        //   Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        //  setSupportActionBar(myToolbar);


        // if (isRequestGranted) {

        WIFIBroadcastReceiver wifiBroadcastReceiver = new WIFIBroadcastReceiver();


        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        intentFilter.addAction("android.net.wifi.STATE_CHANGE");

        registerReceiver(wifiBroadcastReceiver, intentFilter);


        if (!ServerService.isMyServiceRunning()) {
            startService(intent2);
        }

        startService(intent2);


        startServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startServer.setVisibility(View.INVISIBLE);
                stopServer.setVisibility(View.VISIBLE);
                textView.setText(ip + ":8080");
                Intent intent2 = new Intent(getApplicationContext(), ServerService.class);

                startService(intent2);
                // ServerService.startServer();
                // startActivity(intent2);


            }
        });





        stopServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startServer.setVisibility(View.VISIBLE);
                stopServer.setVisibility(View.INVISIBLE);
                Intent intent2 = new Intent(getApplicationContext(), ServerService.class);
                //ServerService.stopServer();
                stopService(intent2);


            }
        });


        Timer timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {


                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {


                        Log.d("server alive", String.valueOf(ServerService.isMyServiceRunning()));

                        Log.d("layout show", String.valueOf(linearLayout.isShown()));
                        if (!linearLayout.isShown()) {
                            if (isWifiConnected) {
                                try {
                                    if (ServerService.isMyServiceRunning()) {
                                        startServer.setVisibility(View.INVISIBLE);
                                        stopServer.setVisibility(View.VISIBLE);
                                    } else {
                                        startServer.setVisibility(View.VISIBLE);
                                        stopServer.setVisibility(View.INVISIBLE);
                                    }
                                } catch (Exception e) {
                                    Log.d("layout show service", e.toString());
                                }
                            } else {
                                startServer.setVisibility(View.INVISIBLE);
                                stopServer.setVisibility(View.INVISIBLE);
                            }

                        }
                    }
                });


            }
        }, 0, 1000);


        if (isWifiConnected) {

        }
        /*
        }else{

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setNegativeButton("OK", new DialogInterface.OnClickListener(){

                @Override
                public void onClick(DialogInterface dialog, int which) {

                    finish();
                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.setMessage("Please allow all permissions requested to run this app");

            alertDialog.show();



        }
        */
    }

    public static class ServerService extends Service {

        static MyServer myServer;

        private static MyServer myServerPrivate;
        static Boolean isServerAlive;


        /*
        public ServerService(){
            super("ServerService");
        }
        */

        @Override

        public int onStartCommand(Intent intent, int flags, int startId)

        {
            //public void onHandleIntent(Intent intent){


            ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();
            String[] STAR = {"*"};

            Cursor cursor;
            Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";


            ContentResolver contentResolver = getContentResolver();

            SharedPreferences sharedPreferences = getSharedPreferences("music_ip", 0);

            // cursor = managedQuery(uri, STAR, selection, null, null);
            cursor = contentResolver.query(uri, STAR, selection, null, null);

            if (cursor != null) {
                if (cursor.moveToFirst()) {

                    int i = 0;

                    do {

                        i++;
                        String songName = cursor
                                .getString(cursor
                                        .getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));

                        String duration = cursor
                                .getString(cursor
                                        .getColumnIndex(MediaStore.Audio.Media.DURATION));


                        String path = cursor.getString(cursor
                                .getColumnIndex(MediaStore.Audio.Media.DATA));


                        String albumName = cursor.getString(cursor
                                .getColumnIndex(MediaStore.Audio.Media.ALBUM));

                        String artistName = cursor.getString(cursor
                                .getColumnIndex(MediaStore.Audio.Media.ARTIST));


                        int albumId = cursor
                                .getInt(cursor
                                        .getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));

                        HashMap<String, String> song = new HashMap<String, String>();
                        song.put("songTitle_" + i, songName);
                        song.put("songPath_" + i, path);
                        song.put("duration_" + i, duration);
                        song.put("artistName_" + i, artistName);
                        song.put("albumName_" + i, albumName);
                        songsList.add(song);

                    } while (cursor.moveToNext());


                }

            }


            WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);


            /*
            try {

            } catch (IOException e) {
                e.printStackTrace();
            }*/


            try {
                myServer = new MyServer(8080, sharedPreferences, songsList, getAssets(), wifiManager);
                myServer.createServerRunnable(10000);

                if (!myServer.isAlive()) {
                    myServer.start(10000);
                    isServerAlive = true;

                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {

            }

              return START_STICKY;
        }


        private static boolean isMyServiceRunning() {

            ActivityManager manager = (ActivityManager) baseContext.getSystemService(ACTIVITY_SERVICE);
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                //  Log.d("service name",service.service.getClassName());

                if ("com.tuhin.remote_audio".equals(service.service.getClassName())) {

                    return true;
                }
            }
            return false;
        }


        @Override
        public void onDestroy() {
            super.onDestroy();

            if (myServer.isAlive()) {
                myServer.stop();
                isServerAlive = false;
            }
        }

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        @Override
        public boolean stopService(Intent name) {
            if (myServer.isAlive()) {
                myServer.stop();
                isServerAlive = false;
            }


            return super.stopService(name);
        }


        @Override
        public void onTaskRemoved(Intent rootIntent) {
            if (myServer.isAlive()) {
                myServer.stop();
                isServerAlive = false;
            }


            super.onTaskRemoved(rootIntent);
        }

        /*

        public static Boolean isServerAlive() {
            try {
                if (myServer.isAlive()) {

                    return true;
                } else {
                    return false;
                }
            }catch(NullPointerException e){
                return false;
            }
        }

*/

        public static void startServer() {
            try {
                if (!myServer.isAlive()) {
                    myServer.start(10000);
                    isServerAlive = true;

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public static void stopServer() {
            if (myServer.isAlive()) {
                myServer.stop();
                isServerAlive = false;
            }
        }

    }


}


class MyServer extends NanoHTTPD {

    WifiManager wm;


    AssetManager assetManager;
    String ip;
    ArrayList<HashMap<String, String>> list;
    SharedPreferences sharedPreferences;

    public MyServer(int port, SharedPreferences sharedPreferences2, ArrayList<HashMap<String, String>> songsList, AssetManager assetManager2, WifiManager wifiManager) throws IOException {
        super(port);

        //   ip = ip2;
        wm = wifiManager;
        list = songsList;
        sharedPreferences = sharedPreferences2;
        assetManager = assetManager2;
        ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        //   ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());

    }


    private Response createResponse(Response.Status status, String mimeType,
                                    InputStream message) {
        Response res = new Response(status, mimeType, message, 100);


        res.addHeader("Accept-Ranges", "bytes");
        return res;
    }


    private Response serveFile(String uri, Map<String, String> header,
                               File file, String mime) throws FileNotFoundException {
        Response res;
        try {
            // Calculate etag


            String etag = Integer.toHexString((file.getAbsolutePath()
                    + file.lastModified() + "" + file.length()).hashCode());


            // Support (simple) skipping:
            long startFrom = 0;
            long endAt = -1;
            String range = header.get("range");
            if (range != null) {
                if (range.startsWith("bytes=")) {
                    range = range.substring("bytes=".length());
                    int minus = range.indexOf('-');
                    try {
                        if (minus > 0) {
                            startFrom = Long.parseLong(range
                                    .substring(0, minus));
                            endAt = Long.parseLong(range.substring(minus + 1));
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }


            // Change return code and add Content-Range header when skipping is
            // requested
            long fileLen = file.length();
            if (range != null && startFrom >= 0) {
                if (startFrom >= fileLen) {
                    res = createResponse(Response.Status.RANGE_NOT_SATISFIABLE,
                            NanoHTTPD.MIME_PLAINTEXT, new FileInputStream(file));
                    res.addHeader("Content-Range", "bytes 0-0/" + fileLen);
                    res.addHeader("ETag", etag);
                } else {
                    if (endAt < 0) {
                        endAt = fileLen - 1;
                    }
                    long newLen = endAt - startFrom + 1;
                    if (newLen < 0) {
                        newLen = 0;
                    }

                    final long dataLen = newLen;
                    FileInputStream fis = new FileInputStream(file) {
                        @Override
                        public int available() throws IOException {
                            return (int) dataLen;
                        }
                    };
                    fis.skip(startFrom);

                    res = createResponse(Response.Status.PARTIAL_CONTENT, mime,
                            fis);
                    res.addHeader("Content-Length", "" + dataLen);
                    res.addHeader("Content-Range", "bytes " + startFrom + "-"
                            + endAt + "/" + fileLen);
                    res.addHeader("ETag", etag);
                }
            } else {
                if (etag.equals(header.get("if-none-match")))
                    res = createResponse(Response.Status.NOT_MODIFIED, mime, new FileInputStream(file));
                else {
                    res = createResponse(Response.Status.OK, mime,
                            new FileInputStream(file));
                    res.addHeader("Content-Length", "" + fileLen);
                    res.addHeader("ETag", etag);
                }
            }
        } catch (IOException ioe) {
            res = createResponse(Response.Status.FORBIDDEN,
                    NanoHTTPD.MIME_PLAINTEXT, new FileInputStream(file));
        }


        return res;
    }

    @Override
    public void setAsyncRunner(AsyncRunner asyncRunner) {
        super.setAsyncRunner(asyncRunner);
    }

    @Override
    public void setServerSocketFactory(ServerSocketFactory serverSocketFactory) throws IOException {

        serverSocketFactory.create();


        super.setServerSocketFactory(serverSocketFactory);
    }


    @Override
    public Response serve(final IHTTPSession session) throws IOException {
        super.serve(session);


        //  AssetManager assetManager = getAssets();

        Log.d("server is alive", "responding");


        String css = "assets\\bootstrap\\css\\bootstrap.min.css";
        String js = "assets\\bootstrap\\js\\bootstrap.min.css";

        String msg = "<html><head>" +
                "" +
                "<!-- Latest compiled and minified CSS -->" +
                "<link rel='stylesheet' href='http://" + ip + ":8080/code/css/' />" +
                "<link rel='stylesheet' href='http://" + ip + ":8080/code/css/custom/' />" +
                "" +
                "<!-- jQuery library -->" +
                "" +
                "<!-- Latest compiled JavaScript -->" +
                "<script src='http://" + ip + ":8080/code/js/jquery/' ></script>" +
                "<script src='http://" + ip + ":8080/code/js/' ></script>" +
                "<script src='http://" + ip + ":8080/code/js/custom_js/' ></script> " +

                "<title> Welcome to Remote IP Music Player</title>" +
                "</head>" +
                "<body> <div class='main list-group'> ";

        Response res = null;

        String secongMSG = msg;

        // return newFixedLengthResponse(msg);
        String uri = session.getUri();
        String endHTML = null;
        int i = 0;

        endHTML += "  </div>  </body> </html> ";


        File fis = null;


        try {


            for (HashMap<String, String> listSong : list) {

                i++;
                String title = listSong.get("songTitle_" + i);
                String path = listSong.get("songPath_" + i);
                String duration = listSong.get("duration_" + i);
                String artistName = listSong.get("artistName_" + i);
                String album_name = listSong.get("albumName_" + i);

                String link = "http://" + ip + ":8080/player/" + i;

                msg += "<div class='alert alert-success ' >  <h3 class='song_name '>" + title + "</h3> ";


                msg += "<div class='song_detail_holder'> <p> Artist : " + artistName + " </p> <p> Album : " + album_name + "</p> </div>";

                msg += "<a class='click_class btn-block btn btn-success' id='" + link + "'href='#" + i + "player' style='text-decoration:none;font-size:20px'> Play </a>  </div>";


                if (uri.equals("/code/css/")) {

                    InputStream streamCss = assetManager.open("bootstrap/css/bootstrap.min.css");

                    //  res = newFixedLengthResponse(Response.Status.OK, "text/css", streamCss, 100);

                    res = newChunkedResponse(Response.Status.OK, "text/css", streamCss);
                }

                if (uri.equals("/code/css/custom/")) {

                    InputStream streamCss = assetManager.open("bootstrap/css/custom.css");

                    res = newChunkedResponse(Response.Status.OK, "text/css", streamCss);
                }

                if (uri.equals("/code/js/")) {

                    InputStream streamJS = assetManager.open("bootstrap/js/bootstrap.min.js");


                    res = newChunkedResponse(Response.Status.OK, "application/javascript", streamJS);
                }

                if (uri.equals("/code/js/jquery/")) {

                    InputStream streamJSquery = assetManager.open("bootstrap/js/jquery.min.js");

                    res = newChunkedResponse(Response.Status.OK, "application/javascript", streamJSquery);
                    // res = newFixedLengthResponse(Response.Status.OK,"application/javascript",streamJS,90000);
                }


                if (uri.equals("/code/js/custom_js/")) {

                    InputStream streamJS = assetManager.open("bootstrap/js/custom_js.js");

                    res = newChunkedResponse(Response.Status.OK, "application/javascript", streamJS);
                }


                if (uri.equals("/player/" + i)) {
                    String alert = "<div class='well well-lg'> <div class='resize'> <div class='main_div'>" +
                            "  <strong>Now Playing </strong> <br/> " +
                            title +
                            "</div>";

                    String msgPlayer = "<audio controls autoplay>" +
                            "  <source src='http://" + ip + ":8080/" + i + "' type='audio/mpeg'>" +
                            "Your browser does not support the audio element." +
                            "</audio> </div> </div>";

                    res = newFixedLengthResponse(alert + msgPlayer);
                }


                if (uri.equals("/" + i)) {

                    fis = new File(path);
                    //  res = newChunkedResponse(Response.Status.OK, "audio/mpeg", fis);

                    String mimeType = "audio/mpeg";

                    return serveFile(uri, session.getHeaders(), fis, mimeType);

                } else if (uri.equals("/")) {

                    String showPlayer = "<div class='display_player'></div>";

                        /*
                        showPlayer+="<script>$('.display_player').hide();</script>";
                        showPlayer+="<script> $('.display_player').click(function(){" +
                                "    show();" +
                                "});</script>";


                        */
                    res = newFixedLengthResponse(msg + "</div>" + showPlayer + "</body> </html>");
                }

            }

            Log.d("uri server", session.getUri());

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            //  e.printStackTrace();

            Log.d("file found ", e.toString());
        }


        // return new NanoHTTPD.Response(Response.Status.OK, "audio/mpeg", fis);

        // return newChunkedResponse(Response.Status.OK, "audio/mpeg", fis);
        if (i == 0) {
            res = newFixedLengthResponse(msg + "</div>" + "Can not find any music files to your android device...." + "</body> </html>");
        }

        return res;
    }

    @Override
    protected ServerRunnable createServerRunnable(int timeout) {

        return super.createServerRunnable(timeout);

    }


}



