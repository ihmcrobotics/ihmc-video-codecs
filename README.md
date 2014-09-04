# Java wrapper for common video encoding format

This library enables the use of common video encoding formats used at IHMC in Java. Native code is bridged using SWIG.

## Philosophy

All code is released under a permissive Apache 2.0 license. This library tries to avoid problems with licensing patents, for example trough the use of the OpenH264 module.

SWIG is used to bridge Java to native code. Where SWIG has trouble translating functions in a usable Java format, helper functions are written in C/C++ instead of using advanced SWIG functionality. This should help maintainability.

Only video is supported.

## Features

- Video tracks only
- Supports RGB <-> YUV conversion using libyuv (BSD license)
- Bridge to OpenH264 (BSD License)
- MP4 support
	- Mux: H264
	- Demux: H264, MJPEG 

## Usage
See the examples directory

## Compile native code

- Compile LibYUV following https://code.google.com/p/libyuv/wiki/GettingStarted
	- When ninja complains about tools/sanitizer_options/sanitizer_options.cc get it from the chromium git: https://chromium.googlesource.com/chromium/src/base/+/f64188fb60c527d54bb54f1d107a5b5890a49c89/debug/sanitizer_options.cc
- Download the openH264 sources (version 1.1) from https://github.com/cisco/openh264
- Install 
- Edit ./CMakeLists.txt and point to the libyuv and openh264 directories
- mkdir build; cmake ..; make


