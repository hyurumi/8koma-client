package com.appspot.hachiko_schedule.util;

import java.util.Map;

public class MapUtils {

    /**
     * Key, ValueともにNot nullなら、mapに追加、そうでなければ単に無視
     */
    public static <K, V> void putOrIgnoreNull(Map<K, V> map, K key, V value) {
        if (key != null && value != null) {
            map.put(key, value);
        }
    }
}
