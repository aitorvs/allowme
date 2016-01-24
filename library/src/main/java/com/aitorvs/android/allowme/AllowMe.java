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
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.lang.reflect.Method;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class AllowMe {

    private static final String TAG = AllowMe.class.getSimpleName();
    private static final String ALLOWME_SHOULD_SHOW_PRIMING_KEY = TAG + ".key.should_show_priming";
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
     * Checks whether user should show the permissions request rationale or not
     *
     * @param permission permission
     * @return Returns <code>boolean</code> with value <code>true</code> when rationale should be shown
     * or <code>false</code> otherwise
     */
    public static boolean shouldShowRationale(@NonNull String permission) {
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
     * Requests the permission, showing the given rationale when necessary and calls the registered
     * callback when permission request operation is performed
     *
     * @param callback        {@link AllowMeCallback} callback method
     * @param requestCode     request code identifier
     * @param rationale        string rationale to be shown when appropriate
     * @param rationaleThemeId rationale alert dialog theme id
     * @param permission      permission under request
     */
    private static void requestPermissionWithRationale(
            final AllowMeCallback callback,
            final int requestCode,
            String rationale,
            int rationaleThemeId,
            final String permission) {

        // healthy check
        if (isPermissionGranted(permission)) {
            // permission is already granted...why you ask?
            return;
        }

        if (ActivityCompat.shouldShowRequestPermissionRationale(safeActivity(), permission)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(safeActivity(), rationaleThemeId)
                    .setTitle("")
                    .setMessage(rationale)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            requestPermissions(callback, requestCode, permission);
                        }
                    })
                    .setNegativeButton("Not Now", null);

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
        private String rationale;
        private int rationaleThemeId = 0;
        private String permission;
        private AllowMeCallback callback;
        private String primingMessage;

        /**
         * Set the permission rationale message
         *
         * @param rationale {@link String} rationale
         * @return {@link Builder}
         */
        public Builder setRationale(@NonNull String rationale) {
            this.rationale = rationale;
            return this;
        }

        /**
         * Sset the permission rationale
         *
         * @param res Rationale string resource ID
         * @return {@link Builder}
         */
        public Builder setRational(@StringRes int res) {
            this.rationale = safeActivity().getString(res);
            return this;
        }

        /**
         * Set the rationale dialog theme
         *
         * @param themeId identifier
         * @return {@link Builder}
         */
        public Builder setRationaleThemeId(@IntRange(from = 0, to = Integer.MAX_VALUE) int themeId) {
            this.rationaleThemeId = themeId;
            return this;
        }

        /**
         * Set the permission to request
         *
         * @param permissions permissions
         * @return {@link Builder}
         */
        public Builder setPermissions(@NonNull String permissions) {
            this.permission = permissions;
            return this;
        }

        /**
         * Set the callback to be call once the permissions are granted
         *
         * @param callback {@link AllowMeCallback} callback
         * @return {@link Builder}
         */
        public Builder setCallback(@NonNull AllowMeCallback callback) {
            this.callback = callback;
            return this;
        }

        /**
         * Set the permission priming message that will be shown before the permission is request
         * (Optional)
         *
         * @param primingMessage priming message {@link String}
         * @return {@link Builder}
         */
        public Builder setPrimingMessage(String primingMessage) {
            this.primingMessage = primingMessage;
            return this;
        }

        /**
         * Request the permissions set using the Builder
         *
         * @param requestCode positive <code>int</code> value to identify the permission request
         */
        public void request(@IntRange(from = 1, to = Integer.MAX_VALUE) final int requestCode) {
            // some checks
            throwIfNoPermissions();
            throwIfNoCallback();

            // permission priming ?
            if (this.primingMessage != null && shouldShowPrimingMessage()) {
                // show the priming message
                AlertDialog.Builder builder = new AlertDialog.Builder(safeActivity(), rationaleThemeId)
                        .setTitle("")
                        .setMessage(this.primingMessage)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                requestPermission(Builder.this.callback, requestCode);
                            }
                        })
                        .setNegativeButton("Not now", null);

                builder.show();
            } else if (this.primingMessage == null) {
                // request permission directly
                requestPermission(Builder.this.callback, requestCode);
            }
        }

        public void requestPermissionForResult(final @NonNull Object handlerClass,
                                               @IntRange(from = 1, to = Integer.MAX_VALUE) int requestCode) {
            // throw when user forgot the permissions
            throwIfNoPermissions();
            // we don't need the callbacks here because should be using annotated callback
            final Method annotatedCallback = getAnnotatedMethod(handlerClass, OnPermissionResult.class);
            annotatedCallback.setAccessible(true);

            final int fRequestCode = requestCode;

            // do the magic
            // permission priming ?
            if (this.primingMessage != null && shouldShowPrimingMessage()) {
                // show the priming message
                AlertDialog.Builder builder = new AlertDialog.Builder(safeActivity(), rationaleThemeId)
                        .setTitle("")
                        .setMessage(this.primingMessage)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                requestPermission(new AllowMeCallback() {
                                    @Override
                                    public void onPermissionResult(int requestCode, PermissionResultSet results) {
                                        try {
                                            annotatedCallback.invoke(handlerClass, requestCode, results);
                                        } catch (Exception e) {
                                            throw new IllegalStateException(String.format("Error invoking %s", annotatedCallback.getName()), e);
                                        }
                                    }
                                }, fRequestCode);
                            }
                        })
                        .setNegativeButton("Not now", null);

                builder.show();
            } else if (this.primingMessage == null) {
                // request permission directly
                requestPermission(new AllowMeCallback() {
                    @Override
                    public void onPermissionResult(int requestCode, PermissionResultSet results) {
                        try {
                            annotatedCallback.invoke(handlerClass, requestCode, results);
                        } catch (Exception e) {
                            throw new IllegalStateException(String.format("Error invoking %s", annotatedCallback.getName()), e);
                        }
                    }
                }, fRequestCode);
            }


        }

        private Method getAnnotatedMethod(Object target, Class<OnPermissionResult> targetAnnotation) {
            final Method[] targetMethods = target.getClass().getDeclaredMethods();
            Method match = null;
            for (Method method : targetMethods) {
                OnPermissionResult annotation = method.getAnnotation(targetAnnotation);
                // FIXME: 02/01/16 permissions should eventually be an array
                if (annotation != null && equalPermissions(new String[]{this.permission}, annotation.requestedPermissions())) {
                    match = method;
                    break;
                }
            }

            // check the correctness of the annotated method
            if (match == null) {
                throw new IllegalStateException(String.format("No OnPermissionResult annotated " +
                        "methods found in %s or with different permission set parameters.", target.getClass().getName()));
            } else if (match.getParameterTypes().length != 2
                    || !match.getParameterTypes()[0].toString().equalsIgnoreCase("int")
                    || match.getParameterTypes()[1] != PermissionResultSet.class) {
                throw new IllegalStateException(String.format("Method %s shall have two " +
                        "parameters of type 'int' and 'PermissionResultSet'", match.getName()));
            }

            return match;
        }

        /**
         * Compare two array of permissions, independent of the order inside the array
         *
         * @param foo array of permissions
         * @param bar array of permissions
         * @return <code>true</code> when match, <code>false</code> otherwise
         */
        private boolean equalPermissions(String[] foo, String[] bar) {
            if (foo == null || bar == null) {
                return (foo == null) && (bar == null);
            } else if (foo.length != bar.length) {
                return false;
            }

            // sort the arrays
            Arrays.sort(foo);
            Arrays.sort(bar);

            // do the actual compare
            for (int i = 0; i < foo.length; i++) {
                if (!foo[i].equals(bar[i]))
                    return false;
            }
            return true;
        }

        /**
         * Request the permissions using the {@link Builder} fields and the given params
         *
         * @param callback    permission request callback
         * @param requestCode permission request code identifier
         */
        private void requestPermission(AllowMeCallback callback, int requestCode) {
            if (rationale == null) {
                AllowMe.requestPermission(callback, requestCode, this.permission);
            } else {
                AllowMe.requestPermissionWithRationale(callback, requestCode, rationale, rationaleThemeId, this.permission);
            }
        }

        /**
         * Returns whether the permission priming message should be shown.
         * The method will return <code>true</code> the first call around, so the permission priming
         * message is shown. After that first call, method will return <code>false</code>
         *
         * @return <code>true</code> first call around, <code>false</code> other method calls
         */
        public boolean shouldShowPrimingMessage() {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(safeActivity());
            boolean value = sharedPreferences.getBoolean(ALLOWME_SHOULD_SHOW_PRIMING_KEY, true);

            // we've been called, set it to false again
            sharedPreferences.edit().putBoolean(ALLOWME_SHOULD_SHOW_PRIMING_KEY, false).apply();

            return value;
        }

        private void throwIfNoPermissions() {
            if (this.permission == null) {
                throw new InvalidParameterException("Permissions must be set");
            }
        }

        private void throwIfNoCallback() {
            if (this.callback == null) {
                throw new InvalidParameterException("Callback must be set");
            }
        }
    }

}
