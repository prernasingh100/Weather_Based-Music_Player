package com.example.user.sdcard1;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;


public class PlayerActivity extends AppCompatActivity {

    private ImageView ivbg;
    private Intent i;
    private ArrayList<File> al;
    private MediaPlayer mp;
    private SeekBar music;
    private TextView currentTime;
    private TextView totalTime;
    private Typeface roboto_regular;
    private TextView songTitle;
    private TextView songAlbum;
    private String title;
    private String extraInfo;
    private String searchExtraInfo ="-";
    private String albumName;
    private String artistName;
    private String albumName_artistName;
    private ImageView albumArt;
    private int index;
    private int position;
    private Thread t;
    private ImageButton pl;
    private MediaMetadataRetriever metadataRetriever=new MediaMetadataRetriever();
    private byte[] art;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        music = (SeekBar) findViewById(R.id.music);

        songTitle=(TextView) findViewById(R.id.songTitle);

        songAlbum=(TextView) findViewById(R.id.songAlbum);

        albumArt=(ImageView) findViewById(R.id.albumArt);

        currentTime =(TextView) findViewById(R.id.currentTime);

        totalTime =(TextView) findViewById(R.id.duration);

        pl = (ImageButton) findViewById(R.id.pl);

        ivbg = (ImageView) findViewById(R.id.bg_art);

        music.setScaleY(1.5f);      //setting height of seekbar

        i = getIntent();

        position = i.getIntExtra("pos", 0);       //retrieving positon of file

        al = (ArrayList) i.getParcelableArrayListExtra("songs"); //arraylist of file objects



        roboto_regular =Typeface.createFromAsset(getAssets(), "fonts/Roboto-Regular.ttf");

        songTitle.setTextSize(25);  //Text size for Title
        songTitle.setTypeface(roboto_regular);  //Font style for Title
        songTitle.setTextColor(Color.parseColor("#dddeec"));    //Text colour for Title

        songAlbum.setTypeface(roboto_regular);  //Font style for albumname-artist name
        songAlbum.setTextSize(17);  //Text size for albumname-artistname
        songAlbum.setTextColor(Color.parseColor("#ff5252")); //Text colour for albumname-artistname
        //Retrieving media metadata

        try {
            metadataRetriever.setDataSource(al.get(position).getAbsolutePath());

            art = metadataRetriever.getEmbeddedPicture();

            if (art != null) {

                Bitmap songImage = BitmapFactory.decodeByteArray(art, 0, art.length); //getting album art
                Bitmap blr = createBlurBitmap(songImage, 25);
                Bitmap blr1 = createBlurBitmap(blr, 25);
                Bitmap blr2 = createBlurBitmap(blr1, 25);
                Bitmap blr3 = createBlurBitmap(blr2, 25);
                albumArt.setImageBitmap(songImage);     //setting album art
                ivbg.setImageBitmap(blr3);

            } else {

                albumArt.setImageDrawable(getDrawable(R.drawable.defaultalbumart)); //default album art
                ivbg.setBackgroundColor(Color.BLACK);
            }

            title=metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE); //getting title of song

