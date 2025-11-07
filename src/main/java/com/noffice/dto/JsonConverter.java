/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.noffice.dto;

import com.google.gson.*;
import org.apache.log4j.Logger;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Chuyển đỗi giữa json và object dữ liệu
 *
 * @author viettel
 * @param <T>
 */
public class JsonConverter<T> {

    private static final Logger LOGGER = Logger.getLogger(JsonConverter.class);
    private static final Map<Class<?>, Type> REGISTER_LISTTYPES = new HashMap<>();
    /**
     * .serializeNulls() .excludeFieldsWithoutExposeAnnotation()
     */
    // hin - VBH
    public static final Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
    //public static final Gson gson = new GsonBuilder().serializeNulls().excludeFieldsWithoutExposeAnnotation()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").registerTypeAdapter(Double.class, new JsonSerializer<Double>() {
        // Chuyển double sang integer
        @Override
        public JsonElement serialize(Double src, Type typeOfSrc, JsonSerializationContext context) {
            if (src == src.longValue()) {
                return new JsonPrimitive(src.longValue());
            }
            return new JsonPrimitive(src);
        }
    }).create();

  

   

    /**
     * Chuyển dữ liệu T sang json
     *
     * @param <T>
     * @param data
     * @return chuỗi json
     */
    public static <T> String serialize(T data) {
        if (data == null) {
            return null;
        }
        return gson.toJson(data);
    }

    public static <T> T fromJson(String jsonData, Class<T> t) {
        if (jsonData == null) {
            return null;
        }
        try {
            return gson.fromJson(jsonData, t);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }

    }

    public static <T> List<T> toList(String json, Class<T> typeClass) {
        try {
            Type type = REGISTER_LISTTYPES.get(typeClass);
            return gson.fromJson(json, type);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return new ArrayList<>();
        }

    }

}
