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
import java.nio.ByteBuffer;

import us.ihmc.codecs.util.MemoryManagement;

import com.google.code.libyuv.FilterModeEnum;
import com.google.code.libyuv.libyuv;

/**
 * Implementation of a YUV 4:2:0 format
 * @author Jesper Smith
 *
 */
public class YUV420Picture extends YUVPicture
{

   private ByteBuffer Y, U, V;

   /**
    * Create YUV 4:2:0 picture from a BufferedImage
    * @param orig BufferedImage of TYPE_3_BYTE_BGR
    */
   public YUV420Picture(BufferedImage orig)
   {
      super(orig.getWidth(), orig.getHeight(), orig.getWidth(), orig.getWidth() >> 1, orig.getWidth() >> 1);

      Y = ByteBuffer.allocateDirect(yStride * h);
      U = ByteBuffer.allocateDirect(uStride * (h >> 1));
      V = ByteBuffer.allocateDirect(vStride * (h >> 1));

      ByteBuffer src = getRGBBuffer(orig);

      int srcStride = w * 3;
      libyuv.RGB24ToI420(src, srcStride, Y, yStride, U, uStride, V, vStride, w, h);

   }

   public YUV420Picture(ByteBuffer Y, ByteBuffer U, ByteBuffer V, int yStride, int uStride, int vStride, int w, int h)
   {
      super(w, h, yStride, uStride, vStride);
      this.Y = Y;
      this.U = U;
      this.V = V;
   }

   @Override
   public YUV420Picture scale(int newWidth, int newHeight, FilterModeEnum filterMode)
   {
      if ((newWidth >> 1) << 1 != newWidth || (newHeight >> 1) << 1 != newHeight)
      {
         throw new RuntimeException("Resolution not divisible by 2");
      }

      int yStrideDest = newWidth;
      int uStrideDest = yStrideDest >> 1;
      int vStrideDest = yStrideDest >> 1;

      ByteBuffer Ydest = ByteBuffer.allocateDirect(yStrideDest * newHeight);
      ByteBuffer Udest = ByteBuffer.allocateDirect(uStrideDest * (newHeight >> 1));
      ByteBuffer Vdest = ByteBuffer.allocateDirect(vStrideDest * (newHeight >> 1));

      libyuv.I420Scale(Y, yStride, U, uStride, V, vStride, w, h, Ydest, yStrideDest, Udest, uStrideDest, Vdest, vStrideDest, newWidth, newHeight, filterMode);

      return new YUV420Picture(Ydest, Udest, Vdest, yStrideDest, uStrideDest, vStrideDest, newWidth, newHeight);
   }

   @Override
   public BufferedImage getImage()
   {
      ByteBuffer dstBuffer = ByteBuffer.allocateDirect(w * h * 3);
      int dstStride = w * 3;
      libyuv.I420ToRGB24(Y, yStride, U, uStride, V, vStride, dstBuffer, dstStride, w, h);

      BufferedImage img = createBGRBufferedImageFromRGB24(dstBuffer);
      MemoryManagement.deallocateNativeByteBuffer(dstBuffer);
      return img;
   }

   @Override
   public ByteBuffer getY()
   {
      return Y;
   }

   @Override
   public ByteBuffer getU()
   {
      return U;
   }

   @Override
   public ByteBuffer getV()
   {
      return V;
   }

   @Override
   public YUV420Picture toYUV420()
   {
      return this;
   }

   @Override
   public void delete()
   {
      MemoryManagement.deallocateNativeByteBuffer(Y);
      MemoryManagement.deallocateNativeByteBuffer(U);
      MemoryManagement.deallocateNativeByteBuffer(V);
      Y = null;
      U = null;
      V = null;
   }
}
