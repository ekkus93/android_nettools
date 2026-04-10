#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
NDK_ROOT="${ANDROID_NDK_ROOT:-${HOME}/Android/Sdk/ndk/28.2.13676358}"
TOOLCHAIN_FILE="${NDK_ROOT}/build/cmake/android.toolchain.cmake"
API_LEVEL="${ANDROID_API_LEVEL:-26}"
WORK_DIR="${ROOT_DIR}/.native-build"

OPENSSL_VERSION="3.3.0"
OPENSSL_ARCHIVE="openssl-${OPENSSL_VERSION}.tar.gz"
OPENSSL_URL="https://github.com/openssl/openssl/releases/download/openssl-${OPENSSL_VERSION}/${OPENSSL_ARCHIVE}"

NGHTTP2_VERSION="1.62.1"
NGHTTP2_ARCHIVE="nghttp2-${NGHTTP2_VERSION}.tar.gz"
NGHTTP2_URL="https://github.com/nghttp2/nghttp2/releases/download/v${NGHTTP2_VERSION}/${NGHTTP2_ARCHIVE}"

CURL_VERSION="8.8.0"
CURL_ARCHIVE="curl-${CURL_VERSION}.tar.gz"
CURL_URL="https://curl.se/download/${CURL_ARCHIVE}"
CACERT_URL="https://curl.se/ca/cacert.pem"

PREBUILT_DIR="${ROOT_DIR}/app/src/main/cpp/prebuilt"
MAIN_ASSET_DIR="${ROOT_DIR}/app/src/main/assets/curl"
DEBUG_ASSET_DIR="${ROOT_DIR}/app/src/debug/assets/curl"
MAIN_JNILIBS_DIR="${ROOT_DIR}/app/src/main/jniLibs"
DEBUG_JNILIBS_DIR="${ROOT_DIR}/app/src/debug/jniLibs"

ABIS=("arm64-v8a" "armeabi-v7a" "x86_64")

require_tools() {
  command -v cmake >/dev/null
  command -v ninja >/dev/null
  command -v python3 >/dev/null
  command -v curl >/dev/null
  command -v perl >/dev/null
  command -v make >/dev/null
}

download_if_missing() {
  local url="$1"
  local output="$2"
  if [[ ! -f "${output}" ]]; then
    curl -L --fail --output "${output}" "${url}"
  fi
}

extract_if_missing() {
  local archive="$1"
  local output_dir="$2"
  if [[ ! -d "${output_dir}" ]]; then
    tar -xf "${archive}" -C "$(dirname "${output_dir}")"
  fi
}

abi_clang() {
  case "$1" in
    arm64-v8a) echo "aarch64-linux-android${API_LEVEL}-clang" ;;
    armeabi-v7a) echo "armv7a-linux-androideabi${API_LEVEL}-clang" ;;
    x86_64) echo "x86_64-linux-android${API_LEVEL}-clang" ;;
    *) return 1 ;;
  esac
}

abi_openssl_target() {
  case "$1" in
    arm64-v8a) echo "android-arm64" ;;
    armeabi-v7a) echo "android-arm" ;;
    x86_64) echo "android-x86_64" ;;
    *) return 1 ;;
  esac
}

build_openssl() {
  local abi="$1"
  local source_dir="${WORK_DIR}/openssl-src-${abi}"
  local install_dir="${WORK_DIR}/install-openssl-${abi}"
  local toolchain_bin="${NDK_ROOT}/toolchains/llvm/prebuilt/linux-x86_64/bin"

  if [[ -f "${install_dir}/lib/libssl.a" && -f "${install_dir}/lib/libcrypto.a" ]]; then
    return
  fi

  rm -rf "${source_dir}" "${install_dir}"
  tar -xf "${WORK_DIR}/${OPENSSL_ARCHIVE}" -C "${WORK_DIR}"
  mv "${WORK_DIR}/openssl-${OPENSSL_VERSION}" "${source_dir}"

  pushd "${source_dir}" >/dev/null
  export PATH="${toolchain_bin}:${PATH}"
  export ANDROID_NDK_ROOT="${NDK_ROOT}"
  export CC="${toolchain_bin}/$(abi_clang "${abi}")"
  export AR="${toolchain_bin}/llvm-ar"
  export RANLIB="${toolchain_bin}/llvm-ranlib"

  local extra_flags=("-fPIC")
  local extra_config=()
  if [[ "${abi}" == "armeabi-v7a" ]]; then
    extra_flags+=("-march=armv7-a")
    extra_config+=(no-asm)
  fi

  ./Configure "$(abi_openssl_target "${abi}")" \
    "-D__ANDROID_API__=${API_LEVEL}" \
    no-shared no-tests "${extra_config[@]}" --prefix="${install_dir}" "${extra_flags[@]}"
  make -j"$(nproc)"
  make install_sw
  popd >/dev/null
}

