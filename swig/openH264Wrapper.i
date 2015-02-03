%module openH264Wrapper
%include "enums.swg"
%include "arrays_java.i"
%include "typemaps.i"
%include "various.i"
%apply unsigned char *NIOBUFFER { uint8* };


%javaconst(1);
%{
	#include "OpenH264DecoderImpl.h"
%}

%newobject  OpenH264DecoderImpl::decodeFrame;

%include "OpenH264DecoderImpl.h"
