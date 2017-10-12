package com.example.user.sdcard1.data;

import org.json.JSONObject;

/**
 * Created by user on 02-04-2017.
 */
public class Location implements JSONPopulator {

        private String city;
        private String region;

    public String getRegion() {
        return region;
    }

    public String getCity() {
        return city;
    }

    @Override
    public void populate(JSONObject data) {

        city=data.optString("city");
        region=data.optString("region");

    }
}
