package us.ihmc.codecs.yuv;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.nio.ByteBuffer;

import us.ihmc.codecs.generated.RGBPicture;
import us.ihmc.codecs.util.ByteBufferProvider;

public class RGBPictureConverter
{
   private ByteBufferProvider byteBufferProvider = new ByteBufferProvider();

//   private ByteBuffer getOrAllocateBuffer(int w, int h)
//   {
//      int size = w * h * 3;
//      if (directBuffer == null)
//      {
//         directBuffer = ByteBuffer.allocateDirect(size);
//         return directBuffer;
//      }
//
//      directBuffer.clear();
//      if (directBuffer.capacity() < size)
//      {
//         directBuffer = ByteBuffer.allocateDirect(size);
//      }
//
//      return directBuffer;
//   }
   
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
      if (target == null || target.getWidth() != w || target.getHeight() != h
            || target.getType() != BufferedImage.TYPE_3BYTE_BGR)
      {
         target = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
      }
      
      
      ByteBuffer dstBuffer = byteBufferProvider.getOrCreateBuffer(w * h * 3);
      dstBuffer.put(10, (byte)31);
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
      if(source.getType() != BufferedImage.TYPE_3BYTE_BGR)
      {
         throw new RuntimeException("Unsupported BufferedImage");
      }
      
      if(target == null || source.getWidth() != target.getWidth() || source.getHeight() != target.getHeight())
      {
         target = new RGBPicture(source.getWidth(), source.getHeight());
      }
      
      WritableRaster raster = source.getRaster();
      byte[] imageBuffer = ((DataBufferByte) raster.getDataBuffer()).getData();

      ByteBuffer bgr = byteBufferProvider.getOrCreateBuffer(source.getWidth() * source.getHeight() * 3);
      bgr.put(imageBuffer);
      target.put(bgr);

      return target;
      
   }
}
