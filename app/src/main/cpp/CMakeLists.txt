cmake_minimum_required(VERSION 3.22.1)

project(nettools_curl_bridge LANGUAGES CXX)

add_library(
    curlbridge
    SHARED
    nativecurlbridge.cpp
)

target_compile_features(curlbridge PRIVATE cxx_std_20)

