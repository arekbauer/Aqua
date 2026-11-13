-dontwarn com.google.auto.value.AutoValue
-dontwarn javax.lang.model.element.Modifier
-keep class dagger.hilt.android.testing.HiltTestApplication { *; }
-keep class com.arekb.aqua.HiltTestRunner { *; }
-keep class dagger.hilt.android.testing.** { *; }
-keep @dagger.hilt.android.testing.HiltAndroidTest class * { *; }
-keepclassmembers class * {
    @org.junit.Rule <fields>;
    @org.junit.Before <methods>;
    @org.junit.After <methods>;
    @org.junit.Test <methods>;
}
-dontwarn org.junit.**
-dontwarn androidx.test.**
