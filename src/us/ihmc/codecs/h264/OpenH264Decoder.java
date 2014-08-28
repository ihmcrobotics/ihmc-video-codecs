package us.ihmc.codecs.h264;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.openh264.DECODING_STATE;
import org.openh264.ERROR_CON_IDC;
import org.openh264.EVideoFormatType;
import org.openh264.ISVCDecoder;
import org.openh264.SDecodingParam;
import org.openh264.STargetPicture;
import org.openh264.VIDEO_BITSTREAM_TYPE;
import org.openh264.codec_api;

import us.ihmc.codecs.YUVPicture;

public class OpenH264Decoder
{

   private final ISVCDecoder isvcDecoder;
   private final STargetPicture pic = new STargetPicture();

   public OpenH264Decoder() throws IOException
   {
      System.loadLibrary("codec_api");
      isvcDecoder = codec_api.WelsCreateDecoder();
      if(isvcDecoder == null)
      {
         throw new IOException("Cannot create Wels decoder");
      }
      SDecodingParam pParam = new SDecodingParam();
      pParam.setEOutputColorFormat(EVideoFormatType.videoFormatI420);
      pParam.setUiTargetDqLayer((short) 255);
      pParam.setEEcActiveIdc(ERROR_CON_IDC.ERROR_CON_SLICE_COPY);
      pParam.getSVideoProperty().setEVideoBsType(VIDEO_BITSTREAM_TYPE.VIDEO_BITSTREAM_DEFAULT);
      isvcDecoder.Initialize(pParam);
      pParam.delete();
   }

   public YUVPicture decodeFrame(ByteBuffer frame) throws IOException
   {
      DECODING_STATE state = isvcDecoder.DecodeFrame2(frame, frame.limit(), pic);
      
      if (state != DECODING_STATE.dsErrorFree)
      {
         throw new IOException("Cannot decode frame: " + state);
      }
      else if (pic.getInfo().getIBufferStatus() == 1)
      {
         int width = pic.getInfo().getUsrData().getIWidth();
         int height = pic.getInfo().getUsrData().getIHeight();
         int[] stride = pic.getInfo().getUsrData().getIStride();

         ByteBuffer Y = ByteBuffer.allocateDirect(stride[0] * height);
         ByteBuffer U = ByteBuffer.allocateDirect(stride[1] * (height >> 1));
         ByteBuffer V = ByteBuffer.allocateDirect(stride[1] * (height >> 1));

         pic.getY(Y);
         pic.getU(U);
         pic.getV(V);

         YUVPicture yuvPicture = new YUVPicture(Y, U, V, stride[0], stride[1], stride[1], width, height);
         
         return yuvPicture;
      }
      else
      {
         return null;
      }

   }
   
   public void delete()
   {
      isvcDecoder.Uninitialize();
      codec_api.WelsDestroyDecoder(isvcDecoder);
      pic.delete();
   }
   
   @Override
   public void finalize()
   {
      delete();
   }
}
