package com.hmos.audio.player.utils;

import ohos.app.Context;

public class Constants {

    private static PrefDB mPrefDB;

    public static PrefDB getPrefDB(Context context){
        if(mPrefDB == null){
            mPrefDB = new PrefDB(context);
        }
        return mPrefDB;
    }

    public final static String PARAM_KEY= "SELECTED_BOOK";
    public final static int TOAST_DURATION_MS = 3000;
    public final static String TOAST_MSG_NO_INTERNET = "No internet available";
}

