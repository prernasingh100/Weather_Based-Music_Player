package com.example.user.sdcard1;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by user on 02-04-2017.
 */
public class FileHandler {

    private FileHandlerCallback callback;
    private Exception error1;   //for internal storage error
    private Exception error2;   //for external storage error
    private ArrayList<File> songsInternal;
    private ArrayList<File> al=new ArrayList<File>();

    public FileHandler(FileHandlerCallback callback) {
        this.callback = callback;
    }

    public void getSongs() {

        new AsyncTask<Void, Void, ArrayList<File>>() {
            @Override
            protected ArrayList<File> doInBackground(Void... params) {

                //reading files from external sd card

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {

                    if ((new File("/mnt/sdcard1").exists()) || (new File("/sdcard1").exists())) {

                        if (new File("/mnt/sdcard1").exists()) {
                            findSongs(new File("/mnt/sdcard1"));
                        }
                        else {
                            findSongs(new File("/sdcard1"));
                        }
                            }
                    else {
                        if ((new File("/extSdCard").exists())&&((!Environment.getExternalStorageDirectory().getAbsolutePath().equals("/extSdCard"))||(!Environment.getExternalStorageDirectory().getAbsolutePath().equals("/mnt/extSdCard")))) {
                            findSongs(new File("/extSdCard"));
                        }
                            }
                                }
                else{
                    if(new File("/storage/sdcard1").exists()){

                    findSongs(new File("/storage/sdcard1"));
                }
                    }

                if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    songsInternal = findSongs(Environment.getExternalStorageDirectory());     //reading internal sd card

                }
                else {
                    callback.mediaUnmounted(new FileException("Internal SD Card not available"));
                }
                return songsInternal;
            }

            @Override
            protected void onPostExecute(ArrayList<File> files) {
                if(error1!=null){
                    callback.readFailure(new FileException("Your device does give permission to read files "));
                }
               else if((files==null)&&(error1==null&&error2==null)){
                    callback.noSongsRead(new FileException("No songs is available in your device"));
                }
                else{
                    callback.readSuccess(files);
                }
            }
        }.execute();
    }

    public ArrayList<File> findSongs(File root){
        try {
            File[] files = root.listFiles();
            for (File singleFile : files) {
                if ((singleFile.isDirectory()) & (!singleFile.isHidden())) {
                    findSongs(singleFile);
                }
                else {
                    if ((singleFile.getName().endsWith("mp3")) || (singleFile.getName().endsWith(".wav"))) {
                        al.add(singleFile);
                    }
                }
            }
        } catch (Exception e){
            if(root==Environment.getExternalStorageDirectory()) {
                error1 = e;
            }
            else{
                error2=e;
            }
        }
        return al;
    }

    public class FileException extends Exception{
        public FileException(String detailMessage) {
            super(detailMessage);
        }
    }
}