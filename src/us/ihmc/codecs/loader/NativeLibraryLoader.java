package us.ihmc.codecs.loader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;

import us.ihmc.codecs.Version;

/**
 * Helper class that unpacks the native libraries and download openH264
 * 
 * @author Jesper Smith
 *
 */
public class NativeLibraryLoader
{
   public final static String LIBRARY_LOCATION = new File(System.getProperty("user.home"), ".ihmc" + File.separator + "lib").getAbsolutePath();
   
   private final static String LIBYUV_MAC_64 = "liblibyuv.jnilib";
   private final static String LIBYUV_LINUX_64 = "liblibyuv.so";
   private final static String LIBYUV_WINDOWS_64 = "libyuv.dll";
   
   private final static String OPENH264BRIDGE_MAC_64 = "libopenh264bridge.jnilib";
   private final static String OPENH264BRIDGE_LINUX_64 = "libopenh264bridge.so";
   private final static String OPENH264BRIDGE_WINDOWS_64 = "openh264bridge.dll";
   
   private final static String SCREENSHOT_LINUX = "libscreenshot.so";
   
   
   private static final HashSet<String> loadedLibraries = new HashSet<String>();
   
   private NativeLibraryLoader()
   {
      // Disallow construction
   }
   
   private static String getScreenshotName()
   {
      if(isX86_64())
      {
         if(isLinux())
         {
            return SCREENSHOT_LINUX;
         }
      }
      
      throw new RuntimeException(System.getProperty("os.name") + "/" + System.getProperty("os.arch")
            + " unsupported. Only 64bit Linux/Mac/Windows supported for now.");
   }
   
   private static String getOpenH264BridgeName()
   {
      if(isX86_64())
      {
         if(isLinux())
         {
            return OPENH264BRIDGE_LINUX_64;
         }
         else if (isMac())
         {
            return OPENH264BRIDGE_MAC_64;
         }
         else if (isWindows())
         {
            return OPENH264BRIDGE_WINDOWS_64;
         }
      }
      
      throw new RuntimeException(System.getProperty("os.name") + "/" + System.getProperty("os.arch")
            + " unsupported. Only 64bit Linux/Mac/Windows supported for now.");
   }

   private static String getYUVLibraryName()
   {
      if(isX86_64())
      {
         if(isLinux())
         {
            return LIBYUV_LINUX_64;
         }
         else if (isMac())
         {
            return LIBYUV_MAC_64;
         }
         else if (isWindows())
         {
            return LIBYUV_WINDOWS_64;
         }
      }
      
      throw new RuntimeException("OS/Arch unsupported. Only 64bit Linux/Mac/Windows supported for now.");
      
   }
   
   public static void loadLibYUV()
   {
      String libyuv = getYUVLibraryName();
      loadLibraryFromClassPath(libyuv, Version.VERSION);  
   }
   
   public static void loadOpenH264Bridge()
   {
      OpenH264Downloader.loadOpenH264();
      String libopenH264bridge = getOpenH264BridgeName();
      loadLibraryFromClassPath(libopenH264bridge, Version.VERSION);
   }
   
   public static void loadScreenShot()
   {
      loadLibraryFromClassPath(getScreenshotName(), Version.VERSION);
   }

   private synchronized static void loadLibraryFromClassPath(String library, String version)
   {
      if(loadedLibraries.contains(library))
      {
         return;
      }
      File directory = new File(LIBRARY_LOCATION);
      if(!directory.exists())
      {
         directory.mkdirs();
      }
      File lib = new File(directory, library + "." + version);
      if(!lib.exists())
      {
         InputStream stream = NativeLibraryLoader.class.getClassLoader().getResourceAsStream(library);
         writeStreamToFile(stream, lib);
         
         try
         {
            stream.close();
         }
         catch (IOException e)
         {
         }
      }
	  System.load(lib.getAbsolutePath());         
    
      loadedLibraries.add(library);
   }
   
   public static void writeStreamToFile(InputStream stream, File file)
   {
      try
      {
         FileOutputStream out = new FileOutputStream(file);
         byte[] buf = new byte[1024];
         int len;
         while((len = stream.read(buf)) > 0)
         {
            out.write(buf, 0, len);
         }
         
         out.close();
         
      }
      catch (IOException e)
      {
         throw new RuntimeException(e);
      }
      
   }

   public static boolean isWindows()
   {
      return System.getProperty("os.name").startsWith("Windows");
   }

   public static boolean isLinux()
   {
      return System.getProperty("os.name").equals("Linux");
   }

   public static boolean isMac()
   {
      return System.getProperty("os.name").equals("MacOSX") || System.getProperty("os.name").equals("Mac OS X");
   }
   
   public static boolean isX86_64()
   {
      return System.getProperty("os.arch").contains("64");
   }
}
