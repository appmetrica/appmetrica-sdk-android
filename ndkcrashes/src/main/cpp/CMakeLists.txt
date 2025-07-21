# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html.
# For more examples on how to use CMake, see https://github.com/android/ndk-samples.

# Sets the minimum CMake version required for this project.
cmake_minimum_required(VERSION 3.31.6)

# Declares the project name. The project name can be accessed via ${ PROJECT_NAME},
# Since this is the top level CMakeLists.txt, the project name is also accessible
# with ${CMAKE_PROJECT_NAME} (both CMake variables are in-sync within the top level
# build script scope).
project("analytics-ndk-crashes")

# external directories with headers
include_directories(
        ${CRASHPAD_SOURCE_DIR}/
        ${CRASHPAD_SOURCE_DIR}/third_party/mini_chromium/mini_chromium/
        ${CRASHPAD_BUILD_DIR}/${ANDROID_ABI}/gen/
)

find_library(log-lib log)

# libraries from sources
add_library(
        appmetrica-native
        SHARED
        # source files
        core/AppMetricaCrashpadConfig.cpp
        core/NativeCrashCoreJni.cpp
        core/NativeCrashUtilsJni.cpp
        utils/JNIUtils.cpp
)

add_library(
        appmetrica-native-runner
        SHARED
        # source files
        runner/NativeCrashHandlerRunnerJni.cpp
        utils/JNIUtils.cpp
)

add_library(
        appmetrica-service-native
        SHARED
        # source files
        service/CrashpadCrash.cpp
        service/NativeCrashDatabase.cpp
        service/NativeCrashServiceJni.cpp
        utils/JNIUtils.cpp
        utils/ArrayList.cpp
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
        appmetrica-native-runner

        ${log-lib}
)

target_link_libraries(
        appmetrica-service-native

        ${log-lib}
        crashpad_common
        crashpad_util
        minichromium_base
)
