/*
 *   Copyright 2014 Florida Institute for Human and Machine Cognition (IHMC)
 *    
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *    
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *    
 *    Written by Jesper Smith with assistance from IHMC team members
 */

package us.ihmc.codecs.h264;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.openh264.DECODING_STATE;
import org.openh264.ERROR_CON_IDC;
import org.openh264.EVideoFormatType;
import org.openh264.ISVCDecoder;
import org.openh264.OpenH264;
import org.openh264.SDecodingParam;
import org.openh264.STargetPicture;
import org.openh264.VIDEO_BITSTREAM_TYPE;

import us.ihmc.codecs.loader.NativeLibraryLoader;
import us.ihmc.codecs.yuv.YUV420Picture;
import us.ihmc.codecs.yuv.YUVPicture;

/**
 * Wrapper class for the OpenH264 Decoder. Easy to use functions for decoding H264 streams. 
 * 
 * Make sure to call delete() after use.
 * 
 * 
 * @author Jesper Smith
 *
 */
public class OpenH264Decoder
{
   static
   {
      NativeLibraryLoader.loadOpenH264Bridge();
   }

   private final ISVCDecoder isvcDecoder;
   private final STargetPicture pic = new STargetPicture();

   /**
    * Create a new decoder
    * 
    * @throws IOException
    */
   public OpenH264Decoder() throws IOException
   {
      isvcDecoder = OpenH264.WelsCreateDecoder();
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

   /**
    * Decode a frame.
    * 
    * @param frame Single NAL packet including header ( 0x00 0x00 0x00 0x01 )
    * @return YUVPicture if a frame was successfully decoded, null if no image was generated (does not mean an error)
    * @throws IOException An error occurred decoding the NAL
    */
   public YUVPicture decodeFrame(ByteBuffer frame) throws IOException
   {
      DECODING_STATE state = isvcDecoder.DecodeFrame2(frame, frame.limit(), pic);
      
      if (state != DECODING_STATE.dsErrorFree)
      {
         throw new IOException("Cannot decode frame: " + state);
      }
      
      
      // Sometimes the encoder is not ready. Send an empty decode command will make it work.
      if(pic.getInfo().getIBufferStatus() == 0)
      {
         NALType type = NALType.fromBitStream(frame);
         if( 
             type == NALType.CODED_SLICE_NON_IDR_PICTURE   ||
             type == NALType.CODED_SLICE_DATA_PARTITION_A  ||
             type == NALType.CODED_SLICE_DATA_PARTITION_B  ||
             type == NALType.CODED_SLICE_DATA_PARTITION_C  ||
             type == NALType.CODED_SLICE_IDR_PICTURE)
         {
            isvcDecoder.DecodeFrame2(pic);            
         }

      }
      
      if (pic.getInfo().getIBufferStatus() == 1)
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

         
         YUVPicture yuvPicture = new YUV420Picture(Y, U, V, stride[0], stride[1], stride[1], width, height);
         
         return yuvPicture;
      }
      else
      {
         return null;
      }

   }
   
   /**
    * Feeds frame to the decoder but ignores the output
    * 
    * @param frame to skip
    * @throws IOException
    */
   public void skipFrame(ByteBuffer frame) throws IOException
   {
      DECODING_STATE state = isvcDecoder.DecodeFrame2(frame, frame.limit(), pic);
      
      if (state != DECODING_STATE.dsErrorFree)
      {
         throw new IOException("Cannot decode frame: " + state);
      }
      
   }
   
   
   /**
    * Free native memory
    */
   public void delete()
   {
      isvcDecoder.Uninitialize();
      OpenH264.WelsDestroyDecoder(isvcDecoder);
      pic.delete();
   }
   
   @Override
   public void finalize()
   {
      delete();
   }
}
