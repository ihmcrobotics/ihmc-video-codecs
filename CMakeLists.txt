cmake_minimum_required(VERSION 2.8)

SET(OPENH264_HOME /home/jesper/workspace/openh264)
SET(LIBYUV_HOME /home/jesper/workspace/libyuv/trunk/)
find_package(JNI REQUIRED)
include_directories(${JNI_INCLUDE_DIRS}) 
add_subdirectory (swig)
