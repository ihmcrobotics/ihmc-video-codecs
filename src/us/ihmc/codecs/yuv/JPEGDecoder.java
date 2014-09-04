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
package us.ihmc.codecs.yuv;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import us.ihmc.codecs.yuv.YUVPicture.YUVSubsamplingType;

import com.google.code.libyuv.MJpegDecoder;

/** 
 * JPEG decoder using libyuv
 * 
 * @author Jesper Smith
 *
 */
public class JPEGDecoder
{
   static
   {
      System.loadLibrary("libyuv");
   }

   private final MJpegDecoder decoder = new MJpegDecoder();

   public static final int kColorSpaceUnknown = MJpegDecoder.getKColorSpaceUnknown();
   public static final int kColorSpaceGrayscale = MJpegDecoder.getKColorSpaceGrayscale();
   public static final int kColorSpaceRgb = MJpegDecoder.getKColorSpaceRgb();
   public static final int kColorSpaceYCbCr = MJpegDecoder.getKColorSpaceYCbCr();
   public static final int kColorSpaceCMYK = MJpegDecoder.getKColorSpaceCMYK();
   public static final int kColorSpaceYCCK = MJpegDecoder.getKColorSpaceYCCK();

   
   public YUVPicture decode(ByteBuffer buf) throws IOException
   {
      ByteBuffer direct;
      if(!buf.isDirect())
      {
         ByteBuffer dup = buf.duplicate();
         direct = ByteBuffer.allocateDirect(dup.remaining());
         for (int i = 0; i < direct.capacity(); i++)
         {
            direct.put(dup.get());
         }
         direct.clear();
      }
      else
      {
         direct = buf.slice();
      }
      
      decoder.LoadFrame(direct, direct.remaining());
      int width = decoder.GetWidth();
      int height = decoder.GetHeight();

      int colorSpace = decoder.GetColorSpace();

      if (colorSpace == kColorSpaceUnknown)
      {
         throw new IOException("Unkown colorspace in MJPEG track");
      }
      else if (colorSpace == kColorSpaceRgb)
      {
         throw new IOException("Cannot handle RGB color space MJPEG track");
      }
      else if (colorSpace == kColorSpaceCMYK)
      {
         throw new IOException("Cannot handle CMYK color space MJPEG track");
      }
      else if (colorSpace == kColorSpaceYCCK)
      {
         throw new IOException("Cannot handle YCCK color space MJPEG track");
      }

      int numberOfPlanes = decoder.GetNumComponents();
      YUVSubsamplingType samplingType;

      if (numberOfPlanes != 3)
      {
         throw new IOException("Can only handle YUV colour MJPEG tracks with 3 components");
      }
      int planeWidths[] = { decoder.GetComponentWidth(0), decoder.GetComponentWidth(1), decoder.GetComponentWidth(2) };
      int planeHeights[] = { decoder.GetComponentHeight(0), decoder.GetComponentHeight(1), decoder.GetComponentHeight(2) };
      samplingType = YUVSubsamplingType.getSubsamplingType(numberOfPlanes, planeWidths, planeHeights);

      ByteBuffer Y = ByteBuffer.allocateDirect(planeWidths[0] * planeHeights[0]);
      ByteBuffer U = ByteBuffer.allocateDirect(planeWidths[1] * planeHeights[1]);
      ByteBuffer V = ByteBuffer.allocateDirect(planeWidths[2] * planeHeights[2]);

      decoder.Decode(Y, U, V);
      decoder.UnloadFrame();
      switch (samplingType)
      {
      case YUV420:
         return new YUV420Picture(Y, U, V, planeWidths[0], planeWidths[1], planeWidths[2], width, height);
      case YUV422:
         return new YUV422Picture(Y, U, V, planeWidths[0], planeWidths[1], planeWidths[2], width, height);
      case YUV444:
         return new YUV444Picture(Y, U, V, planeWidths[0], planeWidths[1], planeWidths[2], width, height);
      case UNSUPPORTED:
      default:
         throw new IOException("Unsupported sampling type");
      }
   }
   
   public YUVPicture readJPEG(File file) throws IOException
   {
      FileInputStream stream = new FileInputStream(file);
      FileChannel channel = stream.getChannel();
      ByteBuffer buffer = ByteBuffer.allocateDirect((int) channel.size());
      channel.read(buffer);
      buffer.flip();
      stream.close();
      return decode(buffer);      
   }


   public void delete()
   {
      decoder.delete();
   }
   
   @Override
   public void finalize()
   {
      delete();
   }

}
