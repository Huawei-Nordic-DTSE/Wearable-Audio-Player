package com.hmos.audio.player.utils;

import ohos.app.Context;
import ohos.data.DatabaseHelper;
import ohos.data.preferences.Preferences;

import java.util.ArrayList;
import java.util.List;

public class PrefDB {

    private final String DB_FILENAME = "listen_progress";

    private Context context;
    private DatabaseHelper databaseHelper;
    private Preferences preferences;

    public PrefDB(Context context) {
        this.context = context;
        databaseHelper = new DatabaseHelper(context);
        preferences = databaseHelper.getPreferences(DB_FILENAME);
    }

    public List<Integer> getProgress(String key){
        String [] progress = preferences.getString(key, "1:0").split(":");
        int currentChapter = Integer.valueOf(progress[0]);
        int currentPosition = Integer.valueOf(progress[1]);
        List<Integer> progressList = new ArrayList<>();
        progressList.add(currentChapter);
        progressList.add(currentPosition);
        return progressList;
    }

    public void setProgress(String id, String chapter, String position){
        preferences.putString(id, chapter + ":" + position);
        preferences.flushSync();
    }

    public void clearDB(){
        preferences.clear();
        preferences.flush();
    }

}

