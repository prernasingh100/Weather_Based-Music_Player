package com.example.user.sdcard1.data;

import org.json.JSONObject;

/**
 * Created by user on 20-03-2017.
 */
public class Condition implements JSONPopulator {

    private int code;
    private String description;

    public int getCode() {
        return code;
    }
    public String getDescription() {
        return description;
    }

    @Override
    public void populate(JSONObject data) {

        code=data.optInt("code");
        description=data.optString("text");
    }
}
