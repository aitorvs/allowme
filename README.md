# AllowMe

AllowMe simple library to handle Android M permissions easily.
 
# Gradle Dependency

### Repository

```gradle
repositories {
    maven { url "https://jitpack.io" }
}
```

### Dependency

```gradle
dependencies {

    // ... other dependencies here

    compile 'com.github.aitorvs:allowme:0.1.0'
}
```
--

# Basics

The library simplifies the task of requesting permissions for Android M. Just one call and one callback
are enough.

## Usage

1. Extend your activity from AllowMeActivity (which extends `AppCompatActivity`)
 
 ```java
 public class MyPermissionsActivity extends AllowMeActivity {
    //... more code here
    
 }
 ```

2. Request the permission and register the callback for handling. All in one call

```java
        //... more code here
        
        if (!AllowMe.isPermissionGranted(permission)) {
            new AllowMe.Builder()
                    .setPermissions(permission)
                    .setRational(rational)
                    .setRationalThemeId(themeId)
                    .setCallback(new AllowMeCallback() {
                        @Override
                        public void onPermissionResult(int requestCode, PermissionResultSet result) {
                            if (result.isGranted(permission)) {
                                //... permission is granted, handle here
                            }
                        }
                    }).request(requestCode);
        } else {
            //... handle permission already granted
        }
```

 - `permission` is the permission you need to request
 - `rational` is optional and it is the string message to show when the user denies the permission for the first time
 - `themeId` is optional and allows to style the rational alert dialog
 - `requestCode` is an integer to identify the request

The library makes sure that `onPermissionResult` is only called when the `requestCode` matches the user
input request code. Anyway, the `requestCode` is also returned in the callback in case the user
wants to double check. Better safe than sorry they say.

And that's all it takes.

It is also possible to request the permissions inside fragments, just extend the parent activity from
`AllowMeActivity` and steps above remain the same.

## License

 Apache 2.0

    Copyright 2015 aitorvs, Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.