build_nghttp2() {
  local abi="$1"
  local build_dir="${WORK_DIR}/build-nghttp2-${abi}"
  local install_dir="${WORK_DIR}/install-nghttp2-${abi}"

  if [[ -f "${install_dir}/lib/libnghttp2.a" ]]; then
    return
  fi

  rm -rf "${build_dir}" "${install_dir}"
  cmake -S "${WORK_DIR}/nghttp2-${NGHTTP2_VERSION}" -B "${build_dir}" -G Ninja \
    -DCMAKE_TOOLCHAIN_FILE="${TOOLCHAIN_FILE}" \
    -DANDROID_ABI="${abi}" \
    -DANDROID_PLATFORM="android-${API_LEVEL}" \
    -DENABLE_LIB_ONLY=ON \
    -DENABLE_APP=OFF \
    -DENABLE_EXAMPLES=OFF \
    -DENABLE_HPACK_TOOLS=OFF \
    -DBUILD_SHARED_LIBS=OFF \
    -DBUILD_STATIC_LIBS=ON \
    -DBUILD_TESTING=OFF \
    -DENABLE_FAILMALLOC=OFF \
    -DENABLE_DOC=OFF \
    -DCMAKE_INSTALL_PREFIX="${install_dir}"
  cmake --build "${build_dir}"
  cmake --install "${build_dir}"
}

build_curl() {
  local abi="$1"
  local build_dir="${WORK_DIR}/build-curl-${abi}"
  local install_dir="${WORK_DIR}/install-curl-${abi}"

  if [[ -x "${build_dir}/src/curl" && -f "${build_dir}/lib/libcurl.a" ]]; then
    return
  fi

  rm -rf "${build_dir}" "${install_dir}"
  cmake -S "${WORK_DIR}/curl-${CURL_VERSION}" -B "${build_dir}" -G Ninja \
    -DCMAKE_TOOLCHAIN_FILE="${TOOLCHAIN_FILE}" \
    -DANDROID_ABI="${abi}" \
    -DANDROID_PLATFORM="android-${API_LEVEL}" \
    -DBUILD_SHARED_LIBS=OFF \
    -DBUILD_STATIC_LIBS=ON \
    -DBUILD_CURL_EXE=ON \
    -DBUILD_TESTING=OFF \
    -DCURL_USE_OPENSSL=ON \
    -DUSE_NGHTTP2=ON \
    -DUSE_LIBIDN2=OFF \
    -DUSE_QUICHE=OFF \
    -DUSE_NGTCP2=OFF \
    -DUSE_MSH3=OFF \
    -DCURL_ZLIB=OFF \
    -DENABLE_ARES=OFF \
    -DHAVE_GETHOSTBYNAME_R=ON \
    -DHAVE_GETHOSTBYNAME_R_3=OFF \
    -DHAVE_GETHOSTBYNAME_R_5=OFF \
    -DHAVE_GETHOSTBYNAME_R_6=ON \
    -DHAVE_GETHOSTBYNAME_R_3_REENTRANT=OFF \
    -DHAVE_GETHOSTBYNAME_R_5_REENTRANT=OFF \
    -DHAVE_GETHOSTBYNAME_R_6_REENTRANT=ON \
    -DHAVE_STRERROR_R=ON \
    -DHAVE_GLIBC_STRERROR_R=OFF \
    -DHAVE_POSIX_STRERROR_R=ON \
    -DOPENSSL_USE_STATIC_LIBS=TRUE \
    -DOPENSSL_ROOT_DIR="${WORK_DIR}/install-openssl-${abi}" \
    -DOPENSSL_INCLUDE_DIR="${WORK_DIR}/install-openssl-${abi}/include" \
    -DOPENSSL_SSL_LIBRARY="${WORK_DIR}/install-openssl-${abi}/lib/libssl.a" \
    -DOPENSSL_CRYPTO_LIBRARY="${WORK_DIR}/install-openssl-${abi}/lib/libcrypto.a" \
    -DNGHTTP2_INCLUDE_DIR="${WORK_DIR}/install-nghttp2-${abi}/include" \
    -DNGHTTP2_LIBRARY="${WORK_DIR}/install-nghttp2-${abi}/lib/libnghttp2.a" \
    -DCMAKE_INSTALL_PREFIX="${install_dir}"
  cmake --build "${build_dir}"
  cmake --install "${build_dir}"
}

