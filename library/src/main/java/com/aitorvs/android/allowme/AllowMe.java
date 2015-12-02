package com.aitorvs.android.allowme;

/*
 * Copyright (C) 02/12/15 aitorvs
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

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AllowMe {

    private static AllowMe instance;

    private Activity mActivity;
    private final Map<String, ArrayList<AllowMeCallback>> mRequestList;

    private AllowMe() {
        mRequestList = new HashMap<>();
    }

    // singleton class
    private static AllowMe getInstance() {
        if (instance == null) {
            instance = new AllowMe();
        }

        return instance;
    }

    public static void registerActivity(@NonNull Activity activity) {
        getInstance().mActivity = activity;

    }

    public static void unregisterActivity(Activity activity) {
        getInstance().mActivity = null;

    }

    private static Activity safeActivity() {
        Activity activity = getInstance().mActivity;
        if (activity == null) {
            throw new IllegalStateException("Activity not registered");
        }

        return activity;
    }

    public static boolean isPermissionGranted(@NonNull String permission) {
        return ContextCompat.checkSelfPermission(safeActivity(), permission) == PackageManager.PERMISSION_GRANTED;
    }

    public static void dispatchResult(String[] permissions, int[] grantResults) {
        synchronized (getRequestQueue()) {
            // get all the permission keys
            final String cacheKey = getCacheKey(permissions);
            // get all registered callbacks for the key
            final ArrayList<AllowMeCallback> callbacks = getRequestQueue().get(cacheKey);

            // no callbacks return
            if (callbacks != null) {
                final PermissionResultBucket resultBucket = PermissionResultBucket.create(permissions, grantResults);

                for (AllowMeCallback callback : callbacks) {
                    callback.onPermissionResult(resultBucket);
                }

                // now remove the request from the queue
                getRequestQueue().remove(cacheKey);
            }
        }
    }

    public static void requestPermissions(
            @NonNull AllowMeCallback callback,
            final int requestId,
            @NonNull String... permissions) {

        synchronized (getRequestQueue()) {
            final String cacheKey = getCacheKey(permissions);
            ArrayList<AllowMeCallback> stack = getRequestQueue().get(cacheKey);
            if (stack != null) {
                stack.add(callback);
            } else {
                stack = new ArrayList<>(2);
                stack.add(callback);
                getRequestQueue().put(cacheKey, stack);

                ActivityCompat.requestPermissions(safeActivity(), permissions, requestId);
            }
        }
    }

    private static String getCacheKey(String[] permissions) {
        StringBuilder result = new StringBuilder();
        for (String perm : permissions) {
            result.append(perm);
            result.append("\0");
        }
        return result.toString();
    }

    public static Map<String, ArrayList<AllowMeCallback>> getRequestQueue() {
        return getInstance().mRequestList;
    }
}
