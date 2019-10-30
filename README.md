# IHMC Java Video Codecs

This library provides Java support for the following video formats
- MP4 (Based on JCodec)
- MJPEG (libJPEG-Turbo)
- H264 (OpenH264)

Native code is used for the actual encoding and decoding of the video formats. Only video support is provided for now. 

H264 support is provided using OpenH264. A downloader for the OpenH264 library is included, and will prompt the user to accept the license. This means all royalities for the H264 codec are taken care off by Cisco. 



## Philosophy

All code is released under a permissive Apache 2.0 license. This library tries to avoid problems with licensing patents, for example trough the use of the OpenH264 module.

Simple, well defined classes to support the functionality needed are written in C++. SWIG is used to bridge our C++ classes to Java. Only basic features are supported, keeping the API simple and maintainable.

Only video is supported.

## Features

- Video tracks only
- Supports RGB <-> YUV conversion using libyuv (BSD license)
- Supports JPEG encoding/decoding using libjpeg-turbo (BSD/IJG license)
- Bridge to OpenH264 (BSD License)
	- Automatic downloading of Cisco licensed binary
- MP4 support based on JCodec (BSD License)
	- Mux: H264, MJPEG
	- Demux: H264, MJPEG 
- Fast screenshot support using native code
	- Linux only
	- Fallback using AWT Robot for Mac/Windows


## Usage
A pre-compiled version is avaiable on [https://bintray.com/ihmcrobotics/maven-release/IHMCVideoCodecs](https://bintray.com/ihmcrobotics/maven-release/IHMCVideoCodecs). You can either use Gradle or Maven to add IHMC Video codecs to your project.

### Gradle
Add the IHMC Video Codecs library as dependency
```
repositories {
    maven {
        url  "https://dl.bintray.com/ihmcrobotics/maven-release"
    }
}
	
dependencies {
	compile group: 'us.ihmc', name: 'IHMCVideoCodecs', version: '2.1'
}
```

### Examples
See the examples directory

## Headless systems
To accept the license on a system without console input, add "-Dopenh264.license=accept" to your Java command line arguments.

## Contributions
Contributions are welcome. We are a robot lab, supporting video codecs is not our main focus. Please provide pull requests. If you or your organization is interested in taking over maintainance, please contact us. 

## Compile native code

For your convenience compiled libraries are placed in the resources directory. Compiled libraries are available for
- Windows (64 bit) (You might need to install [Microsoft Visual C++ Redistributable for Visual Studio 2017](https://go.microsoft.com/fwlink/?LinkId=746572])
- Mac OS X (64 bit)
- Linux (64 bit) 


If you want to use this library on another platform, you have to compile it yourself. Follow the instructions most appropriatly for your platform.

### Dependencies
- LibYUV git #d0ed025447622c570a76bcf2ac88c30209c780a0
- OpenH264 v1.7 
- Swig 3.0.3 or higher

### Linux
- Install OpenJDK (>=8)
- Compile LibYUV following https://chromium.googlesource.com/libyuv/libyuv/+/master/docs/getting_started.md
	- 64 Bit release build
- Download and compile the openH264 sources (version 1.7) from https://github.com/cisco/openh264
	- git clone https://github.com/cisco/openh264.git
	- cd openh264
	- git checkout openh264v1.7
	- make
- Install swig 3.0.3 or higher 
- Go to IHMCVideoCodecs directory
	- mkdir build
	- cd build
	- ccmake ..
		- Set CMAKE_BUILD_TYPE to Release
		- Set LIBYUV_HOME to the libyuv trunk directory
		- Set OPENH264_HOME to the openh264 source directory
	- make
	- make install

### Mac
- Install the JDK for Mac (>=8)
- Install homebrew
	- brew install nasm automake pcre cmake swig
- Download and compile the openH264 sources (version 1.7) from https://github.com/cisco/openh264
	- git clone https://github.com/cisco/openh264.git
	- cd openh264
	- git checkout openh264v1.7
	- make
- Compile LibYUV following https://chromium.googlesource.com/libyuv/libyuv/+/master/docs/getting_started.md
	- 64Bit release build
- Go to IHMCVideoCodecs directory
	- mkdir build
	- cd build
	- ccmake ..
		- Set CMAKE_BUILD_TYPE to Release
		- Set LIBYUV_HOME to the libyuv trunk directory
		- Set OPENH264_HOME to the openh264 source directory
	- make
	- make install


### Windows

- Install Visual Studio Community 2017
	- Continue while this is downloading and get some coffee
- Install Windows SDK for WIndows 10
	- https://developer.microsoft.com/en-us/windows/hardware/windows-driver-kit
	- You only need the "Debugging tools for windows"
- Install the 64 bit JDK for Windows (>=8)
- Install CMake using the installer http://www.cmake.org/download/
- Download and unpack swigwin 3.0.3 or higher
	- http://sourceforge.net/projects/swig/files/swigwin/swigwin-3.0.3/
- Install Libyuv
	- Follow instructions on https://chromium.googlesource.com/libyuv/libyuv/+/master/docs/getting_started.md
		- Use the Windows Release build for x64
		- Before running "gclient sync" set the following env variables
			- set GYP_MSVS_VERSION=2017
			- set DEPOT_TOOLS_WIN_TOOLCHAIN=0
- Install MinGW (you only need to select msys-base)
	- Use mingw-get-setup.exe from http://sourceforge.net/projects/mingw/files/Installer/
- Install nasm for windows 64 bit from http://www.nasm.us/
- Setup %Path% variables, add the following directories
	- C:\MinGW\msys\1.0\bin
	- C:\Program Files\NASM
- Download the openh264 sources, tag v1.7
	- Use git
		- git clone https://github.com/cisco/openh264.git
		- cd openh264
		- git checkout openh264v1.7
	- Read the instructions in the openh264 README.md
	- Start the 64 Native  Tools Command Prompt for VS 2017 (Start -> Visual Studio 2017 -> Visual Studio Tools)
	- cd to the openh264 dir 
	- make OS=msvc ENABLE64BIT=Yes
- Start the cmake-gui
	- Point source directory to IHMCVideoCodecs sources
	- Point build directory to [sources]/build
	- Configure
		- Choose the Visual Studio 15 2017 Win64 generator
		- Choose Default native toolchain
		- Set all paths (LIBYUV_HOME, OPENH264_HOME, SWIG_EXECUTABLE) correctly
		- Configure
	- Generate
- In the command prompt go to IHMCVideoCodecs/build
	- Run "C:\Program Files\CMake\bin\cmake.exe" --build . --config Release --target install
## Publishing

To publish to bintray run 
- gradle bintrayUpload
