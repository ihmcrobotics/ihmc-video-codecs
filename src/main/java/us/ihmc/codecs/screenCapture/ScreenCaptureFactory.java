package us.ihmc.codecs.screenCapture;

import us.ihmc.codecs.loader.NativeLibraryLoader;

public class ScreenCaptureFactory
{
   public static ScreenCapture getScreenCapture()
   {
      if(NativeLibraryLoader.isX86_64())
      {
         if(NativeLibraryLoader.isLinux())
         {
            return new LinuxNativeScreenCapture();
         }
      }
      
      // Default, slow method
      return new RobotScreenCapture();
   }
}
