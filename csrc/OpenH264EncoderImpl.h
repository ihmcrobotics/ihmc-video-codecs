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

#ifndef OPENH264ENCODERIMPL_H
#define OPENH264ENCODERIMPL_H

#include "codec_api.h"
#include "YUVPicture.h"

class OpenH264EncoderImpl
{
private:
	bool initialized;
	int layerIndex;
	int nalIndex;

	ISVCEncoder *isvcEncoder;
	SEncParamExt param;
	SFrameBSInfo info;
	SSourcePicture pic;

	void setOptionParamext();
public:
	OpenH264EncoderImpl();
	void setUsageType(EUsageType usage);
	void setSize(int width, int height);
	void setBitRate(int bitrate);
	void setRCMode(RC_MODES mode);
	void setMaxFrameRate(float rate);

	void setComplexityMode(ECOMPLEXITY_MODE mode);
	void setIntraPeriod(int period);
	void setEnableSpsPpsIdAddition(bool enable);

	//RC Control
	void setEnableFrameSkip(bool enable);
	void setMaxBitrate(int maxBitrate);
	void setMaxQp(int maxQp);
	void setMinQp(int minQp);

	// Quality
	void setEnableDenoise(bool enable);

	bool initialize();

	bool encodeFrameImpl(YUVPicture* frame);
	bool nextNAL();

	int getNALSize();
	void getNAL(uint8* buffer, int bufferSize);

	void sendIntraFrame();
	
	void setLevelIDC(ELevelIdc level);
	void setProfileIdc(EProfileIdc profile);

	~OpenH264EncoderImpl();


};


#endif
