package com.example.user.sdcard1;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by user on 06-04-2017.
 */
public class ToggleHandler {

    private ToggleHandlerCallback callback;
    private String musicList[];
    private String albumArtist[];
    private ArrayList<File> songPaths;
    private MediaMetadataRetriever metadataRetriever=new MediaMetadataRetriever();
    private Context context;
    private ArrayAdapter<String> adp;
    private AppCompatActivity appCompatActivity;
    private ListView songsList;

    public ToggleHandler(ToggleHandlerCallback callback, ArrayList<File> files, Context context,AppCompatActivity appCompatActivity) {

        this.callback=callback;
        songPaths =files;
        this.context=context;
        this.appCompatActivity=appCompatActivity;
    }

    public void getToggle(){

        new AsyncTask<Void, Void, ArrayAdapter<String>>() {
            @Override
            protected ArrayAdapter<String> doInBackground(Void... params) {

                adp=toggleOff();

                return adp;
            }

            @Override
            protected void onPostExecute(ArrayAdapter<String> stringArrayAdapter) {

                songsList=(ListView)appCompatActivity.findViewById(R.id.listView);

                songsList.setAdapter(stringArrayAdapter);

                callback.toggleSuccess();
            }
        }.execute();
    }

    private ArrayAdapter<String> toggleOff(){

        musicList = new String[songPaths.size()];

        albumArtist = new String[songPaths.size()];

        for (int i = 0; i < songPaths.size(); i++) {
            try{
                metadataRetriever.setDataSource(songPaths.get(i).getAbsolutePath());

                musicList[i] = songPaths.get(i).getName().replace(".mp3", "").replace(".wav", "");

                albumArtist[i]= metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);

            }
            catch (Exception e){

                musicList[i] = songPaths.get(i).getName().replace(".mp3", "").replace(".wav", "");
            }
        }
        ArrayAdapter<String> adp = new CustomAdapter(context, musicList,albumArtist);

        return adp;
    }
}