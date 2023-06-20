# Sets the minimum version of CMake required to build your native library.
# This ensures that a certain set of CMake features is available to
# your build.
cmake_minimum_required(VERSION 3.10.2)

# external directories with headers
include_directories(
        ${CRASHPAD_SOURCE_DIR}/
        ${CRASHPAD_SOURCE_DIR}/third_party/mini_chromium/mini_chromium/
)

find_library(log-lib log)

# libraries from sources
add_library(
        appmetrica-native
        SHARED
        # source files
        AppmetricaCrashpadConfig.cpp
        AppProcessConfig.cpp
        JniUtil.cpp
        NativeUncaughtExceptionHandler.cpp
)
target_compile_definitions(appmetrica-native PRIVATE LIBRARY_VERSION_NAME="${LIBRARY_VERSION_NAME}")

add_library(
        appmetrica-native-runner
        SHARED
        # source files
        JniUtil.cpp
        HandlerRunnerJni.cpp
)

add_library(
        appmetrica-service-native
        SHARED
        # source files
        AppmetricaCrashpadConfig.cpp
        JniUtil.cpp
        NativeCrashReaderJni.cpp
        NativeCrashReader.cpp
)

# imported libraries
set(CRASHPAD_OBJ_DIR ${CRASHPAD_BUILD_DIR}/${ANDROID_ABI}/obj)

add_library(crashpad_client STATIC IMPORTED)
set_property(TARGET crashpad_client PROPERTY IMPORTED_LOCATION ${CRASHPAD_OBJ_DIR}/client/libclient.a)

add_library(crashpad_common STATIC IMPORTED)
set_property(TARGET crashpad_common PROPERTY IMPORTED_LOCATION ${CRASHPAD_OBJ_DIR}/client/libcommon.a)

add_library(crashpad_util STATIC IMPORTED)
set_property(TARGET crashpad_util PROPERTY IMPORTED_LOCATION ${CRASHPAD_OBJ_DIR}/util/libutil.a)

add_library(minichromium_base STATIC IMPORTED)
set_property(TARGET minichromium_base PROPERTY IMPORTED_LOCATION ${CRASHPAD_OBJ_DIR}/third_party/mini_chromium/mini_chromium/base/libbase.a)


# dependencies
target_link_libraries(
        appmetrica-native

        ${log-lib}
        crashpad_client
        crashpad_util
        minichromium_base
)

target_link_libraries(
        appmetrica-service-native

        ${log-lib}
        crashpad_common
        crashpad_util
        minichromium_base
)
