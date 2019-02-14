package us.ihmc.codecs.yuv;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.WritableRaster;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import us.ihmc.codecs.generated.RGBPicture;
import us.ihmc.codecs.loader.NativeLibraryLoader;
import us.ihmc.codecs.util.ByteBufferProvider;

public class RGBPictureConverter
{
   static
   {
      NativeLibraryLoader.loadIHMCVideoCodecsLibrary();
   }

   private ByteBufferProvider byteBufferProvider = new ByteBufferProvider();

   /**
    * Convert RGBPicture to BufferedImage, minimizing object allocation
    * 
    * @param picture RGBPicture to convert
    * @return new BufferedImage.
    */
   public BufferedImage toBufferedImage(RGBPicture picture)
   {
      return toBufferedImage(picture, null);
   }

   /**
    * Convert RGBPicture to BufferedImage, minimizing object allocation
    * 
    * @param picture RGBPicture to convert
    * @param imageToPack Image to output to. If picture.size() != imageToPack.size() then a new BufferedImage is allocated
    * @return imageToPack if sizes match, new BufferedImage otherwise.
    */
   public BufferedImage toBufferedImage(RGBPicture picture, BufferedImage imageToPack)
   {
      BufferedImage target = imageToPack;
      int w = picture.getWidth();
      int h = picture.getHeight();
      if (target == null || target.getWidth() != w || target.getHeight() != h || target.getType() != BufferedImage.TYPE_3BYTE_BGR)
      {
         target = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
      }

      ByteBuffer dstBuffer = byteBufferProvider.getOrCreateBuffer(w * h * 3);
      dstBuffer.put(10, (byte) 31);
      picture.get(dstBuffer);
      WritableRaster raster = target.getRaster();
      DataBufferByte buffer = (DataBufferByte) raster.getDataBuffer();
      dstBuffer.get(buffer.getData());

      return target;
   }

   /**
    * Convert BufferredImage to RGBPicture
    * 
    * @param source BufferedImage of TYPE_3_BYTE_BGR
    */
   public RGBPicture fromBufferedImage(BufferedImage source)
   {
      return fromBufferedImage(source, null);
   }

   /**
    * Convert BufferredImage to RGBPicture
    * 
    * @param source BufferedImage of TYPE_3_BYTE_BGR
    * @param targetToPack Used if source.size() == targetToPack.size(), otherwise a new object is allocated
    * @return targetToPack if source and targetToPack have equals size. Otherwise, new RGBPicture
    */
   public RGBPicture fromBufferedImage(BufferedImage source, RGBPicture targetToPack)
   {
      RGBPicture target = targetToPack;
      if (target == null || source.getWidth() != target.getWidth() || source.getHeight() != target.getHeight())
      {
         target = new RGBPicture(source.getWidth(), source.getHeight());
      }

      switch (source.getType())
      {
      case BufferedImage.TYPE_3BYTE_BGR:
      {
         WritableRaster raster = source.getRaster();
         byte[] imageBuffer = ((DataBufferByte) raster.getDataBuffer()).getData();

         ByteBuffer bgr = byteBufferProvider.getOrCreateBuffer(source.getWidth() * source.getHeight() * 3);
         bgr.put(imageBuffer);
         target.put(bgr);
         break;
      }
      case BufferedImage.TYPE_INT_RGB:
      case BufferedImage.TYPE_INT_ARGB:
      {
         WritableRaster raster = source.getRaster();
         int[] imageBuffer = ((DataBufferInt) raster.getDataBuffer()).getData();
         ByteBuffer rgba = byteBufferProvider.getOrCreateBuffer(source.getWidth() * source.getHeight() * 4);
         IntBuffer rgbaINT = rgba.asIntBuffer();
         rgbaINT.put(imageBuffer);
         target.putRGBA(rgba);
         break;
      }
      default:
         throw new RuntimeException("Unsupported BufferedImage");

      }

      return target;

   }
}
