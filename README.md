# Java wrapper for common video encoding format

This library enables the use of common video encoding formats used at IHMC in Java. Native code is bridged using SWIG.

## Philosophy

All code is released under a permissive Apache 2.0 license. This library tries to avoid problems with licensing patents, for example trough the use of the OpenH264 module.

SWIG is used to bridge Java to native code. Where SWIG has trouble translating functions in a usable Java format, helper functions are written in C/C++ instead of using advanced SWIG functionality. This should help maintainability.

Only video is supported.

## Features

- Video tracks only
- Supports RGB <-> YUV conversion using libyuv (BSD license)
- Supports JPEG encoding/decoding using libjpeg-turbo (BSD/IJG license)
- Bridge to OpenH264 (BSD License)
- MP4 support
	- Mux: H264, MJPEG
	- Demux: H264, MJPEG 

## Usage
See the examples directory

### Headless systems
To accept the license on a system without console input, add "-Dopenh264.license=accept" to your Java command line arguments.

## Compile native code

For your convenience compiled libraries are placed in the resources directory. It's only necessary to compile native code if you want to change things or port to different platforms.

### Linux
- Install OpenJDK (>6)
- Compile LibYUV following https://code.google.com/p/libyuv/wiki/GettingStarted
	- When ninja complains about tools/sanitizer_options/sanitizer_options.cc get it from the chromium git: https://chromium.googlesource.com/chromium/src/base/+/f64188fb60c527d54bb54f1d107a5b5890a49c89/debug/sanitizer_options.cc
- Download and compile the openH264 sources (version 1.3) from https://github.com/cisco/openh264
	- git clone https://github.com/cisco/openh264.git
	- cd openh264
	- git checkout v1.3
	- make
- Install swig 3.0.3 or higher (Development version as of 5/09/2014)
- Go to IHMCVideoCodecs directory
	- mkdir build
	- cd build
	- ccmake ..
		- Set LIBYUV_HOME to the libyuv trunk directory
		- Set OPENH264_HOMe to the openh264 source directory
	- make
	- make install

### Mac
- Install the JDK for Mac (>6)
- Install homebrew
	- brew install nasm automake pcre cmake
- Add /usr/local/bin to the start of your path (for nasm)
- Install swig 3.0.3 from GIT
	- ./autogen.sh
	- ./configure --with-javaincl=/Library/Java/JavaVirtualMachines/jdk1.8.0_20.jdk/Contents/Home/include/ (change your jdk version to match)
	- make
	- touch CCache/ccache-swig.1
	- make install
- Download and compile the openH264 sources (version 1.1) from https://github.com/cisco/openh264
	- git clone https://github.com/cisco/openh264.git
	- git checkout v1.1
	- cd openh264
	- make
- Compile LibYUV following https://code.google.com/p/libyuv/wiki/GettingStarted
	- mkdir libyuv
	- cd libyuv
	- git clone https://chromium.googlesource.com/chromium/tools/depot_tools.git
	- export PATH=`pwd`/depot_tools:"$PATH"
	- GYP_DEFINES="clang=1 target_arch=x64" ./gyp_libyuv -fninja --depth=. libyuv_test.gyp
	- ninja -j7 -C out/Release

- Go to IHMCVideoCodecs directory
	- mkdir build
	- cd build
	- ccmake ..
		- Set LIBYUV_HOME to the libyuv trunk directory
		- Set OPENH264_HOMe to the openh264 source directory
	- make
	- make install


### Windows

May the force be with you.

- Install Visual Studio Express 2013 for Windows Desktop
	- Continue while this is downloading and get some coffee
- Install the 64 bit JDK for Windows (>6)
- Install CMake using the installer http://www.cmake.org/download/
- Install swig 3.0.3
	- No binaries have been released as of 05/09/2014 yet. Get the swigwin 3.0.2 binary and replace Lib/java/various.i with https://github.com/swig/swig/blob/master/Lib/java/various.i
- Install Libyuv
	- Non-cygwin
	- Follow instructions on https://code.google.com/p/libyuv/wiki/GettingStarted
		- Dowloading the windows sdk takes forever, just have patience
- Install mingw 
- Download the openh264 sources, tag v1.3
	- Following the instructions in openh264 README.md
	- Setup your path to point to msys and mingw
	- Start msys using C:\MinGW\msys\1.0\msys.bat
	- cd to openh264 dir (C:\ is on /c/)
	- Use make OS=msvc ENABLE64Bit=Yes
- Start the cmake-gui
	- Point source directory to IHMCVideoCodecs sources
	- Point build directory to [sources]/build
	- Configure
		- Choose the Visual Studio 12 2014 Win64 generator
		- Choose native toolchain
		- Set all paths correctly
		- Configure
	- Generate
- Go to IHMCVideoCodecs/build
	- Double click on ALL_BUILD.vcxproj
	- Wait for VS2013 to start
	- Select "Release" build type
	- Right click ALL_BUILD and select build
		- Ignore the LNK2019 errors
	- Right click INSTALL and select build

## Publishing

To publish to Nexus, add the following lines to ~/.gradle/gradle.properties

nexus_username=[username]
nexus_password=[password]


Replace [username] and [password] with your nexus credentials

Then execute 

gradle publish
