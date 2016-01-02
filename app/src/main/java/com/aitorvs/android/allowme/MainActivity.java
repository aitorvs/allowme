package com.aitorvs.android.allowme;

import android.Manifest;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AllowMeActivity {

    private String mPermission = Manifest.permission.READ_CONTACTS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button buttonPriming = (Button) findViewById(R.id.button_priming);
        buttonPriming.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!AllowMe.isPermissionGranted(mPermission)) {
                    new AllowMe.Builder()
                            .setPermissions(mPermission)
                            .setPrimingMessage("Do you want the demo to have read access to your contacts?")
                            .setRational("I need read access to contacts for the demo")
                            .setCallback(new AllowMeCallback() {
                                @Override
                                public void onPermissionResult(int i, PermissionResultSet result) {
                                    if (result.isGranted(mPermission)) {
                                        Toast.makeText(MainActivity.this, "Permission granted", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(MainActivity.this, "Permission denied", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }).request(69);
                } else {
                    Toast.makeText(MainActivity.this, "Permission already granted", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button buttonNoPriming = (Button) findViewById(R.id.button_no_priming);
        buttonNoPriming.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!AllowMe.isPermissionGranted(mPermission)) {
                    new AllowMe.Builder()
                            .setPermissions(mPermission)
                            .setRational("I need read access to contacts for the demo")
                            .setCallback(new AllowMeCallback() {
                                @Override
                                public void onPermissionResult(int i, PermissionResultSet result) {
                                    if (result.isGranted(mPermission)) {
                                        Toast.makeText(MainActivity.this, "Permission granted", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(MainActivity.this, "Permission denied", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }).request(69);
                } else {
                    Toast.makeText(MainActivity.this, "Permission already granted", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button buttonAnnotated = (Button) findViewById(R.id.button_annotated);
        buttonAnnotated.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!AllowMe.isPermissionGranted(mPermission)) {
                    new AllowMe.Builder()
                            .setPermissions(mPermission)
                            .setRational("I need read access to contacts for the demo")
                            .request(MainActivity.this, 69);
                } else {
                    Toast.makeText(MainActivity.this, "Permission already granted", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @OnPermissionResult(requestedPermissions = {Manifest.permission.READ_CONTACTS})
    void permissionRequestHandler(int requestCode, PermissionResultSet result) {
        if (result.isGranted(mPermission)) {
            Toast.makeText(MainActivity.this, "'Annotated' Permission granted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this, "'Annotated' Permission denied", Toast.LENGTH_SHORT).show();
        }
    }
}
