#include <jni.h>
#include <string>

extern "C"
JNIEXPORT jstring JNICALL
Java_dev_nettools_android_data_curl_NativeCurlBridge_nativeGetBundledCurlVersion(
    JNIEnv* env,
    jobject /* this */
) {
    return env->NewStringUTF("native-bridge-scaffold");
}

extern "C"
JNIEXPORT jobjectArray JNICALL
Java_dev_nettools_android_data_curl_NativeCurlBridge_nativeGetSupportedProtocols(
    JNIEnv* env,
    jobject /* this */
) {
    jobjectArray result = env->NewObjectArray(
        0,
        env->FindClass("java/lang/String"),
        nullptr
    );
    return result;
}
