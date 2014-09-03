package us.ihmc.codecs.yuv;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.nio.ByteBuffer;

import com.google.code.libyuv.FilterModeEnum;
import com.google.code.libyuv.libyuv;

public abstract class YUVPicture
{
   static
   {
      System.loadLibrary("libyuv");
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

   public YUVPicture(int w, int h, int yStride, int uStride, int vStride)
   {
      this.w = w;
      this.h = h;
      this.yStride = yStride;
      this.uStride = uStride;
      this.vStride = vStride;
   }

   public int getWidth()
   {
      return w;
   }

   public int getHeight()
   {
      return h;
   }

   protected BufferedImage createBGRBufferedImageFromRGBA(ByteBuffer dstBuffer)
   {
      BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
      WritableRaster raster = img.getRaster();
      DataBufferByte buffer = (DataBufferByte) raster.getDataBuffer();

      for (int i = 0; dstBuffer.hasRemaining(); i += 3)
      {
         buffer.setElem(i, dstBuffer.get());
         buffer.setElem(i + 1, dstBuffer.get());
         buffer.setElem(i + 2, dstBuffer.get());
         dstBuffer.get();
      }
      return img;

   }

   protected BufferedImage createBGRBufferedImageFromRGB24(ByteBuffer dstBuffer)
   {
      BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
      WritableRaster raster = img.getRaster();
      DataBufferByte buffer = (DataBufferByte) raster.getDataBuffer();

      for (int i = 0; dstBuffer.hasRemaining(); i++)
      {
         buffer.setElem(i, dstBuffer.get());
      }
      return img;
   }

   public abstract YUVPicture scale(int newWidth, int newHeight, FilterModeEnum filterMode);

   public abstract BufferedImage getImage();

   public abstract ByteBuffer getY();

   public abstract ByteBuffer getU();

   public abstract ByteBuffer getV();

   public int getYStride()
   {
      return yStride;
   }

   public int getUStride()
   {
      return uStride;
   }

   public int getVStride()
   {
      return vStride;
   }

}
