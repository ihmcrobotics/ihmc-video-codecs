package us.ihmc.codecs.h264;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.openh264.ENCODER_OPTION;
import org.openh264.EVideoFormatType;
import org.openh264.ISVCEncoder;
import org.openh264.RC_MODES;
import org.openh264.SEncParamBase;
import org.openh264.SFrameBSInfo;
import org.openh264.SLayerBSInfo;
import org.openh264.SSourcePicture;
import org.openh264.codec_api;

import us.ihmc.codecs.YUVPicture;

/**
 * Created by jesper on 8/19/14.
 */
public class OpenH264Encoder implements H264Encoder
{

   private final ISVCEncoder isvcEncoder;
   private int width, height;

   public OpenH264Encoder(int width, int height) throws IOException
   {
      System.loadLibrary("codec_api");
      isvcEncoder = codec_api.WelsCreateSVCEncoder();
      SEncParamBase paramBase = new SEncParamBase();
      initializeDefaultParams(paramBase);
      paramBase.setIPicWidth(width);
      paramBase.setIPicHeight(height);
      isvcEncoder.Initialize(paramBase);
      paramBase.delete();

      this.width = width;
      this.height = height;

   }
   
   private void initializeDefaultParams(SEncParamBase paramBase)
   {
      paramBase.setFMaxFrameRate(30);
      paramBase.setIRCMode(RC_MODES.RC_QUALITY_MODE);
      paramBase.setITargetBitrate(500000);
      
   }

   public void setResolution(int width, int height)
   {
      SEncParamBase paramBase = new SEncParamBase();
      initializeDefaultParams(paramBase);
      paramBase.setIPicWidth(width);
      paramBase.setIPicHeight(height);
      isvcEncoder.SetOption(ENCODER_OPTION.ENCODER_OPTION_SVC_ENCODE_PARAM_BASE, paramBase);
      this.width = width;
      this.height = height;
      
      paramBase.delete();
   }

   @Override
   public void encodeFrame(YUVPicture frame, NALProcessor nalProcessor) throws IOException
   {
      if (frame.getWidth() != this.width || frame.getHeight() != this.height)
      {
         throw new RuntimeException("Incompatible resolution");
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

      for (int i = 0; i < info.getILayerNum(); i++)
      {
         SLayerBSInfo sLayerInfo = info.getSLayerInfo(i);
         int[] nalLengthInByte = sLayerInfo.getPNalLengthInByte();

         for (int n = 0; n < sLayerInfo.getINalCount(); n++)
         {
            ByteBuffer nalBuffer = ByteBuffer.allocateDirect(nalLengthInByte[n]);
            sLayerInfo.getNAL(n, nalBuffer);
            nalProcessor.processNAL(nalBuffer);
         }
      }

      picture.delete();
      info.delete();
   }

   public void delete()
   {
      isvcEncoder.delete();
   }

   @Override
   public void finalize()
   {
      delete();
   }
}
