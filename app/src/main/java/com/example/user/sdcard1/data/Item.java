package com.example.user.sdcard1.data;

import org.json.JSONObject;

/**
 * Created by user on 20-03-2017.
 */
public class Item implements JSONPopulator {

    private Condition condition;

    public Condition getCondition() {
        return condition;
    }

    @Override
    public void populate(JSONObject data) {

        condition=new Condition();

        condition.populate(data.optJSONObject("condition"));

    }
}
