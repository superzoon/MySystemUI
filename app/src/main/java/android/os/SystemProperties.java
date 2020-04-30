/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.os;

import android.util.Log;
import android.util.MutableInt;


import java.util.ArrayList;
import java.util.HashMap;


/**
 * Gives access to the system properties store.  The system properties
 * store contains a list of string key-value pairs.
 *
 * {@hide}
 */
public class SystemProperties {

    public static String get(String key) {
        return "";
    }

    public static String get(String key, String def) {
        return def;
    }

    public static int getInt(String key, int def) {
        return def;
    }

    public static long getLong(String key, long def) {
        return def;
    }

    public static boolean getBoolean(String key, boolean def) {
        String s = def?"1":"0";
        return def;
    }

    /**
     * Set the value for the given key.
     * @throws IllegalArgumentException if the value exceeds 92 characters
     */
    public static void set(String key, String val) {
    }

    public static void addChangeCallback(Runnable callback) {
    }

    static void callChangeCallbacks() {
    }


    public static void reportSyspropChanged() {

    }
}
