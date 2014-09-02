package us.ihmc.codecs.h264;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.openh264.CM_RETURN;
import org.openh264.ENCODER_OPTION;
import org.openh264.EVideoFormatType;
import org.openh264.ISVCEncoder;
import org.openh264.OpenH264;
import org.openh264.RC_MODES;
import org.openh264.SEncParamExt;
import org.openh264.SFrameBSInfo;
import org.openh264.SLayerBSInfo;
import org.openh264.SSourcePicture;

import us.ihmc.codecs.YUVPicture;

/**
 * Created by jesper on 8/19/14.
 */
public class OpenH264Encoder implements H264Encoder
{
   static
   {
      System.loadLibrary("openh264bridge");
   }

   private final ISVCEncoder isvcEncoder;
   private SEncParamExt paramExt;

   public OpenH264Encoder() throws IOException
   {
      isvcEncoder = OpenH264.WelsCreateSVCEncoder();
      if (isvcEncoder == null)
      {
         throw new IOException("Cannot create wels encoder");
      }
   }

   /**
    * Create a new SEncParamExt to initialize the encoder. Use this to set advanced settings of the encoder
    * 
    * @param width Width of frames to encode
    * @param height Height of frames to encode
    * @param targetBitRate Target bitrate of the resulting stream
    * @param rateControlMode Desired rate control mode
    * 
    * @return new SEncParamExt object. 
    */
   public SEncParamExt createParamExt(int width, int height, int targetBitRate, RC_MODES rateControlMode)
   {
      if ((width >> 1) << 1 != width || (height >> 1) << 1 != height)
      {
         throw new RuntimeException("Resolution not divisible by 2");
      }

      SEncParamExt paramExt = new SEncParamExt();
      isvcEncoder.GetDefaultParams(paramExt);

      setDimensionParams(paramExt, width, height);
      setBitRateParams(paramExt, targetBitRate);
      paramExt.setIRCMode(rateControlMode);

      return paramExt;
   }

   /**
    * Simple initialization of encoder
    * 
    * @param width Desired frame width
    * @param height Desired frame height
    * @param targetBitRate target bit rate for stream
    * @param rateControlMode rate control mode
    */
   public void initialize(int width, int height, int targetBitRate, RC_MODES rateControlMode)
   {
      SEncParamExt paramExt = createParamExt(width, height, targetBitRate, rateControlMode);
      initialize(paramExt);
   }

   /**
    * Initialize based on current SEncParamExt. 
    * @param paramExt Structure of type SEncParamExt. Create with OpenH264Encoder.createParamExt()
    * 
    * @see createParamExt()
    */
   public void initialize(SEncParamExt paramExt)
   {
      this.paramExt = paramExt;
      CM_RETURN res = CM_RETURN.swigToEnum(isvcEncoder.InitializeExt(paramExt));
      if(res != CM_RETURN.cmResultSuccess)
      {
         throw new RuntimeException("Cannot intialize encoder: " + res);
      }
      
   }

   private void setDimensionParams(SEncParamExt paramExt, int width, int height)
   {
      paramExt.setIPicWidth(width);
      paramExt.setIPicHeight(height);
      paramExt.getSpatialLayer(0).setIVideoHeight(height);
      paramExt.getSpatialLayer(0).setIVideoWidth(width);
   }

   private void setBitRateParams(SEncParamExt paramExt, int bitrate)
   {
      paramExt.getSpatialLayer(0).setISpatialBitrate(bitrate);
      paramExt.setITargetBitrate(bitrate);
   }

   private void checkInitalized()
   {
      if (paramExt == null)
      {
         throw new RuntimeException("Initialize the encoder before use.");
      }
   }

   private void setOptionParamExt()
   {
      CM_RETURN ret = CM_RETURN.swigToEnum(isvcEncoder.SetOption(ENCODER_OPTION.ENCODER_OPTION_SVC_ENCODE_PARAM_EXT, paramExt));
      if(ret != CM_RETURN.cmResultSuccess)
      {
         throw new RuntimeException("Cannot set option: " + ret);
      }
      
   }
   
