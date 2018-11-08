cmake_minimum_required(VERSION 3.4.1)


set(CMAKE_VERBOSE_MAKEFILE on)
#set(assimpDir /src/main/cpp/externals/assimp)
#set(assimpInclude ${assimpDir}/include/assimp)
#include_directories(${assimpInclude})
include_directories(${CMAKE_SOURCE_DIR}/src/main/cpp/externals/assimp/include/assimp)

add_library(
        # Sets the name of the library.
        assimp-jni

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).

        src/main/jni/cmakejnitestactivity.cpp)

add_library(libassimp-shared SHARED IMPORTED)
set_target_properties(
        libassimp-shared

        PROPERTIES
        IMPORTED_LOCATION

        ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libassimp.so
)
#
#find_library(
#        EGL-lib
#
#        EGL
#)
#find_library(
#        GLESv2-lib
#
#        GLESv2
#)
#find_library(
#        stdc++-lib
#
#        stdc++
#)


find_library(android-lib android)


find_library(log-lib log)

target_link_libraries(
        assimp-jni
        #        libassimp-shared
        libassimp-shared
        ${android-lib}
        ${log-lib}
#        ${EGL-lib}
#        ${GLESv2-lib}
#        ${stdc++-lib}
)