package com.hmos.audio.player.slice;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.CircleBitmapDisplayer;
import com.hmos.audio.player.ResourceTable;
import com.hmos.audio.player.model.Audio;
import com.hmos.audio.player.utils.Constants;
import com.hmos.audio.player.utils.LogUtil;
import com.hmos.audio.player.utils.PlayerManager;
import com.hmos.audio.player.utils.Ultils;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.IntentParams;
import ohos.agp.components.*;
import ohos.agp.utils.Color;
import ohos.agp.utils.LayoutAlignment;
import ohos.agp.window.dialog.ToastDialog;
import ohos.app.dispatcher.task.TaskPriority;
import ohos.media.audio.AudioManager;
import ohos.media.audio.AudioRemoteException;
import ohos.media.player.Player;

import java.util.*;

public class PlayerSlice extends AbilitySlice implements Component.ClickedListener {

    private final static String PARAM_KEY= "SELECTED_BOOK";
    private final static String TAG = "PlayerSlice_TAG";

    private RoundProgressBar playerProgressBar;
    private Text currentSpeed, currentPosition, duration, currentChapter, bookTitle;

    private List<String> audioFiles = new ArrayList<>();
    private int currentIndex = 0;
    private int currentTime;
    private Image playstopBtn;
    private ProgressBar volumeProgressBar;
    private PlayerManager mPlayer;
    private AudioManager audioManager;
    private Audio mAudioBook;

