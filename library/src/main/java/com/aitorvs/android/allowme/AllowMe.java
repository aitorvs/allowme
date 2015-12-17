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
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AllowMe {

    private static final String TAG = AllowMe.class.getSimpleName();
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

    public static void unregisterActivity(@NonNull Activity activity) {
        if (activity == getInstance().mActivity) {
            getInstance().mActivity = null;
        } else {
            Log.w(TAG, "unregisterActivity: Old activity is trying to unregister");
        }

    }

    private static Activity safeActivity() {
        Activity activity = getInstance().mActivity;
        if (activity == null) {
            throw new IllegalStateException("Activity not registered");
        }

        return activity;
    }

    /**
     * Checks whether a particular permissions is already granted
     *
     * @param permission permission
     * @return <code>boolean</code> value, <code>true</code> when permissions is already granted,
     * <code>false</code> when permissions not granted.
     */
    public static boolean isPermissionGranted(@NonNull String permission) {
        return ContextCompat.checkSelfPermission(safeActivity(), permission) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Checks whether user should show the permissions request rational or not
     *
     * @param permission permission
     * @return Returns <code>boolean</code> with value <code>true</code> when rational should be shown
     * or <code>false</code> otherwise
     */
    public static boolean shouldShowRational(@NonNull String permission) {
        return ActivityCompat.shouldShowRequestPermissionRationale(safeActivity(), permission);
    }

    /**
     * Dispatches the permission request to the user handler
     *
     * @param requestCode  permission request code
     * @param permissions  permissions
     * @param grantResults request results
     * @return boolean Return <code>false</code> to allow normal menu processing to
     * proceed, <code>true</code> to consume it here.
     */
    public static boolean dispatchResult(int requestCode, String[] permissions, int[] grantResults) {
        synchronized (getRequestQueue()) {
            // get all the permission keys
            final String cacheKey = getRequestKey(requestCode, permissions);
            // get all registered callbacks for the key
            final ArrayList<AllowMeCallback> callbacks = getRequestQueue().get(cacheKey);

            // no callbacks return
            if (callbacks != null) {
                final PermissionResultSet resultSet = PermissionResultSet.create(permissions, grantResults);

                for (AllowMeCallback callback : callbacks) {
                    callback.onPermissionResult(requestCode, resultSet);
                }

                // now remove the request from the queue
                getRequestQueue().remove(cacheKey);

                // consume the event here
                return true;
            }

            // no callback called, event not consumed here.
            return false;
        }
    }

    /**
     * Requests the permission, showing the given rational when necessary and calls the registered
     * callback when permission request operation is performed
     *
     * @param callback        {@link AllowMeCallback} callback method
     * @param requestCode     request code identifier
     * @param rational        string rational to be shown when appropriate
     * @param rationalThemeId rational alert dialog theme id
     * @param permission      permission under request
     */
    private static void requestPermissionWithRational(
            final AllowMeCallback callback,
            final int requestCode,
            String rational,
            int rationalThemeId,
            final String permission) {

        // healthy check
        if (isPermissionGranted(permission)) {
            // permission is already granted...why you ask?
            return;
        }

        if (ActivityCompat.shouldShowRequestPermissionRationale(safeActivity(), permission)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(safeActivity(), rationalThemeId)
                    .setTitle("")
                    .setMessage(rational)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            requestPermissions(callback, requestCode, permission);
                        }
                    })
                    .setNegativeButton("Don't allow", null);

            builder.show();
        } else {
            requestPermissions(callback, requestCode, permission);
        }
    }

    /**
     * Requests the permission, and calls the registered callback when permission request operation
     * is performed
     *
     * @param callback    callback method
     * @param requestCode request code identifier
     * @param permission  permission to request
     */
    private static void requestPermission(
            final AllowMeCallback callback,
            int requestCode,
            final String permission) {

        // healthy check
        if (!isPermissionGranted(permission)) {
            requestPermissions(callback, requestCode, permission);
        }
    }

    private static void requestPermissions(
            AllowMeCallback callback,
            int requestCode,
            String... permissions) {

        synchronized (getRequestQueue()) {
            final String requestKey = getRequestKey(requestCode, permissions);
            ArrayList<AllowMeCallback> callbackList = getRequestQueue().get(requestKey);
            if (callbackList != null) {
                callbackList.add(callback);
            } else {
                callbackList = new ArrayList<>();
                callbackList.add(callback);
                getRequestQueue().put(requestKey, callbackList);

                ActivityCompat.requestPermissions(safeActivity(), permissions, requestCode);
            }
        }
    }

    /**
     * Generates a key given the permissions requested and its request code
     *
     * @param requestCode request code identifier
     * @param permissions permissions
     * @return generated request key
     */
    private static String getRequestKey(int requestCode, String[] permissions) {
        StringBuilder result = new StringBuilder();
        result.append(requestCode);
        for (String perm : permissions) {
            result.append(perm);
            result.append("\0");
        }
        return result.toString();
    }

    private static Map<String, ArrayList<AllowMeCallback>> getRequestQueue() {
        return getInstance().mRequestList;
    }

    /**
     * Builder class
     */
    public static class Builder {
        private String rational;
        private int rationalThemeId = 0;
        private String permission;
        private AllowMeCallback callback;
        private String primingMessage;

        public Builder setRational(@NonNull String rational) {
            this.rational = rational;
            return this;
        }

        public Builder setRational(@StringRes int res) {
            this.rational = safeActivity().getString(res);
            return this;
        }

        public Builder setRationalThemeId(@IntRange(from = 0, to = Integer.MAX_VALUE) int rationalThemeId) {
            this.rationalThemeId = rationalThemeId;
            return this;
        }

        public Builder setPermissions(@NonNull String permissions) {
            this.permission = permissions;
            return this;
        }

        public Builder setCallback(@NonNull AllowMeCallback callback) {
            this.callback = callback;
            return this;
        }

        public Builder setPrimingMessage(String primingMessage) {
            this.primingMessage = primingMessage;
            return this;
        }

        public void request(@IntRange(from = 1, to = Integer.MAX_VALUE) final int requestCode) {
            // some checks
            if (this.permission == null) {
                throw new InvalidParameterException("Permissions must be set");
            }

            // permission priming ?
            if (this.primingMessage != null) {
                // show the priming message
                AlertDialog.Builder builder = new AlertDialog.Builder(safeActivity(), rationalThemeId)
                        .setTitle("")
                        .setMessage(this.primingMessage)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                requestPermission(requestCode);
                            }
                        })
                        .setNegativeButton("Not now", null);

                builder.show();
            } else {
                // request permission directly
                requestPermission(requestCode);
            }
        }

        private void requestPermission(int requestCode) {
            if (rational == null) {
                AllowMe.requestPermission(this.callback, requestCode, this.permission);
            } else {
                AllowMe.requestPermissionWithRational(this.callback, requestCode, rational, rationalThemeId, this.permission);
            }
        }
    }

}
