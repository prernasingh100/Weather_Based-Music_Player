package com.example.user.sdcard1.service;

import com.example.user.sdcard1.data.Channel;

/**
 * Created by user on 20-03-2017.
 */
public interface WeatherServiceCallback {

    void serviceSuccess(Channel channel);

    void serviceSuccessForToggle(Channel channel);

    void serviceFailure(Exception exception);
}
