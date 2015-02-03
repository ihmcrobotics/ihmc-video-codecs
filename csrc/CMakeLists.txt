cmake_minimum_required(VERSION 2.8)

IF(WIN32)
	LINK_DIRECTORIES("${LIBYUV_HOME}/out/Release_x64")
	LINK_DIRECTORIES("${LIBYUV_HOME}/out/Release_x64/obj/third_party/libjpeg_turbo")
ELSE()
	LINK_DIRECTORIES("${LIBYUV_HOME}/out/Release")
	LINK_DIRECTORIES("${LIBYUV_HOME}/out/Release/obj/third_party/libjpeg_turbo")
ENDIF(WIN32)

SET(CMAKE_INCLUDE_CURRENT_DIR ON)
INCLUDE_DIRECTORIES("${LIBYUV_HOME}/include")

add_library(yuvPicture OBJECT RGBPicture.cpp YUVPicture.cpp JPEGDecoderImpl.cpp)
set_property(TARGET yuvPicture PROPERTY POSITION_INDEPENDENT_CODE TRUE)

INCLUDE_DIRECTORIES("${OPENH264_HOME}/codec/api/svc")
LINK_DIRECTORIES("${OPENH264_HOME}")

add_library(openH264WrapperImpl OBJECT OpenH264DecoderImpl.cpp OpenH264EncoderImpl.cpp)
set_property(TARGET openH264WrapperImpl PROPERTY POSITION_INDEPENDENT_CODE TRUE)

