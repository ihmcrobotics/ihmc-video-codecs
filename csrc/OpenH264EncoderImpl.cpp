#include "OpenH264EncoderImpl.h"
#include <string.h>
#include <iostream>

void OpenH264EncoderImpl::setOptionParamext() {
	if (initialized) {
		int ret = isvcEncoder->SetOption(ENCODER_OPTION_SVC_ENCODE_PARAM_EXT, &param);
		if (ret != 0) {
			std::cerr << "Cannot set option: " << ret << std::endl;
		}
	}

}

OpenH264EncoderImpl::OpenH264EncoderImpl() :
		initialized(false), layerIndex(0), nalIndex(0) {
	WelsCreateSVCEncoder(&isvcEncoder);
	isvcEncoder->GetDefaultParams(&param);
}

void OpenH264EncoderImpl::setUsageType(EUsageType usage) {
	param.iUsageType = usage;
	setOptionParamext();
}

void OpenH264EncoderImpl::setSize(int width, int height) {
	param.iPicWidth = width;
	param.iPicHeight = height;
	for (int i = 0; i < param.iSpatialLayerNum; i++) {
		param.sSpatialLayers[i].iVideoWidth = width >> (param.iSpatialLayerNum - 1 - i);
		param.sSpatialLayers[i].iVideoHeight = height >> (param.iSpatialLayerNum - 1 - i);
	}
	setOptionParamext();
}

void OpenH264EncoderImpl::setBitRate(int bitrate) {
	for (int i = 0; i < param.iSpatialLayerNum; i++) {
		param.sSpatialLayers[i].iSpatialBitrate = bitrate / param.iSpatialLayerNum;
	}
	param.iTargetBitrate = bitrate;
	setOptionParamext();
}

void OpenH264EncoderImpl::setRCMode(RC_MODES mode) {
	param.iRCMode = mode;
	setOptionParamext();
}

void OpenH264EncoderImpl::setMaxFrameRate(float rate) {
	param.fMaxFrameRate = rate;
	for (int i = 0; i < param.iSpatialLayerNum; i++) {
		param.sSpatialLayers[i].fFrameRate = rate;
	}
	setOptionParamext();
}

void OpenH264EncoderImpl::setComplexityMode(ECOMPLEXITY_MODE mode) {
	param.iComplexityMode = mode;
	setOptionParamext();
}

void OpenH264EncoderImpl::setIntraPeriod(int period) {
	param.uiIntraPeriod = (unsigned int) period;
	setOptionParamext();
}

void OpenH264EncoderImpl::setEnableSpsPpsIdAddition(bool enable) {
        if(enable)
        {
            param.eSpsPpsIdStrategy = INCREASING_ID;
        }
        else
        {
            param.eSpsPpsIdStrategy = CONSTANT_ID;
        }
	setOptionParamext();
}

void OpenH264EncoderImpl::setEnableFrameSkip(bool enable) {
	param.bEnableFrameSkip = enable;
	setOptionParamext();
}

void OpenH264EncoderImpl::setMaxBitrate(int maxBitrate) {
	param.iMaxBitrate = maxBitrate;
	setOptionParamext();
}

void OpenH264EncoderImpl::setMaxQp(int maxQp) {
	param.iMaxQp = maxQp;
	setOptionParamext();
}

void OpenH264EncoderImpl::setMinQp(int minQp) {
	param.iMinQp = minQp;
	setOptionParamext();
}

void OpenH264EncoderImpl::setEnableDenoise(bool enable) {
	param.bEnableDenoise = enable;
	setOptionParamext();
}

bool OpenH264EncoderImpl::initialize() {
	int rv = isvcEncoder->InitializeExt(&param);
	if (rv != 0) {
		return false;
	}
	int videoFormat = videoFormatI420;
	isvcEncoder->SetOption(ENCODER_OPTION_DATAFORMAT, &videoFormat);
	initialized = true;

	return true;
}

bool OpenH264EncoderImpl::encodeFrameImpl(YUVPicture* frame) {
	memset(&info, 0, sizeof(SFrameBSInfo));
	memset(&pic, 0, sizeof(SSourcePicture));

	frame->toYUV420();

	pic.iPicWidth = frame->getWidth();
	pic.iPicHeight = frame->getHeight();
	pic.iColorFormat = videoFormatI420;
	pic.iStride[0] = frame->getYStride();
	pic.iStride[1] = frame->getUStride();
	pic.iStride[2] = frame->getVStride();

	pic.pData[0] = frame->getY();
	pic.pData[1] = frame->getU();
	pic.pData[2] = frame->getV();

	int e = isvcEncoder->EncodeFrame(&pic, &info);
	if (e != 0) {
		std::cerr << "Cannot encode frame: " + e << std::endl;
		return false;
	}

	switch (info.eFrameType) {
	case videoFrameTypeInvalid:
		std::cerr << "Encoder not ready or parameters are invalid" << std::endl;
		return false;
	case videoFrameTypeSkip:
	case videoFrameTypeI:
	case videoFrameTypeIDR:
	case videoFrameTypeP:
	case videoFrameTypeIPMixed:
		layerIndex = 0;
		nalIndex = -1;
		return true;
		break;
	}

	std::cerr << "Should not get here. Update C++ code!" << std::endl;
	return false;
}

bool OpenH264EncoderImpl::nextNAL() {
	++nalIndex;
	if (nalIndex < info.sLayerInfo[layerIndex].iNalCount) {
		return true;
	}

	nalIndex = 0;
	while (++layerIndex < info.iLayerNum) {
		if (info.sLayerInfo[layerIndex].iNalCount > 0) {
			return true;
		}
	}

	return false;
}

int OpenH264EncoderImpl::getNALSize() {
	if (layerIndex < 0 || layerIndex >= info.iLayerNum) {
		std::cerr << "No more NALs available." << std::endl;
		return -1;
	}
	if (nalIndex < 0 || nalIndex >= info.sLayerInfo[layerIndex].iNalCount) {
		std::cerr << "No more NALs available or nextNAL() hasn't been called" << std::endl;
		return -1;
	}

	return info.sLayerInfo[layerIndex].pNalLengthInByte[nalIndex];
}

void OpenH264EncoderImpl::getNAL(uint8* buffer, int bufferSize) {
	int nalSize = getNALSize();
	if (nalSize <= 0) {
		return;
	}
	if (bufferSize < getNALSize()) {
		std::cerr << "Buffer is to small for NAL" << std::endl;
		return;
	}
	int offset = 0;
	for (int j = 0; j < nalIndex; j++) {
		offset += info.sLayerInfo[layerIndex].pNalLengthInByte[j];
	}
	memcpy(buffer, info.sLayerInfo[layerIndex].pBsBuf + offset, nalSize);
}

void OpenH264EncoderImpl::sendIntraFrame() {
	isvcEncoder->ForceIntraFrame(true);
}

OpenH264EncoderImpl::~OpenH264EncoderImpl() {
	isvcEncoder->Uninitialize();
	WelsDestroySVCEncoder(isvcEncoder);
}
