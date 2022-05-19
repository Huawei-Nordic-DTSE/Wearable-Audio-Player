package com.hmos.audio.player.utils;

import com.hmos.audio.player.model.Audio;
import ohos.app.Context;
import ohos.global.resource.BaseFileDescriptor;
import ohos.media.common.Source;
import ohos.media.player.Player;

import java.io.IOException;

public class PlayerManager {

    private static final int MICRO_MILLI_RATE = 1000;
    private static PlayerManager instance = new PlayerManager();
    BaseFileDescriptor assetfd = null;
    public Player player;
    private Context context;
    private boolean isInitialized = false;
    private Audio playingBook;
    private int currentIndex;

    public static PlayerManager getInstance(){
        return instance;
    }

    // Initialize the instance.
    public synchronized void setup(Context context, Audio playingBook) {
        if(context == null) {
            return;
        }

        if (isInitialized) {
            return;
        } else {
            this.context = context;
            this.playingBook = playingBook;
            this.isInitialized = true;
        }
        player = new Player(this.context);
    }

    public synchronized boolean isPlaying() {
        return player.isNowPlaying();
    }

    public synchronized Player getPlayer() {
        return player;
    }

    public synchronized Audio getBook(){
        return playingBook;
    }

    public synchronized void setBook(Audio book){
        this.playingBook = book;
    }

    public synchronized int getCurrentIndex() {
        return this.currentIndex;
    }

    public synchronized void setCurrentIndex(int currentIndex) {
        this.currentIndex = currentIndex;
    }

    public synchronized boolean play(String filePath, Player.IPlayerCallback iPlayerCallback, int startMillisecond) {
        if (!isInitialized) {
            return false;
        }

        if (player != null) {
            if (isPlaying()) {
                player.stop();
            }
            player.release();
        }

        player = new Player(context);
        player.setPlayerCallback(iPlayerCallback);

        if(filePath.startsWith("http")){
            if (!player.setSource(new Source(filePath))) {
                return false;
            }
        } else {
            try {
                String fullPath = "entry/resources/rawfile/" + filePath;
                assetfd = this.context.getResourceManager().getRawFileEntry(fullPath).openRawFileDescriptor();
            } catch (IOException e) {
                return false;
            }

            if (!player.setSource(assetfd)) {
                return false;
            }
        }

        if (!player.prepare()) {
            return false;
        }

        if (startMillisecond > 0) {
            int microsecond = startMillisecond * MICRO_MILLI_RATE;
            if (!player.rewindTo(microsecond)) {
                return false;
            }
        }

        if (player.play()) {
            return true;
        } else {
            return false;
        }
    }

    // Declare the playback pause method.
    public synchronized void pause() {
        if (player == null) {
            return;
        }
        if (isPlaying()) {
            player.pause();
        }
    }

    // Declare the playback resuming method.
    public synchronized void resume() {
        if (player == null) {
            return;
        }
        if (!isPlaying()) {
            player.play();
        }
    }

    // Obtain the total duration of the current music.
    public synchronized int getAudioDuration() {
        if (player == null) {
            return 0;
        }
        return player.getDuration();
    }

    // Obtain the current music playback position.
    public synchronized int getAudioCurrentPosition() {
        if (player == null ) {
            return 0;
        }
        return player.getCurrentTime();
    }

    // Change playback speed
    public synchronized float increasePlaybackSpeed() {
        float curSpeed = player.getPlaybackSpeed();
        if(curSpeed <3.0) {
            player.setPlaybackSpeed(curSpeed + 0.1f);
        }
        return player.getPlaybackSpeed();
    }

    public synchronized float decreasePlaybackSpeed() {
        float curSpeed = player.getPlaybackSpeed();
        if(curSpeed > 0.5) {
            player.setPlaybackSpeed(curSpeed - 0.1f);
        }
        return player.getPlaybackSpeed();
    }

    public synchronized float getPlaybackSpeed() {
        return player.getPlaybackSpeed();
    }

    public synchronized Boolean setPlaybackSpeed(float speed) {
        return player.setPlaybackSpeed(speed);
    }

    // Change playback position
    public synchronized void skipForward() {
        int currentPosition = player.getCurrentTime();
        int duration = player.getDuration();
        int skipDuration = 30000; //30s

        if((duration - currentPosition) > skipDuration){
            player.rewindTo((currentPosition + skipDuration) * MICRO_MILLI_RATE);
        }
    }

    public synchronized void skipBackward() {
        int currentPosition = getAudioCurrentPosition();
        int skipDuration = 30000; //30s

        if(currentPosition > skipDuration){
            player.rewindTo((currentPosition - skipDuration) * MICRO_MILLI_RATE);
        } else {
            player.rewindTo(0);
        }
    }
}
