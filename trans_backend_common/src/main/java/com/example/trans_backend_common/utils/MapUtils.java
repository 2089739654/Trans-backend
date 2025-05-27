package com.example.trans_backend_common.utils;

import java.util.Iterator;
import java.util.Map;

public class MapUtils {

    public static void transfer(Map<String,Object> map){
        Iterator<Map.Entry<String, Object>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Object> entry = iterator.next();
            if (entry.getValue() == null) {
                iterator.remove();
            }
        }
    }

}
