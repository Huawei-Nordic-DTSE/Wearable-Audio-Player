package com.hmos.audio.player.utils;

import com.hmos.audio.player.model.Audio;
import ohos.app.Context;
import ohos.utils.zson.ZSONReader;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

public class AudioBookHelper {

    private static final String OBJECT_NAME = "books";
    private static final String BOOK_LIST_URL = "https://api.npoint.io/8e7c7d6f8a2cbf462f25";
    private static final String CHARACTER_SET = "gbk";

    public static ArrayList<Audio> retrieveAudioBooks(Context context) {

        try {
            HttpGet getBooksInfo = new HttpGet(BOOK_LIST_URL, CHARACTER_SET, null);
            String response = getBooksInfo.finish();

            ZSONReader zsonReader = new ZSONReader(new StringReader(response));
            zsonReader.startObject();
            ArrayList<Audio> books = new ArrayList<>();

            if (OBJECT_NAME.equals(zsonReader.readName())) {
                zsonReader.startArray();

                while (zsonReader.hasNext()) {
                    Audio book = new Audio();
                    zsonReader.read(book);
                    books.add(book);
                }
                zsonReader.endArray();
            }
            zsonReader.endObject();
            return books;

        } catch (IOException e) {
            throw new RuntimeException("Failed downloading book list.", e);
        }
    }
}
