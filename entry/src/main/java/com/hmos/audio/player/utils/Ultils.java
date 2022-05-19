package com.hmos.audio.player.utils;

import java.util.concurrent.TimeUnit;

public class Ultils {

    public static String getTime(long timeMilliSecond) {
        return String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(timeMilliSecond),
                TimeUnit.MILLISECONDS.toMinutes(timeMilliSecond) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(timeMilliSecond)),
                TimeUnit.MILLISECONDS.toSeconds(timeMilliSecond) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeMilliSecond)));
    }
}
