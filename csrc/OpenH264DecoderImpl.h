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
#ifndef OPENH264DECODERIMPL
#define OPENH264DECODERIMPL

#include <YUVPicture.h>
#include "codec_api.h"

class OpenH264DecoderImpl
{
private:
	ISVCDecoder *isvcDecoder;
	SBufferInfo info;
	unsigned char* pData[3];

public:
	OpenH264DecoderImpl();
	YUVPicture* decodeFrame(uint8* frame, int srcLength);
	void skipFrame(uint8* frame, int srcLength);
	~OpenH264DecoderImpl();
};

#endif
