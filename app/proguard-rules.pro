# ===================================================
# Supply Oscillator - ProGuard Rules
# 개선 버전 (5단계)
# ===================================================

# ===================================================
# 기본 설정
# ===================================================
# 최적화 활성화
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification

# 속성 유지 (디버깅 및 스택 트레이스용)
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keepattributes InnerClasses
-keepattributes EnclosingMethod
-keepattributes Exceptions

# ===================================================
# Chaquopy (Python) - CRITICAL
# ===================================================
# Python 관련 클래스는 절대 난독화하면 안 됨
-keep class com.chaquo.python.** { *; }
-dontwarn com.chaquo.python.**

-keep class org.python.** { *; }
-dontwarn org.python.**

# Python에서 호출되는 Java/Kotlin 클래스
-keepclassmembers class * {
    @com.chaquo.python.* <methods>;
}

# ===================================================
# Data Models (JSON 직렬화)
# ===================================================
# ✅ 개선: 필드와 생성자만 유지 (메서드는 난독화 가능)
-keepclassmembers class com.stockoscillator.data.model.** {
    <fields>;
    <init>(...);
}

# Enum 보호
-keepclassmembers enum com.stockoscillator.data.model.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ===================================================
# ViewModel (AndroidX Lifecycle)
# ===================================================
# ViewModel은 리플렉션으로 생성되므로 보호 필요
-keep class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

-keep class * extends androidx.lifecycle.AndroidViewModel {
    <init>(...);
}

# ViewModelProvider.Factory
-keep class * implements androidx.lifecycle.ViewModelProvider.Factory {
    <init>(...);
}

# ===================================================
# Repository 패턴
# ===================================================
# Repository 클래스의 public 생성자와 메서드 유지
-keep class com.stockoscillator.data.repository.** {
    public <init>(...);
    public <methods>;
}

# ===================================================
# MPAndroidChart
# ===================================================
-keep class com.github.mikephil.charting.** { *; }
-dontwarn com.github.mikephil.charting.**

# Chart Marker 클래스 (커스텀 마커)
-keep class com.stockoscillator.ui.components.*MarkerView { *; }

# ===================================================
# Kotlin
# ===================================================
-dontwarn kotlin.**
-dontwarn kotlinx.**

# Kotlin Metadata
-keep class kotlin.Metadata { *; }

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# Kotlin Coroutines Flow
-keep class kotlinx.coroutines.flow.** { *; }

# ===================================================
# Compose
# ===================================================
# Composable 함수 보호 (리플렉션 사용)
-keep @androidx.compose.runtime.Composable <methods>

# Compose ViewModel
-keep class androidx.lifecycle.viewmodel.compose.** { *; }

# ===================================================
# DataStore (Settings 저장)
# ===================================================
-keepclassmembers class * extends androidx.datastore.preferences.** {
    <fields>;
}

# ===================================================
# Parcelable & Serializable
# ===================================================
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# ===================================================
# Android 기본 보호
# ===================================================
# R 클래스 보호
-keepclassmembers class **.R$* {
    public static <fields>;
}

# View 생성자 보호 (XML에서 사용)
-keepclassmembers class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# onClick 메서드 보호
-keepclassmembers class * {
    void *(**On*Event);
    void *(**On*Listener);
}

# ===================================================
# 경고 억제 (서드파티 라이브러리)
# ===================================================
-dontwarn java.lang.invoke.StringConcatFactory
-dontwarn org.jetbrains.annotations.**
-dontwarn javax.annotation.**

# ===================================================
# 디버그 정보 제거 (Release용)
# ===================================================
# 로그 제거 (프로덕션 빌드)
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# ===================================================
# Native Method 보호
# ===================================================
-keepclasseswithmembernames class * {
    native <methods>;
}

# ===================================================
# ✅ 추가: JSON 직렬화 라이브러리 (미래 확장용)
# ===================================================
# Gson (사용 시)
# -keep class com.google.gson.** { *; }
# -keepclassmembers class * {
#     @com.google.gson.annotations.SerializedName <fields>;
# }

# Moshi (사용 시)
# -keep class com.squareup.moshi.** { *; }
# -keepclassmembers class * {
#     @com.squareup.moshi.Json <fields>;
# }

# Kotlinx Serialization (사용 시)
# -keepattributes *Annotation*, InnerClasses
# -dontnote kotlinx.serialization.AnnotationsKt
# -keepclassmembers class kotlinx.serialization.json.** {
#     *** Companion;
# }
# -keepclasseswithmembers class kotlinx.serialization.json.** {
#     kotlinx.serialization.KSerializer serializer(...);
# }

# ===================================================
# ✅ 추가: Retrofit (미래 확장용)
# ===================================================
# Retrofit
# -keep class retrofit2.** { *; }
# -keepclasseswithmembers class * {
#     @retrofit2.http.* <methods>;
# }

# OkHttp
# -keep class okhttp3.** { *; }
# -keep interface okhttp3.** { *; }
# -dontwarn okhttp3.**

# ===================================================
# 보안: 민감한 정보 보호
# ===================================================
# API 키나 비밀 정보가 담긴 클래스 (예시)
# -keep class com.stockoscillator.config.ApiKeys {
#     <fields>;
# }

# ===================================================
# 테스트 제외
# ===================================================
-dontwarn org.junit.**
-dontwarn org.mockito.**
-dontwarn org.robolectric.**

# ===================================================
# 최종 검증
# ===================================================
# ProGuard 매핑 파일 생성 (디버깅용)
# build/outputs/mapping/release/mapping.txt 참조

# 빌드 후 확인 사항:
# 1. APK 크기 감소 확인
# 2. 앱 정상 동작 확인 (특히 Python 연동)
# 3. Crash 발생 시 mapping.txt로 스택 트레이스 복원

# ===================================================
# 주의사항
# ===================================================
# 1. Python 관련 클래스는 절대 난독화하지 말 것
# 2. ViewModel은 리플렉션으로 생성되므로 보호 필요
# 3. Data Model의 필드명은 JSON 직렬화에 사용되므로 보호
# 4. Chart Marker 클래스는 MPAndroidChart가 리플렉션으로 접근
# 5. Release 빌드 후 반드시 앱 전체 기능 테스트 수행