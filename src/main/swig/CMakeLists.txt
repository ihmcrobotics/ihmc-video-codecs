cmake_minimum_required(VERSION 2.8)

FIND_PACKAGE(SWIG REQUIRED)
INCLUDE(${SWIG_USE_FILE})
SET(CMAKE_SWIG_OUTDIR "${CMAKE_CURRENT_SOURCE_DIR}/generated/us/ihmc/codecs/generated")

add_custom_target(version COMMAND ${CMAKE_COMMAND} -P ${CMAKE_SOURCE_DIR}/version.cmake WORKING_DIRECTORY ${CMAKE_SOURCE_DIR})

INCLUDE_DIRECTORIES("${PROJECT_SOURCE_DIR}/csrc") 
INCLUDE_DIRECTORIES("${OPENH264_HOME}/codec/api/svc")

LINK_DIRECTORIES("${OPENH264_HOME}")

IF(WIN32)
	LINK_DIRECTORIES("${LIBYUV_HOME}/out/Release/clang_x64/obj")
	LINK_DIRECTORIES("${LIBYUV_HOME}/out/Release/obj/third_party/libjpeg_turbo")
ELSE()
	LINK_DIRECTORIES("${LIBYUV_HOME}/out/Release/obj")
	LINK_DIRECTORIES("${LIBYUV_HOME}/out/Release/obj/third_party/libjpeg_turbo")
ENDIF(WIN32)

INCLUDE_DIRECTORIES("${LIBYUV_HOME}/third_party/libjpeg_turbo")
INCLUDE_DIRECTORIES("${LIBYUV_HOME}/include")
INCLUDE_DIRECTORIES("${LIBYUV_HOME}/chromium/src/third_party/libjpeg_turbo/")
SET_SOURCE_FILES_PROPERTIES(ihmcVideoCodecs.i PROPERTIES CPLUSPLUS ON)
SET_PROPERTY(SOURCE ihmcVideoCodecs.i PROPERTY SWIG_FLAGS "-package" "us.ihmc.codecs.generated")
SWIG_ADD_MODULE(ihmcVideoCodecs java ihmcVideoCodecs.i $<TARGET_OBJECTS:yuvPicture> $<TARGET_OBJECTS:openH264WrapperImpl>)
ADD_DEPENDENCIES(ihmcVideoCodecs version)
IF(WIN32)
	SWIG_LINK_LIBRARIES(ihmcVideoCodecs openh264 libyuv_internal.lib libjpeg.lib simd.lib simd_asm.lib msvcrt.lib)
ELSE()
	SWIG_LINK_LIBRARIES(ihmcVideoCodecs openh264 libyuv_internal.a libjpeg.a libsimd.a libsimd_asm.a)
ENDIF(WIN32)
INSTALL(TARGETS ihmcVideoCodecs RUNTIME DESTINATION resources LIBRARY DESTINATION resources)

if(APPLE)
	SET_TARGET_PROPERTIES(ihmcVideoCodecs PROPERTIES INSTALL_RPATH "@loader_path/")
	add_custom_command(TARGET ihmcVideoCodecs POST_BUILD COMMAND ${CMAKE_INSTALL_NAME_TOOL} -change "/usr/local/lib/libopenh264.4.dylib" @rpath/libopenh264.4.dylib $<TARGET_FILE:ihmcVideoCodecs>)
endif(APPLE)

FIND_PACKAGE(X11)

IF(X11_FOUND AND NOT APPLE)
SET(CMAKE_SWIG_OUTDIR "${CMAKE_CURRENT_SOURCE_DIR}/generated/us/ihmc/codecs/screenCapture")

SET_SOURCE_FILES_PROPERTIES(ScreenShot.i PROPERTIES CPLUSPLUS ON)
SET_PROPERTY(SOURCE ScreenShot.i PROPERTY SWIG_FLAGS "-package" "us.ihmc.codecs.screenCapture")
INCLUDE_DIRECTORIES(X11_INCLUDE_DIR)
SWIG_ADD_MODULE(screenshot java ScreenShot.i)
ADD_DEPENDENCIES(screenshot version)
SWIG_LINK_LIBRARIES(screenshot ${X11_LIBRARIES})
INSTALL(TARGETS screenshot RUNTIME DESTINATION resources LIBRARY DESTINATION resources)
ENDIF(X11_FOUND AND NOT APPLE)
