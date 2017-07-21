package us.ihmc.codecs.yuv;

import java.io.IOException;
import java.nio.ByteBuffer;

import us.ihmc.codecs.generated.JPEGEncoderImpl;
import us.ihmc.codecs.generated.YUVPicture;
import us.ihmc.codecs.loader.NativeLibraryLoader;
import us.ihmc.codecs.util.ByteBufferProvider;

/**
 * JPEG Encoder using libjpeg-turbo. Keeps an internal buffer for data marshalling.
 * 
 * @author jesper
 *
 */
public class JPEGEncoder extends JPEGEncoderImpl
{
   static
   {
      NativeLibraryLoader.loadIHMCVideoCodecsLibrary();
   }

   private ByteBufferProvider byteBufferProvider = new ByteBufferProvider();

   /**
    * Encode JPEG to buffer
    * 
    * @param picture Picture to encode. 
    * @param quality JPEG quality factor in 0-100 
    * @return Buffer with the JPEG data. Buffer.limit() equals the compressed length. This buffer gets re-used on subsequent calls to this function
    * @throws IOException 
    */
   public ByteBuffer encode(YUVPicture picture, int quality) throws IOException
   {
      ByteBuffer target = byteBufferProvider.getOrCreateBuffer((int) maxSize(picture));
      int size = encode(picture, target, target.capacity(), quality);
      if (size < 0)
      {
         throw new IOException("Cannot encode picture");
      }
      target.limit(size);
      return target;
   }

   /**
    * Encode JPEG to buffer
    * 
    * @param target Bytebuffer target. Position will be set to 0 and limit to compressed data length.
    * @param picture Picture to encode. 
    * @param quality JPEG quality factor in 0-100 
    * @throws IOException 
    */
   public void encode(ByteBuffer target, YUVPicture picture, int quality) throws IOException
   {
      int maxSize = (int) maxSize(picture);
      if (!target.isDirect())
      {
         throw new RuntimeException("Buffer must be allocated direct.");
      }
      if (target.capacity() < maxSize)
      {
         throw new IOException("Buffer is not large enough. Maximum compressed data size is " + maxSize + "bits");
      }
      int size = encode(picture, target, target.capacity(), quality);
      if (size < 0)
      {
         throw new IOException("Cannot encode picture");
      }
      target.limit(size);
   }
}
