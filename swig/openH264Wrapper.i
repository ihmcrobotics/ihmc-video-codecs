%include "enums.swg"
%include "arrays_java.i"
%include "typemaps.i"
%include "various.i"
%apply unsigned char *NIOBUFFER { uint8* };


%javaconst(1);
%{
	#include "OpenH264DecoderImpl.h"
	#include "OpenH264EncoderImpl.h"
%}

%newobject  OpenH264DecoderImpl::decodeFrame;



// Copied form codec_app_def.h
/**
* @brief Enumulate the complexity mode
*/
typedef enum {
  LOW_COMPLEXITY,             ///< the lowest compleixty,the fastest speed,
  MEDIUM_COMPLEXITY,          ///< medium complexity, medium speed,medium quality
  HIGH_COMPLEXITY             ///< high complexity, lowest speed, high quality
} ECOMPLEXITY_MODE;

/**
* @brief Encoder usage type
*/
typedef enum {
  CAMERA_VIDEO_REAL_TIME,      ///< camera video signal
  SCREEN_CONTENT_REAL_TIME     ///< screen content signal
} EUsageType;

/**
* @brief Enumerate the type of rate control mode
*/
typedef enum {
  RC_QUALITY_MODE = 0,     ///< quality mode
  RC_BITRATE_MODE = 1,     ///< bitrate mode
  RC_BUFFERBASED_MODE = 2, ///< no bitrate control,only using buffer status,adjust the video quality
  RC_OFF_MODE = -1         ///< rate control off mode
} RC_MODES;


%include "OpenH264DecoderImpl.h"
%include "OpenH264EncoderImpl.h"
