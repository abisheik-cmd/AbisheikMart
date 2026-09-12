package com.abisheikmart.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.Reader;
import java.lang.reflect.Type;

public class JsonUtil {

    private static final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .serializeNulls()
            .create();

    private JsonUtil() {}

    public static String toJson(Object src) {
        return gson.toJson(src);
    }

    public static <T> T fromJson(String json, Class<T> classOfT) {
        return gson.fromJson(json, classOfT);
    }

    public static <T> T fromJson(String json, Type typeOfT) {
        return gson.fromJson(json, typeOfT);
    }

    public static <T> T fromJson(Reader reader, Class<T> classOfT) {
        return gson.fromJson(reader, classOfT);
    }

    public static <T> T fromJson(Reader reader, Type typeOfT) {
        return gson.fromJson(reader, typeOfT);
    }
}
