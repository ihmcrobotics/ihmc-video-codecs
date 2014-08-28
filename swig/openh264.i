%module OpenH264
%include "enums.swg"
%include "typemaps.i"
%include "various.i"
%include "arrays_java.i"

%include "ignore.i"
%apply unsigned char *NIOBUFFER { unsigned char* };

%javaconst(1);
%{
#include "codec_api.h"
#include "STargetPicture.hpp"
#include "wels.hpp"
%}

%include "codec_api.h"
%include "codec_app_def.h"
%include "codec_def.h"
%include "STargetPicture.hpp"
%include "wels.hpp"

%include "SSourcePicture.i"
%include "ISVCDecoder.i"
%include "ISVCEncoder.i"
%include "SLayerInfo.i"
%include "SFrameBSInfo.i"
%include "SBufferInfo.i"
%include "SliceInformation.i"
%include "SEncParamExt.i"
