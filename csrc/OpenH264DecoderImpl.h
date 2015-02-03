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
