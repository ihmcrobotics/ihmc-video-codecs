package us.ihmc.codecs.screenCapture;

import java.awt.Rectangle;
import java.nio.ByteBuffer;

import us.ihmc.codecs.generated.RGBPicture;
import us.ihmc.codecs.loader.NativeLibraryLoader;

public class LinuxNativeScreenCapture implements ScreenCapture
{
   private static boolean loaded = false;

   private static synchronized void loadLibrary()
   {
      if (!loaded)
      {
         NativeLibraryLoader.loadScreenShot();
         NativeLibraryLoader.loadIHMCVideoCodecsLibrary();
         loaded = true;
      }
   }

   private ByteBuffer screenBuffer;

   public LinuxNativeScreenCapture()
   {
      loadLibrary();
   }

   @Override
   public RGBPicture createScreenCapture(Rectangle bounds)
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
      if (bitsperpixel < 0)
      {
         switch (bitsperpixel)
         {
         case -1:
            System.err.println("Cannot open display.");
            return null;
         case -2:
            System.err.println("Cannot capture image.");
            return null;

         default:
            return null;
         }
      }
      else if (bitsperpixel != 32)
      {
         throw new RuntimeException("ScreenCapture expects 32 bits per pixel");
      }

      RGBPicture picture = new RGBPicture(bounds.width, bounds.height);
      picture.putRGBA(screenBuffer);

      return picture;
   }

}
