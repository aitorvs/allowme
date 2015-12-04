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

Note: I haven't release a version yet, it's coming soon. You anyway try a snapshot adding the following
dependency to your `app/build.gradle`

```gradle
dependencies {

    // ... other dependencies here

    compile 'com.github.aitorvs:allowme:-SNAPSHOT'
}
```
--

# Basics

The library simplifies the task of requesting permissions for Android M. Just one call and one callback
are enough.

## Usage

1. Extend your activity from AllowMeActivity
 
 ```java
 public class MyPermissionsActivity extends AllowMeActivity {
    //... more code here
    
 }
 ```

2. Request the permission and register the callback for handling. All in one call

```java
        //... more code here
        
        if (!AllowMe.isPermissionGranted(permission)) {
            AllowMe.requestPermissionWithRational(new AllowMeCallback() {
                @Override
                public void onPermissionResult(int requestCode, PermissionResultSet result) {
                    if (result.isGranted(permission)) {
                        //... handle your permission here
                    }
                }
            }, requestCode, rational, permission);
        } else {
            onPermissionGranted();
        }
```

 - `permission` is the permission you need to request
 - `requestCode` is an integer to identify the request
 - `rational` is the string message to show when the user denies the permission for the first time

And that's all it takes.

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



