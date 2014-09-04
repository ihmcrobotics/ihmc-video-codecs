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

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.nio.ByteBuffer;

import us.ihmc.codecs.loader.NativeLibraryLoader;

import com.google.code.libyuv.FilterModeEnum;
import com.google.code.libyuv.libyuv;

public abstract class YUVPicture
{
   static
   {
      NativeLibraryLoader.loadLibYUV();
   }

   public enum YUVSubsamplingType
   {
      YUV444, YUV422, YUV420, UNSUPPORTED;

      /**
       * Detect the type of the YUV image
       * 
       * @param numberOfPlanes Number of planes (YUV = 3)
       * @param planeWidths Width of each plane
       * @param planeHeights Height of each plane
       * @return Sampling type of UNSUPPORTED if format is not known
       */
      public static YUVSubsamplingType getSubsamplingType(int numberOfPlanes, int[] planeWidths, int[] planeHeights)
      {
         if (numberOfPlanes != planeWidths.length || numberOfPlanes != planeHeights.length)
         {
            throw new RuntimeException("component lenghts do not match");
         }

         if (numberOfPlanes == 3)
         {
            if (planeWidths[0] == planeWidths[1] && planeWidths[0] == planeWidths[2] && planeHeights[0] == planeHeights[1]
                  && planeHeights[0] == planeHeights[2])
            {
               return YUV444;
            }
            else if (planeWidths[0] / 2 == planeWidths[1] && planeWidths[0] / 2 == planeWidths[2] && planeHeights[0] == planeHeights[1]
                  && planeHeights[0] == planeHeights[2])
            {
               return YUV422;
            }
            else if (planeWidths[0] / 2 == planeWidths[1] && planeWidths[0] / 2 == planeWidths[2] && planeHeights[0] / 2 == planeHeights[1]
                  && planeHeights[0] / 2 == planeHeights[2])
            {
               return YUV420;
            }

         }
         return UNSUPPORTED;
      }
   }

   /**
    * Convert BufferedImage to a ByteBuffer
    * @param in 
    * @return ByteBuffer with B, G, R channels
    */
   public static ByteBuffer getRGBBuffer(BufferedImage in)
   {
      switch (in.getType())
      {
      case BufferedImage.TYPE_3BYTE_BGR:
         WritableRaster raster = in.getRaster();
         byte[] imageBuffer = ((DataBufferByte) raster.getDataBuffer()).getData();

         int srcStride = in.getWidth() * 3;

         ByteBuffer bgr = ByteBuffer.allocateDirect(srcStride * in.getHeight());
         bgr.put(imageBuffer);

         return bgr;
      default:
         throw new RuntimeException("Can only handle 3 byte bgr images");
      }
   }

   /**
    * Convert BufferedImage to a ByteBuffer
    * 
    * @param in
    * @return ByteBuffer with B, G, R, A channels
    */
   public static ByteBuffer getARGBBuffer(BufferedImage in)
   {
      switch (in.getType())
      {
      case BufferedImage.TYPE_3BYTE_BGR:

         ByteBuffer bgr = getRGBBuffer(in);
         int srcStride = in.getWidth() * 3;
         int dstStride = in.getWidth() * 4;
         ByteBuffer bgra = ByteBuffer.allocateDirect(dstStride * in.getHeight());
         libyuv.RGB24ToARGB(bgr, srcStride, bgra, dstStride, in.getWidth(), in.getHeight());

         return bgra;
      default:
         throw new RuntimeException("Can only handle 3 byte bgr images");
      }
   }

   protected final int w;
   protected final int h;
   protected final int yStride;
   protected final int uStride;
   protected final int vStride;

   protected YUVPicture(int w, int h, int yStride, int uStride, int vStride)
   {
      this.w = w;
      this.h = h;
      this.yStride = yStride;
      this.uStride = uStride;
      this.vStride = vStride;
   }

   /**
    * @return Width of picture
    */
   public int getWidth()
   {
      return w;
   }

   /**
    * 
    * @return Height of picture
    */
   public int getHeight()
   {
      return h;
   }

   protected BufferedImage createBGRBufferedImageFromRGBA(ByteBuffer dstBuffer)
   {
      // The conversion in native code to BGR is much faster than doing byte-for-byte copies in Java
      int srcStride = w * 4;
      int rgbStride = w * 3;
      // Reusing the ARGB buffer looks to work, if problematic make a new buffer
//      ByteBuffer rgbBuffer = ByteBuffer.allocateDirect(rgbStride * h);
      libyuv.ARGBToRGB24(dstBuffer, srcStride, dstBuffer, rgbStride, w, h);
      return createBGRBufferedImageFromRGB24(dstBuffer);
      
//      BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
//      WritableRaster raster = img.getRaster();
//      DataBufferByte buffer = (DataBufferByte) raster.getDataBuffer();
//
//      for (int i = 0; dstBuffer.hasRemaining(); i += 3)
//      {
//         buffer.setElem(i, dstBuffer.get());
//         buffer.setElem(i + 1, dstBuffer.get());
//         buffer.setElem(i + 2, dstBuffer.get());
//         dstBuffer.get();
//      }
//      return img;

   }

   protected BufferedImage createBGRBufferedImageFromRGB24(ByteBuffer dstBuffer)
   {
      BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
      WritableRaster raster = img.getRaster();
      DataBufferByte buffer = (DataBufferByte) raster.getDataBuffer();
      dstBuffer.get(buffer.getData());

      
      return img;
   }

   /**
    * Resize this picture
    * 
    * @param newWidth Desired width, must be a multiple of two
    * @param newHeight Desired height, must be a multiple of two
    * @param filterMode Fitler method to use
    * @return New YUVPicture with the desired dimensions
    */
   public abstract YUVPicture scale(int newWidth, int newHeight, FilterModeEnum filterMode);

   /**
    * Convert this picture to a BufferedImage
    * @return BufferedImage in TYPE_3_BYTE_BGR
    */
   public abstract BufferedImage getImage();

   /**
    * 
    * @return Buffer with Y plane data of size getYStride() * getHeight()
    */
   public abstract ByteBuffer getY();

   /**
    * 
    * @return Buffer with U plane data of size getUStride() * getHeight()
    */
   public abstract ByteBuffer getU();

   /**
    * 
    * @return Buffer with V plane data of size getVStride() * getHeight()
    */
   public abstract ByteBuffer getV();

   /**
    * 
    * @return Y Stride of the the internal buffer
    */
   public int getYStride()
   {
      return yStride;
   }

   /**
    * 
    * @return U Stride of the the internal buffer
    */
   public int getUStride()
   {
      return uStride;
   }

   /**
    * 
    * @return V Stride of the the internal buffer
    */
   public int getVStride()
   {
      return vStride;
   }

   /**
    * 
    * @return YUV420 version of this picture. Could be the same picture.
    */
   public abstract YUV420Picture toYUV420();

}
