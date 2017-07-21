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

/**
* @brief Enumerate the type of profile id
*/
typedef enum {
  PRO_UNKNOWN   = 0,
  PRO_BASELINE  = 66,
  PRO_MAIN      = 77,
  PRO_EXTENDED  = 88,
  PRO_HIGH      = 100,
  PRO_HIGH10    = 110,
  PRO_HIGH422   = 122,
  PRO_HIGH444   = 144,
  PRO_CAVLC444  = 244,

  PRO_SCALABLE_BASELINE = 83,
  PRO_SCALABLE_HIGH     = 86
} EProfileIdc;

/**
* @brief Enumerate the type of level id
*/
typedef enum {
  LEVEL_UNKNOWN = 0,
  LEVEL_1_0 = 10,
  LEVEL_1_B = 9,
  LEVEL_1_1 = 11,
  LEVEL_1_2 = 12,
  LEVEL_1_3 = 13,
  LEVEL_2_0 = 20,
  LEVEL_2_1 = 21,
  LEVEL_2_2 = 22,
  LEVEL_3_0 = 30,
  LEVEL_3_1 = 31,
  LEVEL_3_2 = 32,
  LEVEL_4_0 = 40,
  LEVEL_4_1 = 41,
  LEVEL_4_2 = 42,
  LEVEL_5_0 = 50,
  LEVEL_5_1 = 51,
  LEVEL_5_2 = 52
} ELevelIdc;


%include "OpenH264DecoderImpl.h"
%include "OpenH264EncoderImpl.h"
