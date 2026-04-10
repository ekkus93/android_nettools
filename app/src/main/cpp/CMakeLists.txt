cmake_minimum_required(VERSION 3.22.1)

project(nettools_curl_bridge LANGUAGES CXX)

set(PREBUILT_ROOT "${CMAKE_CURRENT_SOURCE_DIR}/prebuilt")
set(PREBUILT_ABI_DIR "${PREBUILT_ROOT}/${ANDROID_ABI}")

foreach(lib_name curl ssl crypto nghttp2)
    if(NOT EXISTS "${PREBUILT_ABI_DIR}/lib/lib${lib_name}.a")
        message(FATAL_ERROR "Missing prebuilt lib${lib_name}.a for ABI ${ANDROID_ABI}. Run scripts/build_native_deps.sh first.")
    endif()
endforeach()

if(NOT EXISTS "${PREBUILT_ROOT}/include/curl/curl.h")
    message(FATAL_ERROR "Missing bundled curl headers. Run scripts/build_native_deps.sh first.")
endif()

add_library(curl_static STATIC IMPORTED)
set_target_properties(curl_static PROPERTIES IMPORTED_LOCATION "${PREBUILT_ABI_DIR}/lib/libcurl.a")

add_library(ssl_static STATIC IMPORTED)
set_target_properties(ssl_static PROPERTIES IMPORTED_LOCATION "${PREBUILT_ABI_DIR}/lib/libssl.a")

add_library(crypto_static STATIC IMPORTED)
set_target_properties(crypto_static PROPERTIES IMPORTED_LOCATION "${PREBUILT_ABI_DIR}/lib/libcrypto.a")

add_library(nghttp2_static STATIC IMPORTED)
set_target_properties(nghttp2_static PROPERTIES IMPORTED_LOCATION "${PREBUILT_ABI_DIR}/lib/libnghttp2.a")

add_library(
    curlbridge
    SHARED
    nativecurlbridge.cpp
)

target_compile_features(curlbridge PRIVATE cxx_std_20)
target_include_directories(curlbridge PRIVATE "${PREBUILT_ROOT}/include")
target_link_libraries(
    curlbridge
    PRIVATE
    curl_static
    ssl_static
    crypto_static
    nghttp2_static
    android
    log
)
