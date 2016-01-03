# AllowMe

AllowMe simple library to handle Android M permissions easily.
 
# Gradle Dependency

[![Release](https://img.shields.io/github/release/aitorvs/allowme.svg?label=jitpack)](https://jitpack.io/#aitorvs/allowme)


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

    compile 'com.github.aitorvs:allowme:0.2.0'
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
                    .setRationale(rationale)
                    .setPrimingMessage(primingMessage)
                    .setRationaleThemeId(themeId)
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
 - `rationale` is optional and it is the string message to show when the user denies the permission for the first time
 - `primingMessage` is optional and sets the permission priming message dialog that is showed prior to request the permission. 
 In some cases where the dialog requesting the permission appears without any further notice and without enough context, permission priming will help you to put the user in context.
 - `themeId` is optional and allows to style the rationale alert dialog
 - `requestCode` is an integer to identify the request

The library makes sure that `onPermissionResult` is only called when the `requestCode` matches the user
input request code. Anyway, the `requestCode` is also returned in the callback params so that it can 
also be checked against the original request code.

And that's all it takes.

It is also possible to request the permissions inside fragments, just extend the parent activity from
`AllowMeActivity` and steps above remain the same.

## Priming vs rationale

The permission rationale dialog appears once the user has denied the permission perviously. It is 
a way to explain the user why we need the permission. But sometimes the first permission request 
appears with little or no context and, it is then when priming is important.
Priming message dialog, when defined, will appear in the first permission request helping to put the 
user in context as to why the permission is required.

If the user decides not to go ahead after reading the priming message, the permission will not be requested 
again and the user will need to go to device settings to grant it.

# Use annotated methods instead of callbacks

It is possible to, instead of using callback methods, use annotations to define the method to be called
when the permission request is performed.

```java
    
    public class MyActivity extends AllowMeActivity {
        
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            
            //... more code here
            
            int requestCode = 69;
            if (!AllowMe.isPermissionGranted(Manifest.permission.READ_CONTACTS)) {
                new AllowMe.Builder()
                        .setPermissions(Manifest.permission.READ_CONTACTS)
                        .setRationale(rationale)
                        .setPrimingMessage(primingMessage)
                        .setRationaleThemeId(themeId)
                        .request(MyActivity.this, requestCode);
            } else {
                //... handle permission already granted
            }
        }
                
        @OnPermissionResult(requestedPermissions = {Manifest.permission.READ_CONTACTS})
        void permissionRequestHandler(int requestCode, PermissionResultSet result) {
            //... handle result
        }
    }
```

The annotation `OnPermissionResult` an array of permissions in its param `requestedPermissions`.

Note that the `request` call of the Builder pattern also receives a first param which is the class that
will contain the annotated method.

The library will then call the annotated method to handle the permission request.

Developed By
---

Aitor Viana Sanchez - aitor.viana.sanchez@gmail.com>

<a href="https://twitter.com/aitorvs">
  <img alt="Follow me on Twitter"
       src="https://raw.github.com/ManuelPeinado/NumericPageIndicator/master/art/twitter.png" />
</a>
<a href="https://plus.google.com/+AitorViana">
  <img alt="Follow me on Twitter"
       src="https://raw.github.com/ManuelPeinado/NumericPageIndicator/master/art/google-plus.png" />
</a>
<a href="https://www.linkedin.com/in/aitorvs">
  <img alt="Follow me on Twitter"
       src="https://raw.github.com/ManuelPeinado/NumericPageIndicator/master/art/linkedin.png" />
       
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