            if(title!=null) {

                 index=title.lastIndexOf(searchExtraInfo);

                 if(index!=-1){

                     extraInfo=title.substring(index);     //searching extra information in title

                     songTitle.setEllipsize(TextUtils.TruncateAt.MARQUEE);  //setting marquee

                     songTitle.setText(title.replace(extraInfo,""));   //setting title of the song and removing extra information

                     songTitle.setSelected(true);

                     songTitle.setSingleLine(true);
                  }
                else{
                     songTitle.setEllipsize(TextUtils.TruncateAt.MARQUEE);  //setting marquee

                     songTitle.setText(title);   //if no extra information is with title

                     songTitle.setSelected(true);

                     songTitle.setSingleLine(true);
                 }
            }
            else{
                songTitle.setEllipsize(TextUtils.TruncateAt.MARQUEE);  //setting marquee

                songTitle.setText(al.get(position).getName().replace(".mp3","").replace(".wav","")); //default title

                songTitle.setSelected(true);

                songTitle.setSingleLine(true);
            }
            albumName = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM); //album name

            artistName = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST); //artist name

            if ((albumName != null) && (artistName != null)) {

                albumName_artistName = albumName.concat("- ").concat(artistName); //album name-artist name

                songAlbum.setEllipsize(TextUtils.TruncateAt.MARQUEE);  //setting marquee

                songAlbum.setText(albumName_artistName); //setting album name-artist name

                songAlbum.setSelected(true);

                songAlbum.setSingleLine(true);
            } else {
                if ((albumName == null) && (artistName == null)) {

                    songAlbum.setText("Unknown Album");
                } else if (albumName == null) {

                    songAlbum.setText("Artist- " + artistName);
                } else {

                    songAlbum.setText(albumName);
                }
            }

        if (mp == null) {

                mp = MediaPlayer.create(this, Uri.parse(al.get(position).getAbsolutePath()));

                music.setMax(mp.getDuration());

                totalTime.setText(getTimeString(mp.getDuration()));

                mp.start();

            }

        music.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if (fromUser) {
                    mp.seekTo(progress);
                }
                currentTime.setText( getTimeString(mp.getCurrentPosition()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        t = new Thread() {

            @Override
            public void run() {

                super.run();

                while (true) {

                    if (mp == null) {
                        music.setProgress(0);
                    } else {
                        music.setProgress(mp.getCurrentPosition());
                    }
                }
            }
        };

        t.start();
    }
        catch (Exception e){

            Toast.makeText(this,"File could not be opened",Toast.LENGTH_SHORT).show();

            finish();
        }
    }

    private String getTimeString(long millis) {
        StringBuffer buf = new StringBuffer();

        int hours = (int) millis / (1000*60*60);
        int minutes = (int)( millis % (1000*60*60) ) / (1000*60);
        int seconds = (int)( ( millis % (1000*60*60) ) % (1000*60) ) / 1000;

        if(hours==0){

            buf.append(String.format("%02d", minutes)).append(":").append(String.format("%02d", seconds));
        }
        else{
            buf.append(String.format("%02d", hours)).append(":").append(String.format("%02d", minutes)).append(":").append(String.format("%02d", seconds));
        }

        return buf.toString();
    }

    public void play(View v){

        if(mp.isPlaying()){
            mp.pause();
            pl.setImageDrawable(getDrawable(R.drawable.play));
        }
        else {
            mp.start();
            pl.setImageDrawable(getDrawable(R.drawable.pause));
        }
    }

    public void next(View v) {

        if (mp != null) {

            mp.stop();              //stopping current song

            mp = null;
        }
        position = (position + 1) % al.size();     //incrementing position

        //Retrieving media metadata
        try {
            metadataRetriever.setDataSource(al.get(position).getAbsolutePath());

            art = metadataRetriever.getEmbeddedPicture();

            if (art != null) {

                Bitmap songImage = BitmapFactory.decodeByteArray(art, 0, art.length);  //getting album art
                Bitmap blr = createBlurBitmap(songImage, 25);
                Bitmap blr1 = createBlurBitmap(blr, 25);
                Bitmap blr2 = createBlurBitmap(blr1, 25);
                Bitmap blr3 = createBlurBitmap(blr2, 25);
                albumArt.setImageBitmap(songImage);     //setting album art
                ivbg.setImageBitmap(blr3);

            }
            else {
                albumArt.setImageDrawable(getDrawable(R.drawable.defaultalbumart)); //default album art
                ivbg.setImageBitmap(null);
                ivbg.setBackgroundColor(Color.BLACK);
            }
            title=metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE); //getting title of song

            if(title!=null) {

                index=title.lastIndexOf(searchExtraInfo);

                if(index!=-1){

                    extraInfo=title.substring(index);     //searching extra information in title

                    songTitle.setEllipsize(TextUtils.TruncateAt.MARQUEE);  //setting marquee

                    songTitle.setText(title.replace(extraInfo,""));   //setting title of the song and removing extra information

                    songTitle.setSelected(true);

                    songTitle.setSingleLine(true);
                }
                else{

                    songTitle.setEllipsize(TextUtils.TruncateAt.MARQUEE);  //setting marquee

                    songTitle.setText(title);   //if no extra information is with title

                    songTitle.setSelected(true);

                    songTitle.setSingleLine(true);
                }
            }
            else{
                songTitle.setEllipsize(TextUtils.TruncateAt.MARQUEE);  //setting marquee

                songTitle.setText(al.get(position).getName().replace(".mp3","").replace(".wav","")); //default title

                songTitle.setSelected(true);

                songTitle.setSingleLine(true);
            }

            albumName = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM); //album name

            artistName = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST); //artist name

            if ((albumName != null) && (artistName != null)) {

                albumName_artistName = albumName.concat("- ").concat(artistName); //album name-artist name

                songAlbum.setEllipsize(TextUtils.TruncateAt.MARQUEE);  //setting marquee

                songAlbum.setText(albumName_artistName); //setting album name-artist name

                songAlbum.setSelected(true);

                songAlbum.setSingleLine(true);
            }
            else {
                if ((albumName == null) && (artistName == null)) {

                    songAlbum.setText("Unknown Album");
                } else if (albumName == null) {

                    songAlbum.setText("Artist- " + artistName);
                } else {

                    songAlbum.setText(albumName);
                }
            }

            if (mp == null) {

                mp = MediaPlayer.create(this, Uri.parse(al.get(position).getAbsolutePath()));

                music.setMax(mp.getDuration());

                totalTime.setText(getTimeString(mp.getDuration()));

                mp.start();
            }
            if(mp.isPlaying()){

                pl.setImageDrawable(getDrawable(R.drawable.pause));
            }

        }
        catch (Exception e){

            Toast.makeText(this,"File could not be opened",Toast.LENGTH_SHORT).show();

            finish();
        }
    }

    public void previous(View v){

        if(mp!=null) {

            mp.stop();         //stopping current song

            mp = null;
        }
        position=(position-1);  //decrementing position

        if(position<0){

            position=al.size()-1;   //resetting position to last index
        }

                //Retrieving media metadata
        try {
            metadataRetriever.setDataSource(al.get(position).getAbsolutePath());

            art = metadataRetriever.getEmbeddedPicture();

            if (art != null) {

                Bitmap songImage = BitmapFactory.decodeByteArray(art, 0, art.length);  //getting album art
                Bitmap blr = createBlurBitmap(songImage, 25);
                Bitmap blr1 = createBlurBitmap(blr, 25);
                Bitmap blr2 = createBlurBitmap(blr1, 25);
                Bitmap blr3 = createBlurBitmap(blr2, 25);
                albumArt.setImageBitmap(songImage);     //setting album art
                ivbg.setImageBitmap(blr3);
            }
            else {

                albumArt.setImageDrawable(getDrawable(R.drawable.defaultalbumart)); //default album art
                ivbg.setImageBitmap(null);
                ivbg.setBackgroundColor(Color.BLACK);
            }
            title=metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE); //getting title of song

            if(title!=null) {

                index=title.lastIndexOf(searchExtraInfo);

                if(index!=-1){

                    extraInfo=title.substring(index);     //searching extra information in title

                    songTitle.setEllipsize(TextUtils.TruncateAt.MARQUEE);  //setting marquee

                    songTitle.setText(title.replace(extraInfo,""));   //setting title of the song and removing extra information

                    songTitle.setSelected(true);

                    songTitle.setSingleLine(true);
                }
                else{
                    songTitle.setEllipsize(TextUtils.TruncateAt.MARQUEE);  //setting marquee

                    songTitle.setText(title);   //if no extra information is with title

                    songTitle.setSelected(true);

                    songTitle.setSingleLine(true);
                }
            }
            else{
                songTitle.setEllipsize(TextUtils.TruncateAt.MARQUEE);  //setting marquee

                songTitle.setText(al.get(position).getName().replace(".mp3","").replace(".wav","")); //default title

                songTitle.setSelected(true);

                songTitle.setSingleLine(true);
            }
            albumName = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM); //album name

            artistName = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST); //artist name

            if ((albumName != null) && (artistName != null)) {

                albumName_artistName = albumName.concat("- ").concat(artistName); //album name-artist name

                songAlbum.setEllipsize(TextUtils.TruncateAt.MARQUEE);  //setting marquee

                songAlbum.setText(albumName_artistName);  //setting album name-artist name

                songAlbum.setSelected(true);

                songAlbum.setSingleLine(true);
            }
            else {
                if ((albumName == null) && (artistName == null)) {

                    songAlbum.setText("Unknown Album");
                } else if (albumName == null) {

                    songAlbum.setText("Artist- " + artistName);
                } else {

                    songAlbum.setText(albumName);
                }
            }

            if (mp == null) {

                mp = MediaPlayer.create(this, Uri.parse(al.get(position).getAbsolutePath()));

                music.setMax(mp.getDuration());

                totalTime.setText(getTimeString(mp.getDuration()));

                mp.start();
            }
            if(mp.isPlaying()){

                pl.setImageDrawable(getDrawable(R.drawable.pause));
            }
        }
        catch (Exception e){{

            Toast.makeText(this,"File could not be opened",Toast.LENGTH_SHORT).show();

            finish();
        }
            }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(mp!=null) {
            mp.stop();
        }
    }

    private Bitmap createBlurBitmap(Bitmap src, float r) {
        if (r <= 0) {
            r = 0.1f;
        } else if (r > 25) {
            r = 25.0f;
        }
        Bitmap bitmap = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Bitmap.Config.ARGB_8888);
        RenderScript renderScript = RenderScript.create(this);
        Allocation blurInput = Allocation.createFromBitmap(renderScript, src);
        Allocation blurOutput = Allocation.createFromBitmap(renderScript, bitmap);
        ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));
        blur.setInput(blurInput);
        blur.setRadius(r);
        blur.forEach(blurOutput);
        blurOutput.copyTo(bitmap);
        renderScript.destroy();
        return bitmap;
    }
}