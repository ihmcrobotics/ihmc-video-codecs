%module ihmcVideoCodecs
%include "enums.swg"
%include "arrays_java.i"
%include "typemaps.i"
%include "various.i"
%apply unsigned char *NIOBUFFER { uint8* };

%include yuvWrapper.i
%include openH264Wrapper.i
