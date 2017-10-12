package com.example.user.sdcard1;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaMetadataRetriever;


import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ToggleButton;


import com.example.user.sdcard1.data.Channel;
import com.example.user.sdcard1.data.Item;
import com.example.user.sdcard1.service.WeatherServiceCallback;
import com.example.user.sdcard1.service.YahooWeatherService;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements FileHandlerCallback,WeatherServiceCallback,ToggleHandlerCallback {

    private AlertDialog.Builder adb;
    private String albumArtist[];
    private ProgressDialog dialog;
    private ProgressDialog dialog1;  //for toggleSuccess()
    private int conditionCode;
    private FileHandler filehandler;
    private ToggleHandler toggleHandler;
    private Location location;
    private double lat;
    private double lng;
    private YahooWeatherService service;
    private boolean isNetworkEnabled = false;      // flag for network status
    private ArrayList<File> songsInternal;
    private ArrayList<File> songPaths;
    private ArrayList<File> paths;
    private ListView songsList;
    private ToggleButton weatherToggle;
    private Item item;
    private Channel channel;
    private int index;
    private String musicList[];
    private String genre;
    private String weatherCondition;
    private StringBuffer sb;
    private HashMap<String,String> weatherMapping;
    private Intent i;
    private SQLiteDatabase db;
    private MediaMetadataRetriever metadataRetriever=new MediaMetadataRetriever();
    private boolean flag;

    private static final int PERMS_REQUEST_CODE=123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        if(hasPermissions()){
            //if our app has permissions

           showChoice();

        }
        else{
            //if our app doesn't have permissions, so permission is requested

            requestPerms();
        }
    }

    private boolean hasPermissions(){

        int res=0;

        //string array of permissions

        String[] permissions={Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION};

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M) {
            for (String perms : permissions) {

                res = checkCallingOrSelfPermission(perms);

                if (!(res == PackageManager.PERMISSION_GRANTED)) {

                    return false;
                }
            }
        }
        return true;
    }

    private void requestPerms(){

        String[] permissions={Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION};

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){

            requestPermissions(permissions,PERMS_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean allowed = true;

        switch (requestCode) {

            case PERMS_REQUEST_CODE:
                for (int res : grantResults) {

                    // if user has granted all permissions
                    allowed = allowed && (res == PackageManager.PERMISSION_GRANTED);
                }
                break;
            default:
                //if user has not granted permissions

                allowed = false;

                break;
        }
        if (allowed) {

            //if user grants all permissions, we can perform our task.
            showChoice();
        } else {
            //we will give warning message to the user that they have not granted the permissions

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if ((shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) && (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) || shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION))) {

                    Toast.makeText(this, "Storage Permissions and Location Permissions Denied\nCannot open application", Toast.LENGTH_SHORT).show();

                    finish();
                }
                else if (!(shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) || !(shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) || shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION))) {
                    Toast.makeText(this, "Go to settings and enable permissions.", Toast.LENGTH_SHORT).show();

                    finish();
                }
                else if(shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                    Toast.makeText(this, "Storage Permissions Denied\nCannot open application.", Toast.LENGTH_SHORT).show();

                    finish();
                }
                else if((shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)||(shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)))){
                    Toast.makeText(this, "Location Permissions Denied\nCannot open application.", Toast.LENGTH_SHORT).show();

                    finish();
                }
            }
        }
    }

    private void showChoice(){

        //alert dialog for choice

        adb=new AlertDialog.Builder(this);

        adb.setMessage("WeaPlay wants to play a song based on weather?");

        adb.setTitle("Weather Based Choice");

        adb.setIcon(getDrawable(R.drawable.icon_30));

        adb.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {

                flag=true;

                getWeather();
            }
        });

        adb.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                flag=false;

                generator();
            }
        });

        adb.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {

                Toast.makeText(getApplicationContext(),"Please Select a Choice",Toast.LENGTH_SHORT).show();

                finish();        //if user touches outside of alert dialog box
            }
        });

        adb.show();

    }

    private void generator(){

        filehandler=new FileHandler(this);

        filehandler.getSongs();     //read all music files

        dialog=new ProgressDialog(this);

        dialog.setMessage("Loading");

        dialog.setCanceledOnTouchOutside(false);     //block touch outside of progress dialog

        dialog.show();       //dialog for loading music files
    }

    @Override
    public void readSuccess(ArrayList<File> files) {

        songsInternal=files;    //all music files read

        //redirects to weather based player
        if(flag){

            dbOperations();     //creating tables & inserting paths of music file according to different genres of song

            createHashMap();

            service = new YahooWeatherService(MainActivity.this,true);    //if user does not click toggle button till now

            location=getLocation();     //Finding Location

            if(location==null){

                Toast.makeText(this,"Cannot find your location.Please check your GPS",Toast.LENGTH_LONG).show();

                finish();
            }

            else {
                lat = location.getLatitude();       //latitude

                lng = location.getLongitude();      //longitude

                dialog.hide();     //hide dialog after reading all music files

                dialog.dismiss();

                service.refreshWeather("(" + lat + "," + lng + ")");    //get weather condition based on coordinates.
                                                                        //redirects to serviceSuccess()
                dialog = new ProgressDialog(this);

                dialog.setMessage("Loading");

                dialog.setCanceledOnTouchOutside(false);     //block touch outside of progress dialog

                dialog.show();      //dialog for loading weather condition
            }
        }

        //redirects to normal player
        else{
            songsList=(ListView) findViewById(R.id.listView);

            weatherToggle=(ToggleButton) findViewById(R.id.weatherToggle);

            weatherToggle.setChecked(false);    //toggle button is in off state

            weatherToggle.setBackgroundDrawable(getDrawable(R.drawable.defaulttoggle));   //default image of weather toggle button

            musicList = new String[songsInternal.size()];

            albumArtist = new String[songsInternal.size()];

        for (int i = 0; i < songsInternal.size(); i++) {
            try{
                metadataRetriever.setDataSource(songsInternal.get(i).getAbsolutePath());

                musicList[i] = songsInternal.get(i).getName().replace(".mp3", "").replace(".wav", "");

                albumArtist[i]= metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);

            }catch (Exception e){

                musicList[i] = songsInternal.get(i).getName().replace(".mp3", "").replace(".wav", "");
            }
        }
            ArrayAdapter<String> adp = new CustomAdapter(this, musicList,albumArtist);

            songsList.setAdapter(adp);   //loads all music files

            dialog.hide();      //hide dialog after loading all music files

            dialog.dismiss();

            //user clicks on toggle button
            weatherToggle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //user switches to weather based player
                    if(weatherToggle.isChecked()){

                        dbOperations();     //creating tables & inserting paths of music file according to different genres of song

                        createHashMap();

                        service = new YahooWeatherService(MainActivity.this,false);   //false if user clicks toggle button for weather

                        location=getLocation();     //Finding Location

                        if(location==null){

                            Toast.makeText(getApplicationContext(),"Cannot find your location.Please check your GPS",Toast.LENGTH_LONG).show();

                            finish();
                        }
                        else {
                            lat = location.getLatitude();       //latitude

                            lng = location.getLongitude();      //longitude

                            service.refreshWeather("(" + lat + "," + lng + ")");    //get weather condition based on coordinates.
                                                                                    //redirects to serviceSucessForToggle()
                            dialog = new ProgressDialog(MainActivity.this);

                            dialog.setMessage("Loading");

                            dialog.setCanceledOnTouchOutside(false);     //block touch outside of progress dialog

                            dialog.show();    //dialog for loading weather condition
                        }
                    }

                    //user switches back to normal player
                    else{
                        flag=false;

                        weatherToggle.setBackgroundDrawable(getDrawable(R.drawable.defaulttoggle));   //default image of weather toggle

                        toggleHandler=new ToggleHandler(MainActivity.this,songsInternal,getApplicationContext(),MainActivity.this);

                        toggleHandler.getToggle();   //sets the list view to normal player & redirects to toggleSuccess()

                        dialog1 = new ProgressDialog(MainActivity.this);

                        dialog1.setMessage("Loading");

                        dialog1.setCanceledOnTouchOutside(false);     //block touch outside of progress dialog

                        dialog1.show();  //dialog for switching back to normal player

                        Toast.makeText(getApplicationContext(),"OFF",Toast.LENGTH_SHORT).show();
                    }
                }
            });

        songsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                i = new Intent(MainActivity.this, PlayerActivity.class);

                i.putExtra("pos", position);

                //if user switches to weather based player
                if(flag) {
                    i.putExtra("songs", songPaths);
                }

                //if user switches back to normal player
                else{
                    i.putExtra("songs", songsInternal);
                }
                startActivity(i);
            }
        });
    }
     }

    private void getWeather(){

        filehandler=new FileHandler(this);

        filehandler.getSongs();     //reading all the music file

        dialog=new ProgressDialog(this);

        dialog.setMessage("Loading");

        dialog.setCanceledOnTouchOutside(false);     //block touch outside of progress dialog

        dialog.show();      //dialog for reading all music files
    }

    @Override
    public void serviceSuccess(Channel channel) {

        //for weather based player

        this.channel=channel;

        weatherToggle=(ToggleButton) findViewById(R.id.weatherToggle);

        weatherToggle.setChecked(true);

        item = this.channel.getItem();

        weatherCondition=item.getCondition().getDescription();

        conditionCode=item.getCondition().getCode();

        switch (conditionCode) {
            case 32:
                weatherToggle.setBackgroundDrawable(getDrawable(R.drawable.sunny));
                break;
            case 34:
                weatherToggle.setBackgroundDrawable(getDrawable(R.drawable.sunny));
                break;
            case 30:
                weatherToggle.setBackgroundDrawable(getDrawable(R.drawable.partly_cloudy));
                break;
            case 4:
                weatherToggle.setBackgroundDrawable(getDrawable(R.drawable.thunder));
                break;
            case 31:
                weatherToggle.setBackgroundDrawable(getDrawable(R.drawable.clear));
                break;
            case 23:
                weatherToggle.setBackgroundDrawable(getDrawable(R.drawable.breezy));
                break;
            case 26:
                weatherToggle.setBackgroundDrawable(getDrawable(R.drawable.cloudy));
                break;

            case 28:weatherToggle.setBackgroundDrawable(getDrawable(R.drawable.mostly_cloudy));
                break;
            default:
                weatherToggle.setBackgroundDrawable(getDrawable(R.drawable.weathernotfound));
        }
        Toast.makeText(this,weatherCondition,Toast.LENGTH_LONG).show();

        String g=weatherMapping.get(weatherCondition);      //get genre based on weather condition

        songPaths=getPathList(g);

        songsList=(ListView) findViewById(R.id.listView);

        musicList = new String[songPaths.size()];

        albumArtist = new String[songPaths.size()];

        for (int i = 0; i < songPaths.size(); i++) {
            try {
                metadataRetriever.setDataSource(songPaths.get(i).getAbsolutePath());

                musicList[i] = songPaths.get(i).getName().replace(".mp3", "").replace(".wav", "");

                albumArtist[i]= metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
            }
            catch (Exception e){}
        }

        ArrayAdapter<String> adp = new CustomAdapter(this, musicList,albumArtist);

        songsList.setAdapter(adp);

        dialog.hide();      //hide dialog after weather based files have been displayed

        dialog.dismiss();

        weatherToggle.setOnClickListener(new View.OnClickListener() {
            @Override

            //user clicks on toggle button
            public void onClick(View v) {

                //user switches to normal player
                if(!weatherToggle.isChecked()){

                    weatherToggle.setBackgroundDrawable(getDrawable(R.drawable.defaulttoggle));

                    flag=false;

                    Toast.makeText(getApplicationContext(),"OFF",Toast.LENGTH_SHORT).show();

                    toggleHandler=new ToggleHandler(MainActivity.this,songsInternal,getApplicationContext(),MainActivity.this);

                    toggleHandler.getToggle();  //sets the list view based on normal player & redirects to toggleSuccess()

                    dialog1=new ProgressDialog(MainActivity.this);

                    dialog1.setMessage("Loading");  //dialog for switching to normal player

                    dialog1.setCanceledOnTouchOutside(false);     //block touch outside of progress dialog

                    dialog1.show();
                }

                //user switches back to weather based player
                else{
                    switch (conditionCode) {
                        case 32:
                            weatherToggle.setBackgroundDrawable(getDrawable(R.drawable.sunny));
                            break;
                        case 34:weatherToggle.setBackgroundDrawable(getDrawable(R.drawable.sunny));
                                break;
                        case 30:
                            weatherToggle.setBackgroundDrawable(getDrawable(R.drawable.partly_cloudy));
                            break;
                        case 4:
                            weatherToggle.setBackgroundDrawable(getDrawable(R.drawable.thunder));
                            break;
                        case 31:
                            weatherToggle.setBackgroundDrawable(getDrawable(R.drawable.clear));
                            break;
                        case 23:
                            weatherToggle.setBackgroundDrawable(getDrawable(R.drawable.breezy));
                            break;
                        case 26:
                            weatherToggle.setBackgroundDrawable(getDrawable(R.drawable.cloudy));
                            break;

                        case 28:
                            weatherToggle.setBackgroundDrawable(getDrawable(R.drawable.mostly_cloudy));
                            break;
                        default:
                            weatherToggle.setBackgroundDrawable(getDrawable(R.drawable.weathernotfound));
                    }

                    flag=true;

                    toggleHandler=new ToggleHandler(MainActivity.this,songPaths,getApplicationContext(),MainActivity.this);

                    toggleHandler.getToggle();      //sets the list view based on weather based & redirects to toggleSuccess()

                    dialog1=new ProgressDialog(MainActivity.this);

                    dialog1.setMessage("Loading");

                    dialog1.setCanceledOnTouchOutside(false);     //block touch outside of progress dialog

                    dialog1.show();     //dialog for switching to weather based player
                }
            }});

        songsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                i = new Intent(MainActivity.this, PlayerActivity.class);

                i.putExtra("pos", position);

                //if user switches back to weather based player
                if (flag) {
                    i.putExtra("songs", songPaths);
                }

                //if user switches to normal player
                else{
                    i.putExtra("songs", songsInternal);
                }
                startActivity(i);
            }
        });
    }

    @Override
    public void serviceSuccessForToggle(Channel channel) {

        this.channel=channel;

        dialog.hide();  //hide dialog after switching to weather based player when weather conditon is retrieved

        dialog.dismiss();

        flag=true;

        item = this.channel.getItem();

        weatherCondition=item.getCondition().getDescription();      //condition

        conditionCode=item.getCondition().getCode();        //condition code

        switch (conditionCode){
            case 32:
                weatherToggle.setBackgroundDrawable(getDrawable(R.drawable.sunny));
                break;

            case 30:
                weatherToggle.setBackgroundDrawable(getDrawable(R.drawable.partly_cloudy));
                break;
            case 4:
                weatherToggle.setBackgroundDrawable(getDrawable(R.drawable.thunder));
                break;
            case 31:
                weatherToggle.setBackgroundDrawable(getDrawable(R.drawable.clear));
                break;
            case 23:
                weatherToggle.setBackgroundDrawable(getDrawable(R.drawable.breezy));
                break;
            case 26:
                weatherToggle.setBackgroundDrawable(getDrawable(R.drawable.cloudy));
                break;

            case 28:weatherToggle.setBackgroundDrawable(getDrawable(R.drawable.mostly_cloudy));
                break;

            case 34: weatherToggle.setBackgroundDrawable(getDrawable(R.drawable.sunny));
            break;

            default:

                weatherToggle.setBackgroundDrawable(getDrawable(R.drawable.weathernotfound));
        }

        genre=weatherMapping.get(weatherCondition);     //genre

        songPaths=getPathList(genre);        //music files based on genre

        toggleHandler=new ToggleHandler(MainActivity.this,songPaths,getApplicationContext(),MainActivity.this);

        toggleHandler.getToggle();          //set the list view based on weather based & redirects to toggleSuccess()

         dialog1=new ProgressDialog(MainActivity.this);

         dialog1.setMessage("Loading");

         dialog1.setCanceledOnTouchOutside(false);     //block touch outside of progress dialog

         dialog1.show();        //dialog for loading listview based on weather based player
    }

    @Override
    public void serviceFailure(Exception exception) {

        dialog.hide();

        dialog.dismiss();

        Toast.makeText(this,exception.getMessage(),Toast.LENGTH_SHORT).show();

        finish();

    }

    private ArrayList<File> getPathList(String genre){

        paths=new ArrayList<File>();
        Cursor c;

        if(genre.equals("Bollywood Music")||genre.equals("Dance") ||genre.equals("Electronic Dance Music")|| genre.equals("Electronic Dance")||genre.equals("K-Pop")||genre.equals("Pop")||genre.equals("Single/Pop")||genre.equals("Indipop & Remix")||genre.equals("Indie Pop")||genre.equals("Classic")||genre.equals("Hip-Hop")||genre.equals("Hip-hop")) {

            switch (genre) {

                case "Bollywood Music":

                     c= db.rawQuery("SELECT * FROM Bollywood",null);

                    if(c.moveToNext()) {
                        c.moveToPrevious();

                        while (c.moveToNext()) {

                            StringBuilder sb = new StringBuilder(c.getString(0));

                            File f = new File(sb.toString());

                            paths.add(f);
                        }
                    }
                    else{

                        c=db.rawQuery("SELECT * FROM Others",null);

                        if(c.moveToNext()) {
                            c.moveToPrevious();

                            while (c.moveToNext()) {

                                StringBuilder sb = new StringBuilder(c.getString(0));

                                File f = new File(sb.toString());

                                paths.add(f);
                            }
                        }
                        else {

                            Toast.makeText(this,"There are no songs mapped to the weather outside",Toast.LENGTH_SHORT).show();

                            finish();
                        }

                    }

                    break;
                case "Dance":

                     c= db.rawQuery("SELECT * FROM EDM",null);

                    if(c.moveToNext()) {
                        c.moveToPrevious();

                    while(c.moveToNext()){

                        StringBuilder sb=new StringBuilder(c.getString(0));

                        File f=new File(sb.toString());

                        paths.add(f);
                    }
                        }
                    else{

                        c=db.rawQuery("SELECT * FROM Others",null);

                        if(c.moveToNext()) {
                            c.moveToPrevious();

                            while (c.moveToNext()) {

                                StringBuilder sb = new StringBuilder(c.getString(0));

                                File f = new File(sb.toString());

                                paths.add(f);
                            }
                        }
                        else {

                            Toast.makeText(this,"There are no songs mapped to the weather outside",Toast.LENGTH_SHORT).show();

                            finish();
                        }

                    }
                    break;

                case "Electronic Dance Music":

                     c= db.rawQuery("SELECT * FROM EDM",null);

                    if(c.moveToNext()) {
                        c.moveToPrevious();

                        while (c.moveToNext()) {

                            StringBuilder sb = new StringBuilder(c.getString(0));

                            File f = new File(sb.toString());

                            paths.add(f);
                        }
                    }

                    else{

                        c=db.rawQuery("SELECT * FROM Others",null);

                        if(c.moveToNext()) {
                            c.moveToPrevious();

                            while (c.moveToNext()) {

                                StringBuilder sb = new StringBuilder(c.getString(0));

                                File f = new File(sb.toString());

                                paths.add(f);
                            }
                        }
                        else {

                            Toast.makeText(this,"There are no songs mapped to the weather outside",Toast.LENGTH_SHORT).show();

                            finish();
                        }

                    }

                    break;

                case "Electronic Dance":

                     c= db.rawQuery("SELECT * FROM EDM",null);

                    if(c.moveToNext()) {
                        c.moveToPrevious();

                        while(c.moveToNext()){

                        StringBuilder sb=new StringBuilder(c.getString(0));

                        File f=new File(sb.toString());

                        paths.add(f);
                    }
                        }
                    else{

                        c=db.rawQuery("SELECT * FROM Others",null);

                        if(c.moveToNext()) {
                            c.moveToPrevious();

                            while (c.moveToNext()) {

                                StringBuilder sb = new StringBuilder(c.getString(0));

                                File f = new File(sb.toString());

                                paths.add(f);
                            }
                        }
                        else {

                            Toast.makeText(this,"There are no songs mapped to the weather outside",Toast.LENGTH_SHORT).show();

                            finish();
                        }

                    }

                    break;

                case "K-Pop":

                        c = db.rawQuery("SELECT * FROM K_Pop", null);

                    if(c.moveToNext()) {
                        c.moveToPrevious();

                        while (c.moveToNext()) {

                            StringBuilder sb = new StringBuilder(c.getString(0));

                            File f = new File(sb.toString());

                            paths.add(f);
                        }
                    }
                    else{

                        c=db.rawQuery("SELECT * FROM Others",null);

                        if(c.moveToNext()) {
                            c.moveToPrevious();

                            while (c.moveToNext()) {

                                StringBuilder sb = new StringBuilder(c.getString(0));

                                File f = new File(sb.toString());

                                paths.add(f);
                            }
                        }
                        else {

                            Toast.makeText(this,"There are no songs mapped to the weather outside",Toast.LENGTH_SHORT).show();

                            finish();
                        }

                    }

                    break;

                case "Pop":

                     c= db.rawQuery("SELECT * FROM K_Pop",null);

                    if(c.moveToNext()) {
                        c.moveToPrevious();

                        while (c.moveToNext()) {

                            StringBuilder sb = new StringBuilder(c.getString(0));

                            File f = new File(sb.toString());

                            paths.add(f);
                        }
                    }
                    else{

                        c=db.rawQuery("SELECT * FROM Others",null);

                        if(c.moveToNext()) {
                            c.moveToPrevious();

                            while (c.moveToNext()) {

                                StringBuilder sb = new StringBuilder(c.getString(0));

                                File f = new File(sb.toString());

                                paths.add(f);
                            }
                        }
                        else {

                            Toast.makeText(this,"There are no songs mapped to the weather outside",Toast.LENGTH_SHORT).show();

                            finish();
                        }

                    }


                    break;

                case "Single/Pop":

                     c= db.rawQuery("SELECT * FROM K_Pop",null);

                    if(c.moveToNext()) {
                        c.moveToPrevious();

                        while (c.moveToNext()) {

                            StringBuilder sb = new StringBuilder(c.getString(0));

                            File f = new File(sb.toString());

                            paths.add(f);
                        }
                    }
                    else{

                        c=db.rawQuery("SELECT * FROM Others",null);

                        if(c.moveToNext()) {
                            c.moveToPrevious();

                            while (c.moveToNext()) {

                                StringBuilder sb = new StringBuilder(c.getString(0));

                                File f = new File(sb.toString());

                                paths.add(f);
                            }
                        }
                        else {

                            Toast.makeText(this,"There are no songs mapped to the weather outside",Toast.LENGTH_SHORT).show();

                            finish();
                        }

                    }

                    break;

                case "Indipop & Remix":

                     c= db.rawQuery("SELECT * FROM Indipop_Remix",null);

                    if(c.moveToNext()) {
                        c.moveToPrevious();

                        while (c.moveToNext()) {

                            StringBuilder sb = new StringBuilder(c.getString(0));

                            File f = new File(sb.toString());

                            paths.add(f);
                        }
                    }
                    else{

                        c=db.rawQuery("SELECT * FROM Others",null);

                        if(c.moveToNext()) {
                            c.moveToPrevious();

                            while (c.moveToNext()) {

                                StringBuilder sb = new StringBuilder(c.getString(0));

                                File f = new File(sb.toString());

                                paths.add(f);
                            }
                        }
                        else {

                            Toast.makeText(this,"There are no songs mapped to the weather outside",Toast.LENGTH_SHORT).show();

                            finish();
                        }

                    }

                    break;

                case "Indie Pop":

                     c= db.rawQuery("SELECT * FROM Indipop_Remix",null);

                    if(c.moveToNext()) {
                        c.moveToPrevious();


                        while (c.moveToNext()) {

                            StringBuilder sb = new StringBuilder(c.getString(0));

                            File f = new File(sb.toString());

                            paths.add(f);
                        }
                    }
                    else{

                        c=db.rawQuery("SELECT * FROM Others",null);

                        if(c.moveToNext()) {
                            c.moveToPrevious();

                            while (c.moveToNext()) {

                                StringBuilder sb = new StringBuilder(c.getString(0));

                                File f = new File(sb.toString());

                                paths.add(f);
                            }
                        }
                        else {

                            Toast.makeText(this,"There are no songs mapped to the weather outside",Toast.LENGTH_SHORT).show();

                            finish();
                        }

                    }

                    break;

                case "Classic":

                     c= db.rawQuery("SELECT * FROM Classical",null);

                    if(c.moveToNext()) {
                        c.moveToPrevious();

                        while (c.moveToNext()) {

                            StringBuilder sb = new StringBuilder(c.getString(0));

                            File f = new File(sb.toString());

                            paths.add(f);
                        }
                    }
                    else{

                        c=db.rawQuery("SELECT * FROM Others",null);

                        if(c.moveToNext()) {
                            c.moveToPrevious();

                            while (c.moveToNext()) {

                                StringBuilder sb = new StringBuilder(c.getString(0));

                                File f = new File(sb.toString());

                                paths.add(f);
                            }
                        }
                        else {

                            Toast.makeText(this,"There are no songs mapped to the weather outside",Toast.LENGTH_SHORT).show();

                            finish();
                        }

                    }


                    break;

                case "Hip-Hop":

                     c= db.rawQuery("SELECT * FROM Hip_Hop",null);

                    if(c.moveToNext()) {
                        c.moveToPrevious();

                        while (c.moveToNext()) {

                            StringBuilder sb = new StringBuilder(c.getString(0));

                            File f = new File(sb.toString());

                            paths.add(f);
                        }
                    }
                    else{

                        c=db.rawQuery("SELECT * FROM Others",null);

                        if(c.moveToNext()) {
                            c.moveToPrevious();

                            while (c.moveToNext()) {

                                StringBuilder sb = new StringBuilder(c.getString(0));

                                File f = new File(sb.toString());

                                paths.add(f);
                            }
                        }
                        else {

                            Toast.makeText(this,"There are no songs mapped to the weather outside",Toast.LENGTH_SHORT).show();

                            finish();
                        }

                    }

                    break;

                case "Hip-hop":

                    c= db.rawQuery("SELECT * FROM Hip_Hop",null);

                    if(c.moveToNext()) {
                        c.moveToPrevious();

                        String s = "";

                        while (c.moveToNext()) {

                            StringBuilder sb = new StringBuilder(c.getString(0));

                            File f = new File(sb.toString());

                            paths.add(f);
                        }
                    }
                    else{

                    c=db.rawQuery("SELECT * FROM Others",null);

                    if(c.moveToNext()) {
                        c.moveToPrevious();

                        while (c.moveToNext()) {

                            StringBuilder sb = new StringBuilder(c.getString(0));

                            File f = new File(sb.toString());

                            paths.add(f);
                        }
                    }
                    else {

                        Toast.makeText(this,"There are no songs mapped to the weather outside",Toast.LENGTH_SHORT).show();

                        finish();
                    }

                }
                    break;

            }
        }
        else {

            c = db.rawQuery("SELECT * FROM '" +genre+ "' ", null);

            if(c.moveToNext()) {
                c.moveToPrevious();

                while (c.moveToNext()) {

                    StringBuilder sb = new StringBuilder(c.getString(0));

                    File f = new File(sb.toString());

                    paths.add(f);
                }
            }
            else{

                c=db.rawQuery("SELECT * FROM Others",null);

                if(c.moveToNext()) {
                    c.moveToPrevious();

                    while (c.moveToNext()) {

                        StringBuilder sb = new StringBuilder(c.getString(0));

                        File f = new File(sb.toString());

                        paths.add(f);
                    }
                }
                else {

                    Toast.makeText(this,"There are no songs mapped to the weather outside",Toast.LENGTH_SHORT).show();

                    finish();

                }

            }
        }
        return paths;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void toggleSuccess() {

        Thread  t = new Thread() {

            @Override
            public void run() {

                super.run();

                try {
                    sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        t.start();

        if(flag){

            Toast.makeText(getApplicationContext(),"ON",Toast.LENGTH_SHORT).show();

            Toast.makeText(getApplicationContext(),weatherCondition,Toast.LENGTH_LONG).show();
        }

        dialog1.hide();     //hide dialog after listview contents have been changed

        dialog1.dismiss();
    }

    @Override
    public void readFailure(Exception exception) {

        dialog.hide();

        dialog.dismiss();

        Toast.makeText(this,exception.getMessage(),Toast.LENGTH_SHORT).show();

        finish();
    }

    @Override
    public void noSongsRead(Exception exception) {

        dialog.hide();

        dialog.dismiss();

        Toast.makeText(this,exception.getMessage(),Toast.LENGTH_SHORT).show();

        finish();
    }

    @Override
    public void mediaUnmounted(Exception exception) {

        dialog.hide();

        dialog.dismiss();

        Toast.makeText(this,exception.getMessage(),Toast.LENGTH_SHORT).show();

        finish();

    }

    private Location getLocation() {

        //getting Location

        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        isNetworkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!isNetworkEnabled) {
            // no network provider is enabled

            Toast.makeText(this, "Your Network Provider is not enabled", Toast.LENGTH_SHORT).show();

            finish();
        }
        else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.

            }
            location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            if(location==null){

                Toast.makeText(this,"Cannot get Location",Toast.LENGTH_SHORT).show();

                finish();
            }
        }
        return location;
    }

    private void dbOperations(){

        //Database Operations

        db=openOrCreateDatabase("Genre",MODE_PRIVATE,null);

        if(db!=null){

        }

        try{        //Create Table

            db.execSQL("CREATE TABLE Bollywood(File_Path varchar(2000) primary key)");
            db.execSQL("CREATE TABLE Alternative(File_Path varchar(2000) primary key)");
            db.execSQL("CREATE TABLE Rock(File_Path varchar(2000) primary key)");
            db.execSQL("CREATE TABLE EDM(File_Path varchar(2000) primary key)");
            db.execSQL("CREATE TABLE Electronic(File_Path varchar(2000) primary key)");
            db.execSQL("CREATE TABLE Tamil(File_Path varchar(2000) primary key)");
            db.execSQL("CREATE TABLE K_Pop(File_Path varchar(2000) primary key)");
            db.execSQL("CREATE TABLE Indipop_Remix(File_Path varchar(2000) primary key)");
            db.execSQL("CREATE TABLE Jazz(File_Path varchar(2000) primary key)");
            db.execSQL("CREATE TABLE Classical(File_Path varchar(2000) primary key)");
            db.execSQL("CREATE TABLE Opera(File_Path varchar(2000) primary key)");
            db.execSQL("CREATE TABLE Blues(File_Path varchar(2000) primary key)");
            db.execSQL("CREATE TABLE Comedy(File_Path varchar(2000) primary key)");
            db.execSQL("CREATE TABLE Country(File_Path varchar(2000) primary key)");
            db.execSQL("CREATE TABLE Latin(File_Path varchar(2000) primary key)");
            db.execSQL("CREATE TABLE Folk(File_Path varchar(2000) primary key)");
            db.execSQL("CREATE TABLE Hip_Hop(File_Path varchar(2000) primary key)");
            db.execSQL("CREATE TABLE Instrumental(File_Path varchar(2000) primary key)");
            db.execSQL("CREATE TABLE Trance(File_Path varchar(2000) primary key)");
            db.execSQL("CREATE TABLE Others(File_Path varchar(2000) primary key)");

        }

        catch (Exception e){

        }

        for(int i=0;i<songsInternal.size();i++){

            String pth=songsInternal.get(i).getAbsolutePath();

            try{
                metadataRetriever.setDataSource(songsInternal.get(i).getAbsolutePath());

                genre = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE); //getting genre

                if(genre!=null) {

                    switch (genre) {        //Insert Paths according to genre

                        case "Bollywood Music":

                            index=pth.indexOf("'");

                            if(index!=-1){

                                sb=new StringBuffer(pth);

                                sb.insert(index,"'");

                                db.execSQL("INSERT INTO Bollywood VALUES('" + sb + "')");

                            }
                            else {

                                db.execSQL("INSERT INTO Bollywood VALUES('" + songsInternal.get(i).getAbsolutePath() + "')");
                            }
                            break;

                        case "Bollywood":

                            index=pth.indexOf("'");

                            if(index!=-1){

                                sb=new StringBuffer(pth);

                                sb.insert(index,"'");

                                db.execSQL("INSERT INTO Bollywood VALUES('" + sb + "')");

                            }
                            else {

                                db.execSQL("INSERT INTO Bollywood VALUES('" + songsInternal.get(i).getAbsolutePath() + "')");
                            }
                            break;

                        case "Alternative":
                            index=pth.indexOf("'");

                            if(index!=-1){

                                sb=new StringBuffer(pth);

                                sb.insert(index,"'");

                                db.execSQL("INSERT INTO Alternative VALUES('" + sb + "')");

                            }
                            else {

                                db.execSQL("INSERT INTO Alternative VALUES('" + songsInternal.get(i).getAbsolutePath() + "')");
                            }
                            break;

                        case "EDM":
                            index=pth.indexOf("'");

                            if(index!=-1){

                                sb=new StringBuffer(pth);

                                sb.insert(index,"'");

                                db.execSQL("INSERT INTO EDM VALUES('" + sb + "')");

                            }
                            else {

                                db.execSQL("INSERT INTO EDM VALUES('" + songsInternal.get(i).getAbsolutePath() + "')");
                            }
                            break;

                        case "Dance":
                            index=pth.indexOf("'");

                            if(index!=-1){

                                sb=new StringBuffer(pth);

                                sb.insert(index,"'");

                                db.execSQL("INSERT INTO EDM VALUES('" + sb + "')");

                            }
                            else {

                                db.execSQL("INSERT INTO EDM VALUES('" + songsInternal.get(i).getAbsolutePath() + "')");
                            }
                            break;

                        case "Electronic Dance Music":
                            index=pth.indexOf("'");

                            if(index!=-1){

                                sb=new StringBuffer(pth);

                                sb.insert(index,"'");

                                db.execSQL("INSERT INTO EDM VALUES('" + sb + "')");

                            }
                            else {

                                db.execSQL("INSERT INTO EDM VALUES('" + songsInternal.get(i).getAbsolutePath() + "')");
                            }
                            break;

                        case "Electronic Dance":
                            index=pth.indexOf("'");

                            if(index!=-1){

                                sb=new StringBuffer(pth);

                                sb.insert(index,"'");

                                db.execSQL("INSERT INTO EDM VALUES('" + sb + "')");

                            }
                            else {

                                db.execSQL("INSERT INTO EDM VALUES('" + songsInternal.get(i).getAbsolutePath() + "')");
                            }
                            break;

                        case "Rock":
                            index=pth.indexOf("'");

                            if(index!=-1){

                                sb=new StringBuffer(pth);

                                sb.insert(index,"'");

                                db.execSQL("INSERT INTO Rock VALUES('" + sb + "')");

                            }
                            else {

                                db.execSQL("INSERT INTO Rock VALUES('" + songsInternal.get(i).getAbsolutePath() + "')");
                            }
                            break;

                        case "Electronic":
                            index=pth.indexOf("'");

                            if(index!=-1){

                                sb=new StringBuffer(pth);

                                sb.insert(index,"'");

                                db.execSQL("INSERT INTO Electronic VALUES('" + sb + "')");

                            }
                            else {

                                db.execSQL("INSERT INTO Electronic VALUES('" + songsInternal.get(i).getAbsolutePath() + "')");
                            }
                            break;

                        case "Tamil":
                            index=pth.indexOf("'");

                            if(index!=-1){

                                sb=new StringBuffer(pth);

                                sb.insert(index,"'");

                                db.execSQL("INSERT INTO Tamil VALUES('" + sb + "')");

                            }
                            else {

                                db.execSQL("INSERT INTO Tamil VALUES('" + songsInternal.get(i).getAbsolutePath() + "')");
                            }
                            break;

                        case "K-Pop":
                            index=pth.indexOf("'");

                            if(index!=-1){

                                sb=new StringBuffer(pth);

                                sb.insert(index,"'");

                                db.execSQL("INSERT INTO K_Pop VALUES('" + sb + "')");

                            }
                            else {

                                db.execSQL("INSERT INTO K_Pop VALUES('" + songsInternal.get(i).getAbsolutePath() + "')");
                            }
                            break;

                        case "Pop":
                            index=pth.indexOf("'");

                            if(index!=-1){

                                sb=new StringBuffer(pth);

                                sb.insert(index,"'");

                                db.execSQL("INSERT INTO K_Pop VALUES('" + sb + "')");

                            }
                            else {

                                db.execSQL("INSERT INTO K_Pop VALUES('" + songsInternal.get(i).getAbsolutePath() + "')");
                            }
                            break;

                        case "Single/Pop":
                            index=pth.indexOf("'");

                            if(index!=-1){

                                sb=new StringBuffer(pth);

                                sb.insert(index,"'");

                                db.execSQL("INSERT INTO K_Pop VALUES('" + sb + "')");

                            }
                            else {

                                db.execSQL("INSERT INTO K_Pop VALUES('" + songsInternal.get(i).getAbsolutePath() + "')");
                            }
                            break;

                        case "Indipop & Remix":
                            index=pth.indexOf("'");

                            if(index!=-1){

                                sb=new StringBuffer(pth);

                                sb.insert(index,"'");

                                db.execSQL("INSERT INTO Indipop_Remix VALUES('" + sb + "')");

                            }
                            else {

                                db.execSQL("INSERT INTO Indipop_Remix VALUES('" + songsInternal.get(i).getAbsolutePath() + "')");
                            }
                            break;

                        case "Indie Pop":
                            index=pth.indexOf("'");

                            if(index!=-1){

                                sb=new StringBuffer(pth);

                                sb.insert(index,"'");

                                db.execSQL("INSERT INTO Indipop_Remix VALUES('" + sb + "')");

                            }
                            else {

                                db.execSQL("INSERT INTO Indipop_Remix VALUES('" + songsInternal.get(i).getAbsolutePath() + "')");
                            }
                            break;

                        case "Jazz":
                            index=pth.indexOf("'");

                            if(index!=-1){

                                sb=new StringBuffer(pth);

                                sb.insert(index,"'");

                                db.execSQL("INSERT INTO Jazz VALUES('" + sb + "')");

                            }
                            else {

                                db.execSQL("INSERT INTO Jazz VALUES('" + songsInternal.get(i).getAbsolutePath() + "')");
                            }
                            break;

                        case "Classical":
                            index=pth.indexOf("'");

                            if(index!=-1){

                                sb=new StringBuffer(pth);

                                sb.insert(index,"'");

                                db.execSQL("INSERT INTO Classical VALUES('" + sb + "')");

                            }
                            else {

                                db.execSQL("INSERT INTO Classical VALUES('" + songsInternal.get(i).getAbsolutePath() + "')");
                            }
                            break;

                        case "Classic":
                            index=pth.indexOf("'");

                            if(index!=-1){

                                sb=new StringBuffer(pth);

                                sb.insert(index,"'");

                                db.execSQL("INSERT INTO Classical VALUES('" + sb + "')");

                            }
                            else {

                                db.execSQL("INSERT INTO Classical VALUES('" + songsInternal.get(i).getAbsolutePath() + "')");
                            }
                            break;

                        case "Opera":
                            index=pth.indexOf("'");

                            if(index!=-1){

                                sb=new StringBuffer(pth);

                                sb.insert(index,"'");

                                db.execSQL("INSERT INTO Opera VALUES('" + sb + "')");

                            }
                            else {

                                db.execSQL("INSERT INTO Opera VALUES('" + songsInternal.get(i).getAbsolutePath() + "')");
                            }
                            break;
                        case "Blues":
                            index=pth.indexOf("'");

                            if(index!=-1){

                                sb=new StringBuffer(pth);

                                sb.insert(index,"'");

                                db.execSQL("INSERT INTO Blues VALUES('" + sb + "')");

                            }
                            else {

                                db.execSQL("INSERT INTO Blues VALUES('" + songsInternal.get(i).getAbsolutePath() + "')");
                            }
                            break;

                        case "Comedy":
                            index=pth.indexOf("'");

                            if(index!=-1){

                                sb=new StringBuffer(pth);

                                sb.insert(index,"'");

                                db.execSQL("INSERT INTO Comedy VALUES('" + sb + "')");

                            }
                            else {

                                db.execSQL("INSERT INTO Comedy VALUES('" + songsInternal.get(i).getAbsolutePath() + "')");
                            }
                            break;

                        case "Country":
                            index=pth.indexOf("'");

                            if(index!=-1){

                                sb=new StringBuffer(pth);

                                sb.insert(index,"'");

                                db.execSQL("INSERT INTO Country VALUES('" + sb + "')");

                            }
                            else {

                                db.execSQL("INSERT INTO Country VALUES('" + songsInternal.get(i).getAbsolutePath() + "')");
                            }
                            break;

                        case "Latin":
                            index=pth.indexOf("'");

                            if(index!=-1){

                                sb=new StringBuffer(pth);

                                sb.insert(index,"'");

                                db.execSQL("INSERT INTO Latin VALUES('" + sb + "')");

                            }
                            else {

                                db.execSQL("INSERT INTO Latin VALUES('" + songsInternal.get(i).getAbsolutePath() + "')");
                            }
                            break;

                        case "Folk":
                            index=pth.indexOf("'");

                            if(index!=-1){

                                sb=new StringBuffer(pth);

                                sb.insert(index,"'");

                                db.execSQL("INSERT INTO Folk VALUES('" + sb + "')");

                            }
                            else {

                                db.execSQL("INSERT INTO Folk VALUES('" + songsInternal.get(i).getAbsolutePath() + "')");
                            }
                            break;

                        case "Hip-Hop":
                            index=pth.indexOf("'");

                            if(index!=-1){

                                sb=new StringBuffer(pth);

                                sb.insert(index,"'");

                                db.execSQL("INSERT INTO Hip_Hop VALUES('" + sb + "')");

                            }
                            else {

                                db.execSQL("INSERT INTO Hip_Hop VALUES('" + songsInternal.get(i).getAbsolutePath() + "')");
                            }
                            break;

                        case "Hip-hop":
                            index=pth.indexOf("'");

                            if(index!=-1){

                                sb=new StringBuffer(pth);

                                sb.insert(index,"'");

                                db.execSQL("INSERT INTO Hip_Hop VALUES('" + sb + "')");

                            }
                            else {

                                db.execSQL("INSERT INTO Hip_Hop VALUES('" + songsInternal.get(i).getAbsolutePath() + "')");
                            }
                            break;

                        case "Instrumental":
                            index=pth.indexOf("'");

                            if(index!=-1){

                                sb=new StringBuffer(pth);

                                sb.insert(index,"'");

                                db.execSQL("INSERT INTO Instrumental VALUES('" + sb + "')");

                            }
                            else {

                                db.execSQL("INSERT INTO Instrumental VALUES('" + songsInternal.get(i).getAbsolutePath() + "')");
                            }
                            break;

                        case "Trance":
                            index=pth.indexOf("'");

                            if(index!=-1){

                                sb=new StringBuffer(pth);

                                sb.insert(index,"'");

                                db.execSQL("INSERT INTO Trance VALUES('" + sb + "')");

                            }
                            else {

                                db.execSQL("INSERT INTO Trance VALUES('" + songsInternal.get(i).getAbsolutePath() + "')");
                            }
                            break;

                        default:
                            index=pth.indexOf("'");

                            if(index!=-1){

                                sb=new StringBuffer(pth);

                                sb.insert(index,"'");

                                db.execSQL("INSERT INTO Others VALUES('" + sb + "')");

                            }
                            else {

                                db.execSQL("INSERT INTO Others VALUES('" + songsInternal.get(i).getAbsolutePath() + "') ");
                            }
                    }
                }
                else{

                    index=pth.indexOf("'");

                    if(index!=-1){

                        sb=new StringBuffer(pth);

                        sb.insert(index,"'");

                        db.execSQL("INSERT INTO Others VALUES('" + sb + "')");

                    }
                    else {

                        db.execSQL("INSERT INTO Others VALUES('" + songsInternal.get(i).getAbsolutePath() + "')");
                    }
                }
            }
            catch (Exception e){}
        }
    }

    private void createHashMap(){

        weatherMapping=new HashMap<String, String>();

        weatherMapping.put("Sunny","Bollywood");
        weatherMapping.put("Cloudy","EDM");
        weatherMapping.put("Thunderstorms","Classic");
        weatherMapping.put("Partly Cloudy","Alternative");
        weatherMapping.put("Breezy","Indipop & Remix");
        weatherMapping.put("Mostly Sunny","Tamil");
        weatherMapping.put("Mostly Cloudy","K-Pop");
    }
}