    private Timer timer;


    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_player);

        IntentParams intentParams = intent.getParams();
        mAudioBook = (Audio) intentParams.getParam(PARAM_KEY);
        String str[] = mAudioBook.getAudioFiles().split(",");
        audioFiles = Arrays.asList(str);

        List<Integer> currentProgress = Constants.getPrefDB(this).getProgress(mAudioBook.getId());
        currentIndex = currentProgress.get(0) - 1;
        currentTime = currentProgress.get(1);

        initComponents();

        // Start playing
        playstopBtn.setPixelMap(ResourceTable.Media_ic_pause_button);

        if(!mPlayer.isPlaying()){
            getGlobalTaskDispatcher(TaskPriority.DEFAULT).asyncDispatch(() -> mPlayer.play(audioFiles.get(currentIndex),
                    iPlayerCallback,currentTime));
        } else {
            if(!mPlayer.getBook().getAudioFiles().equals(mAudioBook.getAudioFiles())){
                mPlayer.setBook(mAudioBook);
                mPlayer.setPlaybackSpeed(1.0f);
                currentSpeed.setText(String.format(Locale.US,"x%.1f",
                        Math.abs(mPlayer.getPlaybackSpeed())));
                getGlobalTaskDispatcher(TaskPriority.DEFAULT).asyncDispatch(() -> mPlayer.play(audioFiles.get(currentIndex),
                        iPlayerCallback,currentTime));
            } else {
                currentIndex = mPlayer.getCurrentIndex();
                currentChapter.setText(String.format("%d/%d", currentIndex + 1, audioFiles.size()));
            }
        }

        startTimer();
    }

    private void initComponents() {
        bookTitle = (Text) findComponentById(ResourceTable.Id_station_name);
        bookTitle.setText(mAudioBook.getTitle());
        bookTitle.startAutoScrolling();

        currentChapter = (Text) findComponentById(ResourceTable.Id_current_chapter);
        currentChapter.setText(String.format("%d/%d", currentIndex + 1, audioFiles.size()));
        Image previousChapter = (Image) findComponentById(ResourceTable.Id_img_prechapter);
        Image nexChapter = (Image) findComponentById(ResourceTable.Id_img_nextchapter);
        previousChapter.setClickedListener(this);
        nexChapter.setClickedListener(this);

        playerProgressBar = (RoundProgressBar) findComponentById(ResourceTable.Id_round_progress_bar);

        Image imgVolIncrease = (Image) findComponentById(ResourceTable.Id_img_vol_increase);
        Image imgVolDecrease = (Image) findComponentById(ResourceTable.Id_img_vol_decrease);
        imgVolIncrease.setClickedListener(this);
        imgVolDecrease.setClickedListener(this);

        currentSpeed = (Text) findComponentById(ResourceTable.Id_txt_speed);
        Image speedIncrease = (Image) findComponentById(ResourceTable.Id_btn_plusSpeed);
        Image speedDecrease = (Image) findComponentById(ResourceTable.Id_btn_minusSpeed);
        speedIncrease.setClickedListener(this);
        speedDecrease.setClickedListener(this);

        duration = (Text) findComponentById(ResourceTable.Id_txt_duration);
        currentPosition = (Text) findComponentById(ResourceTable.Id_txt_currentTime);

        Image skipFwd = (Image) findComponentById(ResourceTable.Id_btn_skipfwd);
        Image skipBwd = (Image) findComponentById(ResourceTable.Id_btn_skipback);
        skipFwd.setClickedListener(this);
        skipBwd.setClickedListener(this);

        playstopBtn = (Image) findComponentById(ResourceTable.Id_img_pause_resume_btn);
        playstopBtn.setClickedListener(this);

        volumeProgressBar = (ProgressBar) findComponentById(ResourceTable.Id_progressbar_volume);

        audioManager = new AudioManager(this);

        mPlayer = PlayerManager.getInstance();
        mPlayer.setup(this, mAudioBook);

        try {
            int currentVol = audioManager.getVolume(AudioManager.AudioVolumeType.STREAM_MUSIC);
            volumeProgressBar.setProgressValue(currentVol);
        } catch (AudioRemoteException e) {
            e.printStackTrace();
        }

        Image background = (Image) findComponentById(ResourceTable.Id_img_background);
        ImageLoader imageLoader = ImageLoader.getInstance();
        ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(this);
        config.threadPriority(Thread.NORM_PRIORITY - 2);
        config.denyCacheImageMultipleSizesInMemory();
        config.diskCacheFileNameGenerator(new Md5FileNameGenerator());
        config.diskCacheSize(50 * 1024 * 1024); // 50 MB
        config.tasksProcessingOrder(QueueProcessingType.LIFO);
        config.writeDebugLogs();

        ImageLoader.getInstance().init(config.build());
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .displayer(new CircleBitmapDisplayer(Color.WHITE.getValue(), 90))
                .build();
        imageLoader.displayImage(mAudioBook.getCover(), background, options);
    }

    @Override
    public void onClick(Component component) {
        switch (component.getId()) {
            case ResourceTable.Id_img_pause_resume_btn:
                if (mPlayer.isPlaying()) {
                    playstopBtn.setPixelMap(ResourceTable.Media_ic_play_button);
                    mPlayer.pause();
                    stopTimer();
                } else {
                    playstopBtn.setPixelMap(ResourceTable.Media_ic_pause_button);
                    mPlayer.resume();
                    startTimer();
                }
                break;

            case ResourceTable.Id_img_vol_increase:
                audioManager.changeVolumeBy(AudioManager.AudioVolumeType.STREAM_MUSIC,1);
                try {
                    int increasedVol = audioManager.getVolume(AudioManager.AudioVolumeType.STREAM_MUSIC);
                    volumeProgressBar.setProgressValue(increasedVol);
                } catch (AudioRemoteException e) {
                    e.printStackTrace();
                }
                break;

            case ResourceTable.Id_img_vol_decrease:
                audioManager.changeVolumeBy(AudioManager.AudioVolumeType.STREAM_MUSIC,-1);
                try {
                    int decreasedVol = audioManager.getVolume(AudioManager.AudioVolumeType.STREAM_MUSIC);
                    volumeProgressBar.setProgressValue(decreasedVol);
                } catch (AudioRemoteException e) {
                    e.printStackTrace();
                }
                break;

            case ResourceTable.Id_btn_plusSpeed:
                float newIncSpeed = mPlayer.increasePlaybackSpeed();
                currentSpeed.setText(String.format(Locale.US,"x%.1f", newIncSpeed));
                break;

            case ResourceTable.Id_btn_minusSpeed:
                float newDecSpeed = mPlayer.decreasePlaybackSpeed();
                currentSpeed.setText(String.format(Locale.US,"x%.1f", newDecSpeed));
                break;

            case ResourceTable.Id_btn_skipfwd:
                mPlayer.skipForward();
                break;

            case ResourceTable.Id_btn_skipback:
                mPlayer.skipBackward();
                break;

            case ResourceTable.Id_img_nextchapter:
                next();
                playstopBtn.setPixelMap(ResourceTable.Media_ic_pause_button);
                currentChapter.setText(String.format("%d/%d", currentIndex + 1, audioFiles.size()));
                getGlobalTaskDispatcher(TaskPriority.DEFAULT).asyncDispatch(()
                        -> mPlayer.play(audioFiles.get(currentIndex),
                        iPlayerCallback,0));
                break;

            case ResourceTable.Id_img_prechapter:
                previous();
                playstopBtn.setPixelMap(ResourceTable.Media_ic_pause_button);
                currentChapter.setText(String.format("%d/%d", currentIndex + 1, audioFiles.size()));
                getGlobalTaskDispatcher(TaskPriority.DEFAULT).asyncDispatch(()
                        -> mPlayer.play(audioFiles.get(currentIndex),
                        iPlayerCallback,0));
                break;
        }
    }

    public void previous() {
        currentIndex--;
        if (currentIndex < 0) {
            currentIndex = audioFiles.size() - 1;
        }
        PlayerManager.getInstance().setCurrentIndex(currentIndex);
    }

    public void next() {
        currentIndex++;
        if (currentIndex >= audioFiles.size()) {
            currentIndex = 0;
        }
        PlayerManager.getInstance().setCurrentIndex(currentIndex);
    }

    @Override
    public void onActive() {
        super.onActive();
    }

    @Override
    protected void onInactive() {
        super.onInactive();
        mPlayer.setCurrentIndex(currentIndex);

        if(!mPlayer.isPlaying()){
            String currentChapter = String.valueOf(currentIndex + 1);
            String currentPosition = String.valueOf(mPlayer.getAudioCurrentPosition());
            Constants.getPrefDB(this).setProgress(mAudioBook.getId(),
                    currentChapter, currentPosition);
        }
    }

    @Override
    public void onForeground(Intent intent) {
        super.onForeground(intent);
    }

    public Player.IPlayerCallback iPlayerCallback = new Player.IPlayerCallback() {
        @Override
        public void onPrepared() {
        }

        @Override
        public void onMessage(int i, int i1) {
        }

        @Override
        public void onError(int i, int i1) {
            getUITaskDispatcher().delayDispatch(() -> {
                playstopBtn.setPixelMap(ResourceTable.Media_ic_play_button);
                showToast("Can not play");
            }, 1);
        }

        @Override
        public void onResolutionChanged(int i, int i1) {
        }

        @Override
        public void onPlayBackComplete() {
            if(currentIndex + 1 == audioFiles.size()){
                getUITaskDispatcher().asyncDispatch(()
                        -> playstopBtn.setPixelMap(ResourceTable.Media_ic_play_button));
            } else {
                next();
                getUITaskDispatcher().asyncDispatch(()
                        -> currentChapter.setText(String.format("%d/%d", currentIndex + 1, audioFiles.size())));
                getGlobalTaskDispatcher(TaskPriority.DEFAULT).asyncDispatch(()
                        -> mPlayer.play(audioFiles.get(currentIndex),
                        iPlayerCallback,0));
            }
        }

        @Override
        public void onRewindToComplete() {
        }

        @Override
        public void onBufferingChange(int i) {
        }

        @Override
        public void onNewTimedMetaData(Player.MediaTimedMetaData mediaTimedMetaData) {
        }

        @Override
        public void onMediaTimeIncontinuity(Player.MediaTimeInfo mediaTimeInfo) {
        }
    };

    private void showToast(String mgs) {
        DirectionalLayout toastLayout = (DirectionalLayout) LayoutScatter.getInstance(this)
                .parse(ResourceTable.Layout_toast_dialog, null, false);

        if(toastLayout == null) {
            LogUtil.debug(TAG, "toastLayout is null");
        } else {
            Text mgsComp = (Text) toastLayout.getComponentAt(0);

            if (mgsComp == null) {
                LogUtil.debug(TAG, "mgsComp is null");
            } else {
                mgsComp.setText(mgs);
            }
        }

        ToastDialog toastDialog = new ToastDialog(getContext())
                .setComponent(toastLayout)
                .setSize(DirectionalLayout.LayoutConfig.MATCH_CONTENT, DirectionalLayout.LayoutConfig.MATCH_CONTENT)
                .setAlignment(LayoutAlignment.CENTER);

        toastDialog.setDuration(3000).show();
    }

    private void startTimer() {
        stopTimer();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                PlayerSlice.this.getApplicationContext().getUITaskDispatcher().delayDispatch(() -> {
                    int currentPos = mPlayer.getAudioCurrentPosition();
                    currentPosition.setText(Ultils.getTime(currentPos));
                    duration.setText("-" + Ultils.getTime(mPlayer.getAudioDuration() - currentPos));
                    float progressValue = ((float)mPlayer.getAudioCurrentPosition()/(float)mPlayer.getAudioDuration())*100.f;
                    playerProgressBar.setProgressValue((int)progressValue);
                }, 0);
            }
        }, 0, 1000);
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }
}
