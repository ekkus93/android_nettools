#include <jni.h>
#include <curl/curl.h>
#include <mutex>
#include <stdexcept>
#include <string>
#include <vector>

namespace {

std::once_flag curl_init_once;

void throwJavaException(JNIEnv* env, const char* class_name, const std::string& message) {
    jclass exception_class = env->FindClass(class_name);
    if (exception_class != nullptr) {
        env->ThrowNew(exception_class, message.c_str());
    }
}

std::vector<std::string> collectSupportedFeatures(const curl_version_info_data* info) {
    std::vector<std::string> features;
    if ((info->features & CURL_VERSION_SSL) != 0) {
        features.emplace_back("SSL");
    }
    if ((info->features & CURL_VERSION_IPV6) != 0) {
        features.emplace_back("IPv6");
    }
    if ((info->features & CURL_VERSION_LIBZ) != 0) {
        features.emplace_back("libz");
    }
    if ((info->features & CURL_VERSION_HTTP2) != 0) {
        features.emplace_back("HTTP2");
    }
    if ((info->features & CURL_VERSION_ASYNCHDNS) != 0) {
        features.emplace_back("AsyncDNS");
    }
    if ((info->features & CURL_VERSION_LARGEFILE) != 0) {
        features.emplace_back("Largefile");
    }
    if ((info->features & CURL_VERSION_UNIX_SOCKETS) != 0) {
        features.emplace_back("UnixSockets");
    }
    if ((info->features & CURL_VERSION_BROTLI) != 0) {
        features.emplace_back("Brotli");
    }
    if ((info->features & CURL_VERSION_ZSTD) != 0) {
        features.emplace_back("Zstd");
    }
    return features;
}

jobjectArray toJavaStringArray(JNIEnv* env, const std::vector<std::string>& values) {
    jclass string_class = env->FindClass("java/lang/String");
    jobjectArray result = env->NewObjectArray(static_cast<jsize>(values.size()), string_class, nullptr);
    for (jsize index = 0; index < static_cast<jsize>(values.size()); ++index) {
        env->SetObjectArrayElement(result, index, env->NewStringUTF(values[index].c_str()));
    }
    return result;
}

}  // namespace

extern "C"
JNIEXPORT void JNICALL
Java_dev_nettools_android_data_curl_NativeCurlBridge_nativeInitializeGlobal(
    JNIEnv* env,
    jclass /* clazz */
) {
    try {
        std::call_once(curl_init_once, []() {
            const CURLcode code = curl_global_init(CURL_GLOBAL_DEFAULT);
            if (code != CURLE_OK) {
                throw std::runtime_error(curl_easy_strerror(code));
            }
        });
    } catch (const std::exception& exception) {
        throwJavaException(env, "java/lang/IllegalStateException", exception.what());
    }
}

extern "C"
JNIEXPORT jstring JNICALL
Java_dev_nettools_android_data_curl_NativeCurlBridge_nativeGetBundledCurlVersion(
    JNIEnv* env,
    jobject /* this */
) {
    return env->NewStringUTF(curl_version());
}

extern "C"
JNIEXPORT jobjectArray JNICALL
Java_dev_nettools_android_data_curl_NativeCurlBridge_nativeGetSupportedProtocols(
    JNIEnv* env,
    jobject /* this */
) {
    const curl_version_info_data* info = curl_version_info(CURLVERSION_NOW);
    std::vector<std::string> protocols;
    if (info->protocols != nullptr) {
        for (const char* const* protocol = info->protocols; *protocol != nullptr; ++protocol) {
            protocols.emplace_back(*protocol);
        }
    }
    return toJavaStringArray(env, protocols);
}

extern "C"
JNIEXPORT jobjectArray JNICALL
Java_dev_nettools_android_data_curl_NativeCurlBridge_nativeGetSupportedFeatures(
    JNIEnv* env,
    jobject /* this */
) {
    const curl_version_info_data* info = curl_version_info(CURLVERSION_NOW);
    return toJavaStringArray(env, collectSupportedFeatures(info));
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_dev_nettools_android_data_curl_NativeCurlBridge_nativeIsHttp2Supported(
    JNIEnv* /* env */,
    jobject /* this */
) {
    const curl_version_info_data* info = curl_version_info(CURLVERSION_NOW);
    return (info->features & CURL_VERSION_HTTP2) != 0 ? JNI_TRUE : JNI_FALSE;
}