   @Override
   public synchronized void setTargetBitRate(int bitrate)
   {
      checkInitalized();
      setBitRateParams(paramExt, bitrate); // Set bitrate params, or resolution change overwrites bitrate settings
      setOptionParamExt();
      
//      SBitrateInfo sBitrateInfo = new SBitrateInfo();
//      sBitrateInfo.setIBitrate(bitrate);
//      sBitrateInfo.setILayer(LAYER_NUM.SPATIAL_LAYER_ALL);
//      isvcEncoder.SetOption(ENCODER_OPTION.ENCODER_OPTION_BITRATE, sBitrateInfo);
   }

   @Override
   public synchronized void setFrameRate(float fps)
   {
      checkInitalized();
      paramExt.getSpatialLayer(0).setFFrameRate(fps);
      setOptionParamExt();
   }
   
   @Override
   public synchronized void setIDRPeriod(int period)
   {
      checkInitalized();
      paramExt.setUiIntraPeriod(period);
      setOptionParamExt();
   }

   @Override
   public synchronized void setResolution(int width, int height)
   {
      checkInitalized();
      if ((width >> 1) << 1 != width || (height >> 1) << 1 != height)
      {
         throw new RuntimeException("Resolution not divisible by 2");
      }

      setDimensionParams(paramExt, width, height);
      setOptionParamExt();
   }

   @Override
   public synchronized void encodeFrame(YUVPicture frame, NALProcessor nalProcessor) throws IOException
   {
      checkInitalized();
      if (frame.getWidth() != paramExt.getIPicWidth() || frame.getHeight() != paramExt.getIPicHeight())
      {
         throw new RuntimeException("Resolution of frame does not correspond to encoder resolution");
      }

      SSourcePicture picture = new SSourcePicture();

      picture.setIPicWidth(frame.getWidth());
      picture.setIPicHeight(frame.getHeight());
      picture.setIColorFormat(EVideoFormatType.videoFormatI420.swigValue());

      int[] stride = { frame.getYStride(), frame.getUStride(), frame.getVStride(), 0 };
      picture.setIStride(stride);
      picture.setPData(0, frame.getY());
      picture.setPData(1, frame.getU());
      picture.setPData(2, frame.getV());

      SFrameBSInfo info = new SFrameBSInfo();
      int e = isvcEncoder.EncodeFrame(picture, info);

      if (e != 0)
      {
         throw new IOException("Cannot encode frame: " + e);
      }

      switch (info.getEFrameType())
      {
      case videoFrameTypeInvalid:
         throw new IOException("Encoder not ready or parameters are invalidate");
      case videoFrameTypeSkip:
         break;
      case videoFrameTypeI:
      case videoFrameTypeIDR:
      case videoFrameTypeP:
      case videoFrameTypeIPMixed:
         
         for (int i = 0; i < info.getILayerNum(); i++)
         {
            SLayerBSInfo sLayerInfo = info.getSLayerInfo(i);
            for (int n = 0; n < sLayerInfo.getINalCount(); n++)
            {
               ByteBuffer nalBuffer = ByteBuffer.allocateDirect(sLayerInfo.getNalLengthInByte(n));
               sLayerInfo.getNal(n, nalBuffer);
               NALType type = NALType.fromBitStream(nalBuffer);
               nalProcessor.processNal(type, nalBuffer);
            }
         }
      }

      picture.delete();
      info.delete();
   }

   /** 
    * Reclaim native memory.
    */
   public void delete()
   {
      isvcEncoder.Uninitialize();
      OpenH264.WelsDestroySVCEncoder(isvcEncoder);

      if (paramExt != null)
      {
         paramExt.delete();
      }
   }

   @Override
   public void finalize()
   {
      delete();
   }
}
