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

import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

public class PermissionResultSet {
    private final PermissionResult[] mResults;

    private PermissionResultSet(PermissionResult[] results) {
        mResults = results;
    }

    protected static PermissionResultSet create(@NonNull String[] permissions, int[] grantResults) {
        PermissionResult[] results = new PermissionResult[permissions.length];
        for (int i = 0; i < permissions.length; i++)
            results[i] = new PermissionResult(permissions[i], grantResults[i]);
        return new PermissionResultSet(results);
    }

    public Map<String, Boolean> getGrantedMap() {
        HashMap<String, Boolean> map = new HashMap<>();
        for (String perm : getPermissions())
            map.put(perm, isGranted(perm));
        return map;
    }

    public String[] getPermissions() {
        String[] perms = new String[mResults.length];
        for (int i = 0; i < perms.length; i++)
            perms[i] = mResults[i].getPermission();
        return perms;
    }

    public boolean isGranted(@NonNull String permission) {
        synchronized (mResults) {
            for (PermissionResult result : mResults) {
                if (result.getPermission().equals(permission))
                    return result.isGranted();
            }
            return false;
        }
    }
}
