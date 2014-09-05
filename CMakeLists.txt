cmake_minimum_required(VERSION 2.812)

SET(OPENH264_HOME ~/workspace/openh264 CACHE FILEPATH "Path to openh264 sources")
SET(LIBYUV_HOME ~/workspace/libyuv/trunk/ CACHE FILEPATH "Path to libyuv sources")

IF(CMAKE_INSTALL_PREFIX_INITIALIZED_TO_DEFAULT)
  SET(CMAKE_INSTALL_PREFIX
    ${CMAKE_SOURCE_DIR} CACHE PATH "CommonWalkingControlModules install prefix" FORCE
    )
ENDIF(CMAKE_INSTALL_PREFIX_INITIALIZED_TO_DEFAULT)
SET(CMAKE_BUILD_TYPE Release)
find_package(JNI REQUIRED)
include_directories(${JNI_INCLUDE_DIRS}) 

if(APPLE)
	# clang complains about missing lookups, which are a lot in libyuv
	SET(CMAKE_MODULE_LINKER_FLAGS "-undefined dynamic_lookup")
ELSE(UNIX)
	SET(CMAKE_INSTALL_RPATH "\$ORIGIN")
endif(APPLE)
add_subdirectory (swig)

