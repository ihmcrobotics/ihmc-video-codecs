package us.ihmc.codecs.h264;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.openh264.DECODING_STATE;
import org.openh264.ERROR_CON_IDC;
import org.openh264.EVideoFormatType;
import org.openh264.ISVCDecoder;
import org.openh264.SBufferInfoExt;
import org.openh264.SDecodingParam;
import org.openh264.VIDEO_BITSTREAM_TYPE;
import org.openh264.codec_api;

import us.ihmc.codecs.colorSpace.YCbCr420;

public class OpenH264Decoder
{
   
   private final ISVCDecoder isvcDecoder;
   
   public OpenH264Decoder() throws IOException
   {
      System.loadLibrary("codec_api");
      isvcDecoder = codec_api.WelsCreateDecoder();
      SDecodingParam pParam = new SDecodingParam();
      pParam.setEOutputColorFormat(EVideoFormatType.videoFormatI420);
      pParam.setUiTargetDqLayer((short) 255);
      pParam.setEEcActiveIdc(ERROR_CON_IDC.ERROR_CON_SLICE_COPY);
      pParam.getSVideoProperty().setEVideoBsType(VIDEO_BITSTREAM_TYPE.VIDEO_BITSTREAM_DEFAULT);
      isvcDecoder.Initialize(pParam);
   }
   
   public BufferedImage decodeFrame(ByteBuffer frame) throws IOException
   {
      SBufferInfoExt pic = isvcDecoder.DecodeFrame2(frame, frame.limit());
//      SBufferInfoExt info = isvcDecoder.DecodeFrame2(frame, frame.limit());
      if(pic.getState() != DECODING_STATE.dsErrorFree)
      {
         throw new IOException("Cannot decode frame: " + pic.getState());
      }
      else if(pic.getInfo().getIBufferStatus() == 1)
      {
         System.out.println("DECODED");
         int width = pic.getInfo().getUsrData().getIWidth();
         int height = pic.getInfo().getUsrData().getIHeight();
         int[] stride = pic.getInfo().getUsrData().getIStride();
         
         ByteBuffer data = ByteBuffer.allocateDirect(stride[0] * height + 2 * stride[1] * height);
         pic.getPpDst(data);
         ByteBuffer Yb = data.slice();
         data.position(stride[0] * height);
         ByteBuffer CBb = data.slice();
         data.position(stride[0] * height + stride[1] * height);
         ByteBuffer CRb = data.slice();
         
         return YCbCr420.convertYCbCr420ToRGB888(Yb, CBb, CRb, width, height, stride[0], stride[1]);
         
      }
      else
      {
         return null;
      }
      
   }
}
