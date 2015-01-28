package us.ihmc.codecs.screenCapture;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.nio.ByteBuffer;

import us.ihmc.codecs.loader.NativeLibraryLoader;

public class LinuxNativeScreenCapture implements ScreenCapture
{
   private static boolean loaded = false;

   private static synchronized void loadLibrary()
   {
      if (!loaded)
      {
         NativeLibraryLoader.loadScreenShot();
         loaded = true;
      }
   }

   private ByteBuffer screenBuffer;

   public LinuxNativeScreenCapture()
   {
      loadLibrary();
   }

   @Override
   public BufferedImage createScreenCapture(Rectangle bounds)
   {
      int imageSize = bounds.height * bounds.width * 4;
      if (screenBuffer == null || screenBuffer.capacity() < imageSize)
      {
         screenBuffer = ByteBuffer.allocateDirect(imageSize);
      }
      else
      {
         screenBuffer.clear();
      }
      int bitsperpixel = ScreenShot.getPixels(screenBuffer, bounds.x, bounds.y, bounds.width, bounds.height);
      if(bitsperpixel != 32)
      {
         throw new RuntimeException("ScreenCapture expects 32 bits per pixel");
      }
      BufferedImage bufferedImage = new BufferedImage(bounds.width, bounds.height, BufferedImage.TYPE_3BYTE_BGR);

      WritableRaster wr = bufferedImage.getRaster();
      DataBufferByte db = (DataBufferByte) wr.getDataBuffer();

      byte[] imageArray = db.getData();
      for (int i = 0; i < imageArray.length; i += 3)
      {
         byte b = screenBuffer.get();
         byte g = screenBuffer.get();
         byte r = screenBuffer.get();
         /* byte a = */ screenBuffer.get();

         imageArray[i] = b;
         imageArray[i + 1] = g;
         imageArray[i + 2] = r;
      }

      return bufferedImage;
   }

}
