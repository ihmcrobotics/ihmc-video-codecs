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
#ifndef RGBPICTURE_H
#define RGBPICTURE_H

#define LIBYUV_DISABLE_NEON
#include <libyuv/basic_types.h>
#include <YUVPicture.h>

class RGBPicture {
private:
	int width;
	int height;
	uint8* buffer;

public:
	RGBPicture(int width, int height);

	uint8* getBuffer();

	void put(uint8* src);
	void get(uint8* target);

	int getWidth();
	int getHeight();

	YUVPicture* toYUV(YUVPicture::YUVSubsamplingType samplingType);

	~RGBPicture();
};
#endif
