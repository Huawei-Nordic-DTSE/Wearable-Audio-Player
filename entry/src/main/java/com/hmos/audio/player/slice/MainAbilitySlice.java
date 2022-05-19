package com.hmos.audio.player.slice;

import com.hmos.audio.player.ResourceTable;
import com.hmos.audio.player.model.Audio;
import com.hmos.audio.player.utils.*;
import com.github.ybq.core.style.Circle;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.IntentParams;
import ohos.agp.components.*;
import ohos.agp.utils.Color;
import ohos.agp.utils.LayoutAlignment;
import ohos.agp.window.dialog.ToastDialog;
import ohos.app.dispatcher.task.TaskPriority;
import ohos.net.NetHandle;
import ohos.net.NetManager;

import java.util.List;


public class MainAbilitySlice extends AbilitySlice {

    private final static String TAG = "MainAbilitySlice_TAG";

    private Image img_retry;
    private Text playing_book_name;
    private DirectionalLayout layout_playing_status;
    private ProgressBar progressBar;

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_main);

        progressBar = (ProgressBar) findComponentById(ResourceTable.Id_progress_bar);
        Circle circle = new Circle();
        circle.onBoundsChange( 0, 0, progressBar.getHeight(), progressBar.getHeight());
        circle.setComponent(progressBar);
        progressBar.setProgressElement(circle);
        progressBar.setIndeterminate(true);
        progressBar.addDrawTask((component, canvas) -> circle.drawToCanvas(canvas));

        playing_book_name = (Text) findComponentById(ResourceTable.Id_text_playing_book);
        layout_playing_status = (DirectionalLayout) findComponentById(ResourceTable.Id_layout_play_status);

        layout_playing_status.setClickedListener(component -> {
            Intent playerIntent = new Intent();
            IntentParams intentParams = new IntentParams();
            if(PlayerManager.getInstance() != null && PlayerManager.getInstance().getBook() != null){
                intentParams.setParam(Constants.PARAM_KEY, PlayerManager.getInstance().getBook());
                playerIntent.setParams(intentParams);
                present(new PlayerSlice(), playerIntent);
            }
        });

        img_retry = (Image)findComponentById(ResourceTable.Id_image_retry);
        img_retry.setClickedListener(component -> {
            if (isInternetAvailable()){
                initListContainer();
                img_retry.setVisibility(Component.HIDE);
            }
        });

        if (isInternetAvailable()){
            initListContainer();
        } else {
            img_retry.setVisibility(Component.VISIBLE);
        }
    }

    private void initListContainer() {
        ListContainer listContainer = (ListContainer) findComponentById(ResourceTable.Id_list_container);
        progressBar.setVisibility(Component.VISIBLE);

        getGlobalTaskDispatcher(TaskPriority.DEFAULT).asyncDispatch(() -> {
            List<Audio> list = AudioBookHelper.retrieveAudioBooks(MainAbilitySlice.this);

            getUITaskDispatcher().delayDispatch(() -> {
                BookListItemProvider bookListItemProvider = new BookListItemProvider(list,
                        MainAbilitySlice.this);
                listContainer.setItemProvider(bookListItemProvider);
                progressBar.setVisibility(Component.HIDE);
            }, 1);

        });

        listContainer.enableScrollBar(Component.AXIS_Y, true);
        listContainer.setScrollbarBackgroundColor(Color.GRAY);
        listContainer.setScrollbarColor(Color.WHITE);

        //Enable scrolling with crown button
        listContainer.setFocusable(Component.FOCUS_ADAPTABLE);
        listContainer.requestFocus();

        //Set oval mode
        listContainer.setMode(Component.OVAL_MODE);

        //Adjust the size and position of the scrollbar
        listContainer.setScrollbarStartAngle(-25f);
        listContainer.setScrollbarSweepAngle(50f);

        // Drag down list to reload book list
        listContainer.setDraggedListener(Component.DRAG_DOWN, new Component.DraggedListener() {
            @Override
            public void onDragDown(Component component, DragInfo dragInfo) {
            }

            @Override
            public void onDragStart(Component component, DragInfo dragInfo) {
            }

            @Override
            public void onDragUpdate(Component component, DragInfo dragInfo) {
            }

            @Override
            public void onDragEnd(Component component, DragInfo dragInfo) {
                initListContainer();
            }

            @Override
            public void onDragCancel(Component component, DragInfo dragInfo) {
            }
        });

        listContainer.setItemClickedListener((container, component, position, id) -> {
            if(isInternetAvailable()){
                Intent playerIntent = new Intent();
                IntentParams intentParams = new IntentParams();
                intentParams.setParam(Constants.PARAM_KEY, container.getItemProvider().getItem(position));
                playerIntent.setParams(intentParams);
                present(new PlayerSlice(), playerIntent);
            }
        });
    }

    private boolean isInternetAvailable() {
        NetManager netManager = NetManager.getInstance(this);
        NetHandle[] netHandles = netManager.getAllNets();

        if (netHandles.length < 1) {
            showToast();
            return false;
        }

        return true;
    }

    private void showToast() {
        DirectionalLayout toastLayout = (DirectionalLayout) LayoutScatter.getInstance(this)
                .parse(ResourceTable.Layout_toast_dialog, null, false);

        if(toastLayout == null) {
            LogUtil.debug(TAG, "toastLayout is null");
        } else {
            Text mgsComp = (Text) toastLayout.getComponentAt(0);

            if (mgsComp == null) {
                LogUtil.debug(TAG, "mgsComp is null");
            } else {
                mgsComp.setText(Constants.TOAST_MSG_NO_INTERNET);
            }
        }

        ToastDialog toastDialog = new ToastDialog(getContext())
                .setComponent(toastLayout)
                .setSize(DirectionalLayout.LayoutConfig.MATCH_CONTENT, DirectionalLayout.LayoutConfig.MATCH_CONTENT)
                .setAlignment(LayoutAlignment.CENTER);

        toastDialog.setDuration(Constants.TOAST_DURATION_MS).show();
    }

    @Override
    public void onActive() {
        super.onActive();

        // Show playing book title at the bottom of watch screen if player is being played
        if(PlayerManager.getInstance() != null && PlayerManager.getInstance().getPlayer() != null){
            if(PlayerManager.getInstance().isPlaying()) {
                layout_playing_status.setVisibility(Component.VISIBLE);
                playing_book_name.setText(PlayerManager.getInstance().getBook().getTitle());
                playing_book_name.setAutoScrollingCount(Text.AUTO_SCROLLING_FOREVER);
                playing_book_name.startAutoScrolling();
            } else {
                layout_playing_status.setVisibility(Component.HIDE);
            }
        }
    }

    @Override
    protected void onInactive() {
        super.onInactive();

        // Save playing progress if player is playing
        if(PlayerManager.getInstance() != null && PlayerManager.getInstance().getPlayer() != null){
            if(PlayerManager.getInstance().isPlaying()) {
                String bookId = PlayerManager.getInstance().getBook().getId();
                String currentChapter = String.valueOf(PlayerManager.getInstance().getCurrentIndex() + 1);
                String currentPosition = String.valueOf(PlayerManager.getInstance().getAudioCurrentPosition());
                Constants.getPrefDB(this).setProgress(bookId, currentChapter, currentPosition);
            }
        }
    }

    @Override
    public void onForeground(Intent intent) {
        super.onForeground(intent);
    }
}