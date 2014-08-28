# Java wrapper for common video encoding format

This library enables the use of common video encoding formats used at IHMC in Java. Native code is bridged using SWIG.

## Philosophy

All code is released under a permissive Apache 2.0 license. This library tries to avoid problems with licensing patents, for example trough the use of the OpenH264 module.

SWIG is used to bridge Java to native code. Where SWIG has trouble translating functions in a usable Java format, helper functions are written in C/C++ instead of using advanced SWIG functionality. This should help maintainablity.

Only video is supported.

## Features

- Supports RGB <-> YUV conversion using libyuv (BSD license)
- Bridge to OpenH264 (BSD License)