AndroidDependencySizeAnalyzer
======
Are you looking for a gradle script to analyze your dependency size and aar file content? well today is your lucky day.

Last Version
--------
```
1.0.0
```
usage
---------
add
```groovy
classpath "io.github.chinacoolder:android_dependency_size_analyzer:${last_version}"
```
to your project build gradle<br>
```groovy
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath "io.github.chinacoolder:android_dependency_size_analyzer:1.0.0"
    }
}
```
apply plugin
```groovy
plugins {
    id 'happy.jyc.android_dependency_analyzer'
}
```
now after sync the gradle, run
```
./gradlew tasks
```
then you will see
```
JYCAndroidAnalyzer tasks
------------------------
jycAARAnalyze - analyze aar file's size
jycDependencySize - list all the dependency and it's size of class path
```
now is time to enjoy the fun

Analyze Dependency Size
---------
run 
```
./gradlew jycDependencySize --name=app
```
then will list all classpath and the dependency size of the classpath
```
For classpath releaseCompileClasspath:
Total dependencies size :                                                                                               12.93 mb
com.google.android.material:material:1.4.0@aar                                                                          1573.87 kb
org.jetbrains.kotlin:kotlin-stdlib:1.6.10@jar                                                                           1472.73 kb
org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.5.0@jar                                                             1447.78 kb
androidx.appcompat:appcompat:1.3.1@aar                                                                                  1058.66 kb
androidx.core:core:1.7.0@aar                                                                                            957.23 kb
com.squareup.okhttp3:okhttp:4.9.1@jar                                                                                   772.84 kb
com.tencent:mmkv-static:1.2.10@aar                                                                                      693.48 kb
com.google.android.gms:play-services-base:17.1.0@aar                                                                    522.27 kb
androidx.constraintlayout:constraintlayout:2.0.4@aar                                                                    375.35 kb
androidx.recyclerview:recyclerview:1.1.0@aar                                                                            349.86 kb
androidx.fragment:fragment:1.3.6@aar                                                                                    291.21 kb
com.google.android.gms:play-services-basement:17.0.0@aar                                                                271.39 kb
com.squareup.okio:okio:2.8.0@jar                                                                                        237.48 kb
com.google.code.gson:gson:2.8.8@jar                                                                                     236.37 kb
androidx.constraintlayout:constraintlayout-solver:2.0.4@jar                                                             225.67 kb
org.jetbrains.kotlin:kotlin-stdlib-common:1.6.10@jar                                                                    195.92 kb
jp.co.cyberagent.android:gpuimage:2.1.0@aar                                                                             190.46 kb
androidx.transition:transition:1.2.0@aar                                                                                166.82 kb
androidx.core:core-ktx:1.7.0@aar                                                                                        158.69 kb
```
for task `jycDependencySize`, these are the supported params:

| Name      | Optional | Multiple | Remark                                                                                  |
|-----------|----------|----------|-----------------------------------------------------------------------------------------|
| name      | false    | false    | the module name which module need to be an analyze                                      |
| classpath | true     | true     | which classpath need to be analyze                                                      |
| filter    | true     | true     | dependency filter, eg. `com.facebook`                                                   | 
| gradlew   | true     | false    | the gradlew file path, default is project root dir, eg `D:\project\projectname`         |
| cache     | true     | false    | the gradle cache path, default is `{user_home}\.gradle\caches`, eg. `D:\.gralde\caches` |

Analyze AAR Size
---------
run
```
./gradlew jycAARAnalyze --aar=com.google.android.material:material:1.4.0
```
then will list all file of this aar and the size of every file
```
For aar com.google.android.material:material:1.4.0:
Total size:                                                                                                             2.47 mb
classes.jar                                                                                                             1304.39 kb
res/values/values.xml                                                                                                   272.35 kb
R.txt                                                                                                                   213.27 kb
public.txt                                                                                                              19.67 kb
annotations.zip                                                                                                         13.63 kb
res/values-v21/values-v21.xml                                                                                           8.90 kb
res/values-ml/values-ml.xml                                                                                             6.51 kb
res/values-ta/values-ta.xml                                                                                             6.41 kb
res/values-te/values-te.xml                                                                                             6.26 kb
res/values-my/values-my.xml                                                                                             6.18 kb
res/values-km/values-km.xml                                                                                             6.15 kb
res/values-kn/values-kn.xml                                                                                             6.14 kb
res/values-ne/values-ne.xml                                                                                             6.10 kb
res/values-ka/values-ka.xml                                                                                             6.05 kb
res/values-th/values-th.xml                                                                                             5.86 kb
res/values-gu/values-gu.xml                                                                                             5.84 kb
```
for task `jycAARAnalyze`, these are the supported params:

| Name    | Optional | Multiple | Remark                                                                                  |
|---------|----------|----------|-----------------------------------------------------------------------------------------|
| aar     | false    | false    | target aar need to be an analyze                                                        |
| ext     | true     | true     | extension filter, eg. `png`,`jpg`                                                       |
| filter  | true     | true     | content file filter, eg. `com.facebook`                                                 |
| cache   | true     | false    | the gradle cache path, default is `{user_home}\.gradle\caches`, eg. `D:\.gralde\caches` |

License
-------

```
Copyright JiaYiChi.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```