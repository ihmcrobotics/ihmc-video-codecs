cmake_minimum_required(VERSION 2.8.12)

SET(OPENH264_HOME ~/workspace/openh264 CACHE FILEPATH "Path to openh264 sources")
SET(LIBYUV_HOME ~/workspace/libyuv/trunk/ CACHE FILEPATH "Path to libyuv sources")

IF(CMAKE_INSTALL_PREFIX_INITIALIZED_TO_DEFAULT)
  SET(CMAKE_INSTALL_PREFIX
    ${CMAKE_SOURCE_DIR} CACHE PATH "CommonWalkingControlModules install prefix" FORCE
    )
ENDIF(CMAKE_INSTALL_PREFIX_INITIALIZED_TO_DEFAULT)
SET(CMAKE_BUILD_TYPE Release)

if(MSVC)
#We statically link to reduce dependencies
foreach(flag_var CMAKE_CXX_FLAGS CMAKE_CXX_FLAGS_DEBUG CMAKE_CXX_FLAGS_RELEASE CMAKE_CXX_FLAGS_MINSIZEREL CMAKE_CXX_FLAGS_RELWITHDEBINFO)
    if(${flag_var} MATCHES "/MD")
        string(REGEX REPLACE "/MD" "/MT" ${flag_var} "${${flag_var}}")
    endif(${flag_var} MATCHES "/MD")
    if(${flag_var} MATCHES "/MDd")
        string(REGEX REPLACE "/MDd" "/MTd" ${flag_var} "${${flag_var}}")
    endif(${flag_var} MATCHES "/MDd")
endforeach(flag_var)
endif(MSVC)

find_package(JNI REQUIRED)
include_directories(${JNI_INCLUDE_DIRS}) 

if(APPLE)
	# clang complains about missing lookups, which are a lot in libyuv
	SET(CMAKE_MODULE_LINKER_FLAGS "-undefined dynamic_lookup")
ELSEIF(WIN32)
	SET(CMAKE_MODULE_LINKER_FLAGS "/FORCE:UNRESOLVED /NODEFAULTLIB:libcmt.lib")
ELSEIF(UNIX)
	SET(CMAKE_INSTALL_RPATH "\$ORIGIN")
ENDIF(APPLE)

add_subdirectory(csrc)
add_subdirectory (swig)


