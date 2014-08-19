%module codec_api
%include "enums.swg"
%include "arrays_java.i"
%include "typemaps.i"
%include "various.i"




%include "outargs.i"
%include "ignore.i"
%apply unsigned char *NIOBUFFER { unsigned char* };
%include "var_arrays.i"


%javaconst(1);
%{
#include "codec_api.h"
#include "definitions.h"
%}

%include "codec_api.h"
%include "codec_app_def.h"
%include "codec_def.h"
%include "definitions.h"

%include "SSourcePicture.i"
%include "ISVCDecoder.i"
%include "ISVCEncoder.i"

