package us.ihmc.codecs.yuv;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

import us.ihmc.codecs.util.MemoryManagement;

import com.google.code.libyuv.FilterModeEnum;
import com.google.code.libyuv.libyuv;

/**
 * Implementation of a YUV 4:4:4 format
 * @author Jesper Smith
 *
 */
public class YUV444Picture extends YUVPicture
{
   private ByteBuffer Y, U, V;

   public YUV444Picture(ByteBuffer Y, ByteBuffer U, ByteBuffer V, int yStride, int uStride, int vStride, int w, int h)
   {
      super(w, h, yStride, uStride, vStride);
      this.Y = Y;
      this.U = U;
      this.V = V;
   }

   /**
    * Create YUV 4:4:4 picture from a BufferedImage
    * @param orig BufferedImage of TYPE_3_BYTE_BGR
    */
   public YUV444Picture(BufferedImage orig)
   {
      super(orig.getWidth(), orig.getHeight(), orig.getWidth(), orig.getWidth(), orig.getWidth());

      Y = ByteBuffer.allocateDirect(yStride * h);
      U = ByteBuffer.allocateDirect(uStride * h);
      V = ByteBuffer.allocateDirect(vStride * h);

      ByteBuffer src = getARGBBuffer(orig);

      int srcStride = w * 3;
      libyuv.ARGBToI444(src, srcStride, Y, yStride, U, uStride, V, vStride, w, h);

   }

   @Override
   public YUV444Picture scale(int newWidth, int newHeight, FilterModeEnum filterMode)
   {
      if ((newWidth >> 1) << 1 != newWidth || (newHeight >> 1) << 1 != newHeight)
      {
         throw new RuntimeException("Resolution not divisible by 2");
      }

      int yStrideDest = newWidth;
      int uStrideDest = yStrideDest;
      int vStrideDest = yStrideDest;

      ByteBuffer Ydest = ByteBuffer.allocateDirect(yStrideDest * newHeight);
      ByteBuffer Udest = ByteBuffer.allocateDirect(uStrideDest * newHeight);
      ByteBuffer Vdest = ByteBuffer.allocateDirect(vStrideDest * newHeight);

      libyuv.ScalePlane(Y, yStride, w, h, Ydest, yStrideDest, newWidth, newHeight, filterMode);
      libyuv.ScalePlane(U, uStride, w, h, Udest, uStrideDest, newWidth, newHeight, filterMode);
      libyuv.ScalePlane(V, vStride, w, h, Vdest, vStrideDest, newWidth, newHeight, filterMode);

      return new YUV444Picture(Ydest, Udest, Vdest, yStrideDest, uStrideDest, vStrideDest, newWidth, newHeight);

   }

   @Override
   public BufferedImage getImage()
   {
      int dstStride = w * 4;
      ByteBuffer dstBuffer = ByteBuffer.allocateDirect(dstStride * h);

      libyuv.I444ToARGB(Y, yStride, U, uStride, V, vStride, dstBuffer, dstStride, w, h);

      BufferedImage img = createBGRBufferedImageFromRGBA(dstBuffer);
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
      
      int yStrideDest = w;
      int uStrideDest = yStrideDest >> 1;
      int vStrideDest = yStrideDest >> 1;

      ByteBuffer Ydest = ByteBuffer.allocateDirect(yStrideDest * h);
      ByteBuffer Udest = ByteBuffer.allocateDirect(uStrideDest * (h >> 1));
      ByteBuffer Vdest = ByteBuffer.allocateDirect(vStrideDest * (h >> 1));
      
      libyuv.I444ToI420(Y, yStride, U, uStride, V, vStride, Ydest, yStrideDest, Udest, uStrideDest, Vdest, vStrideDest, w, h);
      
      return new YUV420Picture(Ydest, Udest, Vdest, yStrideDest, uStrideDest, vStrideDest, w, h);
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
