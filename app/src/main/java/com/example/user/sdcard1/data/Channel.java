package com.example.user.sdcard1.data;

import org.json.JSONObject;

/**
 * Created by user on 20-03-2017.
 */
public class Channel implements JSONPopulator {

    private Item item;
    private Location location;

    public Location getLocation() {return location;}

    public Item getItem() {
        return item;
    }

    @Override
    public void populate(JSONObject data) {

        item=new Item();
        item.populate(data.optJSONObject("item"));

        location=new Location();
        location.populate(data.optJSONObject("location"));

    }
}