generate_supported_options() {
  local output_file="${MAIN_ASSET_DIR}/supported-options.txt"
  mkdir -p "${MAIN_ASSET_DIR}"
  python3 - <<'PY' "${WORK_DIR}/curl-${CURL_VERSION}/src/tool_listhelp.c" "${output_file}"
import re
import sys
from pathlib import Path

source = Path(sys.argv[1]).read_text()
output = Path(sys.argv[2])
options = set()
for match in re.finditer(r'\{"([^"]+)"', source):
    text = match.group(1).strip()
    if not text.startswith("-"):
        continue
    parts = [part.strip() for part in text.split(",")]
    for part in parts:
        if part.startswith("-"):
            options.add(part)

options.discard("--http3")
options.discard("--http3-only")
output.write_text("\n".join(sorted(options)) + "\n")
PY
}

copy_outputs() {
  local abi="$1"
  local prebuilt_abi_dir="${PREBUILT_DIR}/${abi}"
  local asset_root
  local jnilib_root
  if [[ "${abi}" == "x86_64" ]]; then
    asset_root="${DEBUG_ASSET_DIR}"
    jnilib_root="${DEBUG_JNILIBS_DIR}"
  else
    asset_root="${MAIN_ASSET_DIR}"
    jnilib_root="${MAIN_JNILIBS_DIR}"
  fi

  mkdir -p "${prebuilt_abi_dir}/lib" "${asset_root}/${abi}" "${jnilib_root}/${abi}" "${PREBUILT_DIR}/include"
  cp "${WORK_DIR}/build-curl-${abi}/lib/libcurl.a" "${prebuilt_abi_dir}/lib/"
  cp "${WORK_DIR}/install-openssl-${abi}/lib/libssl.a" "${prebuilt_abi_dir}/lib/"
  cp "${WORK_DIR}/install-openssl-${abi}/lib/libcrypto.a" "${prebuilt_abi_dir}/lib/"
  cp "${WORK_DIR}/install-nghttp2-${abi}/lib/libnghttp2.a" "${prebuilt_abi_dir}/lib/"
  cp "${WORK_DIR}/build-curl-${abi}/src/curl" "${asset_root}/${abi}/curl"
  cp "${WORK_DIR}/build-curl-${abi}/src/curl" "${jnilib_root}/${abi}/libcurl_exec.so"

  if [[ ! -d "${PREBUILT_DIR}/include/curl" ]]; then
    cp -R "${WORK_DIR}/install-curl-${abi}/include/curl" "${PREBUILT_DIR}/include/"
  fi
}

download_cacert() {
  mkdir -p "${MAIN_ASSET_DIR}"
  download_if_missing "${CACERT_URL}" "${MAIN_ASSET_DIR}/cacert.pem"
}

main() {
  require_tools
  if [[ ! -f "${TOOLCHAIN_FILE}" ]]; then
    echo "Android NDK toolchain file not found at ${TOOLCHAIN_FILE}" >&2
    exit 1
  fi

  mkdir -p "${WORK_DIR}" "${PREBUILT_DIR}" "${MAIN_ASSET_DIR}" "${DEBUG_ASSET_DIR}"
  download_if_missing "${OPENSSL_URL}" "${WORK_DIR}/${OPENSSL_ARCHIVE}"
  download_if_missing "${NGHTTP2_URL}" "${WORK_DIR}/${NGHTTP2_ARCHIVE}"
  download_if_missing "${CURL_URL}" "${WORK_DIR}/${CURL_ARCHIVE}"

  extract_if_missing "${WORK_DIR}/${NGHTTP2_ARCHIVE}" "${WORK_DIR}/nghttp2-${NGHTTP2_VERSION}"
  extract_if_missing "${WORK_DIR}/${CURL_ARCHIVE}" "${WORK_DIR}/curl-${CURL_VERSION}"

  for abi in "${ABIS[@]}"; do
    echo "=== Building native curl dependencies for ${abi} ==="
    build_openssl "${abi}"
    build_nghttp2 "${abi}"
    build_curl "${abi}"
    copy_outputs "${abi}"
  done

  generate_supported_options
  download_cacert
}

main "$@"
