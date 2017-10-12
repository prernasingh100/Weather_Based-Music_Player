package com.example.user.sdcard1;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Created by user on 06-04-2017.
 */
public class CustomAdapter extends ArrayAdapter<String> {

    private String albumArtist[];
    private Context context;
    private Typeface roboto_bold;
    private Typeface roboto_light;
    private TextView title;
    private TextView album;

    public CustomAdapter(Context context, String songs[],String albumArtist[]) {
        super(context,R.layout.naminglist,songs);
        this.context=context;
        this.albumArtist=albumArtist;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View customView = inflater.inflate(R.layout.naminglist, parent, false);
        String filename = getItem(position);
        title = (TextView) customView.findViewById(R.id.song_name);
        title.setText(filename); //setting filename of song in listview
        album = (TextView) customView.findViewById(R.id.album_name);

        roboto_bold = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Bold.ttf");
        roboto_light = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Light.ttf");

        title.setTypeface(roboto_bold);  //Font style of filename of song in listview
        title.setTextColor(Color.parseColor("#ddddec"));   //Font colour of filename
        title.setTextSize(20); //Font size of filename
        album.setText(albumArtist[position]);  //setting album of song in listview
        album.setTypeface(roboto_light);   //Font style of album of song in listview
        album.setTextColor(Color.parseColor("#c6c7d4"));    //Font colour of album
        album.setTextSize(18);     //Font size of album
        return customView;
    }
}
