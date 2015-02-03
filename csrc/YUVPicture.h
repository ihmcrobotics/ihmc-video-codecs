/*
 *   Copyright 2014 Florida Institute for Human and Machine Cognition (IHMC)
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 *    Written by Jesper Smith with assistance from IHMC team members
 */
#ifndef YUVPICTURE_H
#define YUVPICTURE_H

#define LIBYUV_DISABLE_NEON
#include <libyuv.h>

class RGBPicture;

class YUVPicture {
public:
	enum YUVSubsamplingType {
		YUV444, YUV422, YUV420, UNSUPPORTED
	};
	static YUVSubsamplingType getSubsamplingType(int yWidth, int yHeight, int uWidth, int uHeight, int vWidth, int vHeight);
private:
	static bool isHalf(int orig, int toTest);
	static int divideByTwoRoundUp(int orig);
private:
	YUVSubsamplingType type;
	int width, height;

	int yStride, uStride, vStride;

	uint8* Y;
	uint8* U;
	uint8* V;

public:
	YUVPicture(YUVSubsamplingType type, int width, int height, int yStride, int uStride, int vStride, uint8 *Yin, uint8 *Uin, uint8 *Vin);
	YUVPicture(YUVSubsamplingType type, int width, int height);

	void scale(int newWidth, int newHeight, libyuv::FilterModeEnum filterMode);
	RGBPicture* toRGB();
	YUVSubsamplingType getType();

	uint8* getY();
	uint8* getU();
	uint8* getV();

	int getWidth();
	int getHeight();
	int getYStride();
	int getUStride();
	int getVStride();

	void toYUV420();

	~YUVPicture();
};

#endif
