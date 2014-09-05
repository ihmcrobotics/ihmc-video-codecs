%module libyuv
%include "enums.swg"
%include "arrays_java.i"
%include "typemaps.i"
%include "various.i"
%apply unsigned char *NIOBUFFER { unsigned char* };

%typemap(jni) enum RotationMode "RotationModeEnum";
%typemap(jstype) enum RotationMode "RotationModeEnum";

%ignore libyuv::MJpegDecoder::DecodeToBuffers;
%ignore libyuv::MJpegDecoder::DecodeToCallback;
%ignore libyuv::MJpegDecoder::JpegSubsamplingTypeHelper;

%javaconst(1);
%{
#define LIBYUV_DISABLE_NEON
#include "libyuv.h"
using namespace libyuv;
%}


%include "libyuv/basic_types.h"
%include "libyuv/compare.h"
%include "libyuv/convert.h"
%include "libyuv/convert_argb.h"
%include "libyuv/convert_from.h"
%include "libyuv/convert_from_argb.h"
%include "libyuv/format_conversion.h"
%include "libyuv/mjpeg_decoder.h"
%include "libyuv/planar_functions.h"
%include "libyuv/rotate.h"
%include "libyuv/rotate_argb.h"
%include "libyuv/row.h"
%include "libyuv/scale.h"
%include "libyuv/scale_argb.h"
%include "libyuv/scale_row.h"
%include "libyuv/version.h"

%extend libyuv::MJpegDecoder{
	int Decode(uint8* Y, uint8* U, uint8* V)
	{
		uint8* planes[] = { Y, U, V };
		return $self->DecodeToBuffers(planes, $self->GetWidth(), $self->GetHeight());
	}
}
