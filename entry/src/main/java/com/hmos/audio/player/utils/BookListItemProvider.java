package com.hmos.audio.player.utils;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.CircleBitmapDisplayer;
import com.hmos.audio.player.ResourceTable;
import com.hmos.audio.player.model.Audio;
import ohos.aafwk.ability.AbilitySlice;
import ohos.agp.components.*;
import ohos.agp.utils.Color;

import java.util.List;

public class BookListItemProvider extends BaseItemProvider {

    private List<Audio> bookList;
    private AbilitySlice slice;
    private DisplayImageOptions options;

    public BookListItemProvider(List<Audio> bookList, AbilitySlice slice) {
        this.bookList = bookList;
        this.slice = slice;

        ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(slice);
        config.threadPriority(Thread.NORM_PRIORITY - 2);
        config.denyCacheImageMultipleSizesInMemory();
        config.diskCacheFileNameGenerator(new Md5FileNameGenerator());
        config.diskCacheSize(50 * 1024 * 1024); // 50 MB
        config.tasksProcessingOrder(QueueProcessingType.LIFO);
        config.writeDebugLogs();

        ImageLoader.getInstance().init(config.build());

        options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .displayer(new CircleBitmapDisplayer(Color.WHITE.getValue(), 90))
                .build();
    }

    @Override
    public int getCount() {
        return bookList == null ? 0 : bookList.size();
    }

    @Override
    public Object getItem(int position) {
        if (bookList != null
                && position >= 0
                && position < bookList.size()){
            return bookList.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Component getComponent(int position, Component convertComponent, ComponentContainer componentContainer) {

        final Component cpt;

        if (convertComponent == null) {
            cpt = LayoutScatter.getInstance(slice)
                    .parse(ResourceTable.Layout_list_item, null, false);
        } else {
            cpt = convertComponent;
        }

        Audio audioBook = bookList.get(position);
        Text text = (Text) cpt.findComponentById(ResourceTable.Id_text_station_name);
        Image logo = (Image) cpt.findComponentById(ResourceTable.Id_img_station_logo);

        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(audioBook.getCover(), logo, options);

        text.setText(audioBook.getTitle());
        return cpt;
    }
}
