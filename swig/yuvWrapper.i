
%ignore RGBPicture::getBuffer;
%ignore YUVPicture::getY;
%ignore YUVPicture::getU;
%ignore YUVPicture::getV;

%javaconst(1);
%{
#include "YUVPicture.h"
#include "RGBPicture.h"
#include "JPEGDecoderImpl.h"
#include "JPEGEncoderImpl.h"

%}

namespace libyuv
{
	typedef enum FilterMode {
	kFilterNone = 0, // Point sample; Fastest.
	kFilterLinear = 1, // Filter horizontally only.
	kFilterBilinear = 2, // Faster than box, but lower quality scaling down.
	kFilterBox = 3 // Highest quality.
	} FilterModeEnum;
}

%newobject  YUVPicture::toRGB;
%newobject RGBPicture::toYUV;
%newobject JPEGDecoderImpl::decode;

%include "YUVPicture.h"
%include "RGBPicture.h"
%include "JPEGDecoderImpl.h"
%include "JPEGEncoderImpl.h"
