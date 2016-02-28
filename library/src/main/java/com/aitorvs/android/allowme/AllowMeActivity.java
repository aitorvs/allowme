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

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

public class AllowMeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AllowMe.registerActivity(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // on visible
        AllowMe.registerActivity(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // on  begin interaction
        AllowMe.registerActivity(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // on stop interaction
        AllowMe.unregisterActivity(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // on invisible
        AllowMe.unregisterActivity(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean consumed = AllowMe.dispatchResult(requestCode, permissions, grantResults);
        if (!consumed) {
            // this means we didn't find any callback for this permission, try super!
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
