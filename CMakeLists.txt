cmake_minimum_required(VERSION 2.8)

SET(OPENH264_HOME /Users/dstephen/Developer/Workspaces/IHMC/openh264)
SET(LIBYUV_HOME /Users/dstephen/Developer/Workspaces/IHMC/libyuv/trunk/)
find_package(JNI REQUIRED)
include_directories(${JNI_INCLUDE_DIRS}) 

SET(CMAKE_MODULE_LINKER_FLAGS "-undefined dynamic_lookup")
add_subdirectory (swig)
