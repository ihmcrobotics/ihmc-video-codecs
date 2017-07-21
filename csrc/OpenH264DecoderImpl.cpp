#include <OpenH264DecoderImpl.h>
#include <iostream>
#include <string.h>

OpenH264DecoderImpl::OpenH264DecoderImpl() : pParam({0}) {
	WelsCreateDecoder(&isvcDecoder);
	pParam.uiTargetDqLayer = 255;
	pParam.eEcActiveIdc = ERROR_CON_SLICE_COPY;
	pParam.bParseOnly = false;
	pParam.sVideoProperty.eVideoBsType = VIDEO_BITSTREAM_AVC;
	isvcDecoder->Initialize(&pParam);

	memset(&info, 0, sizeof(SBufferInfo));
}

YUVPicture* OpenH264DecoderImpl::decodeFrame(uint8* frame, int srcLength) {

	DECODING_STATE state = isvcDecoder->DecodeFrame2(frame, srcLength, pData, &info);

	if (state != dsErrorFree) {
		std::cerr << "Cannot decode frame: " << state << std::endl;
		return NULL;
	}

	if (info.iBufferStatus == 0) {
		switch (frame[3]) {
		case 1: //CODED_SLICE_NON_IDR_PICTURE:
		case 2: //CODED_SLICE_DATA_PARTITION_A:
		case 3: //CODED_SLICE_DATA_PARTITION_B:
		case 4: //CODED_SLICE_DATA_PARTITION_C:
		case 5: //CODED_SLICE_IDR_PICTURE:
			isvcDecoder->DecodeFrame2(NULL, 0, pData, &info);
			break;
		}
	}

	if (info.iBufferStatus == 1) {
		int width = info.UsrData.sSystemBuffer.iWidth;
		int height = info.UsrData.sSystemBuffer.iHeight;
		int *stride = info.UsrData.sSystemBuffer.iStride;

		return new YUVPicture(YUVPicture::YUV420, width, height, stride[0], stride[1], stride[1], pData[0], pData[1], pData[2]);
	} else {
		return NULL;
	}

}

void OpenH264DecoderImpl::skipFrame(uint8* frame, int srcLength) {
	DECODING_STATE state = isvcDecoder->DecodeFrame2(frame, srcLength, pData, &info);

	if (state != dsErrorFree) {
		std::cerr << "Cannot decode frame: " << state << std::endl;
		return;
	}
}

OpenH264DecoderImpl::~OpenH264DecoderImpl() {
	isvcDecoder->Uninitialize();
	WelsDestroyDecoder(isvcDecoder);
}
