package com.example.user.sdcard1;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by user on 02-04-2017.
 */
public interface FileHandlerCallback {

    void readSuccess(ArrayList<File> files);

    void readFailure(Exception exception);

    void noSongsRead(Exception exception);

    void mediaUnmounted(Exception exception);
}
