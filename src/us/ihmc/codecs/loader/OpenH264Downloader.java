package us.ihmc.codecs.loader;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

public class OpenH264Downloader
{
   private static final String repository = "http://ciscobinary.openh264.org/";
   private static final String ext = ".bz2";

   public static final String android = "libopenh264-1.1.0-android19.so";
   public static final String ios32 = "libopenh264-1.1.0-ios32.a";
   public static final String ios64 = "libopenh264-1.1.0-ios64.a";
   public static final String linux32 = "libopenh264-1.1.0-linux32.so";
   public static final String linux64 = "libopenh264-1.1.0-linux64.so";
   public static final String osx32 = "libopenh264-1.1.0-osx32.dylib";
   public static final String osx64 = "libopenh264-1.1.0-osx64.dylib";
   public static final String win32 = "openh264-1.1.0-win32msvc.dll";
   public static final String win64 = "openh264-1.1.0-win64msvc.dll";

   private OpenH264Downloader()
   {
      //Disallow construction
   }

   public static String getLibraryName()
   {
      if (NativeLibraryLoader.isX86_64())
      {
         if (NativeLibraryLoader.isLinux())
         {
            return linux64;
         }
         else if (NativeLibraryLoader.isMac())
         {
            return osx64;
         }
         else if (NativeLibraryLoader.isWindows())
         {
            return win64;
         }
      }
      else
      // assume 32 bit
      {
         if (NativeLibraryLoader.isLinux())
         {
            return linux32;
         }
         else if (NativeLibraryLoader.isMac())
         {
            return osx32;
         }
         else if (NativeLibraryLoader.isWindows())
         {
            return win32;
         }
      }

      throw new RuntimeException("Cannot load archive for " + System.getProperty("os.name") + "/" + System.getProperty("os.arch"));
   }

   private static void downloadOpenH264(File target, String libraryName)
   {

      acceptLicense();

      try
      {
         URL url = new URL(repository + libraryName + ext);
         InputStream remote = url.openStream();
         System.out.println("Downloading " + url + " to " + target);
         BZip2CompressorInputStream decompressor = new BZip2CompressorInputStream(remote);
         NativeLibraryLoader.writeStreamToFile(decompressor, target);
         remote.close();

      }
      catch (MalformedURLException e)
      {
         throw new RuntimeException(e);
      }
      catch (IOException e)
      {
         new RuntimeException(e);
      }
   }

   public static void loadOpenH264()
   {
      String libraryName = getLibraryName();
      File directory = new File(System.getProperty("user.home"), ".ihmc/lib");
      if (!directory.exists())
      {
         directory.mkdirs();
      }

      File library = new File(directory, "libopenh264.so");

      if (!library.exists())
      {
         downloadOpenH264(library, libraryName);
      } 
      
      if(NativeLibraryLoader.isWindows())
      {
         System.load(library.getAbsolutePath());
      }
   }

   private static void acceptLicense()
   {
      if (GraphicsEnvironment.isHeadless())
      {
         acceptLicenseConsole();
      }
      else
      {
         acceptLicenseGUI();
      }
   }

   private static void acceptLicenseGUI()
   {

      JPanel panel = new JPanel();
      panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
      JTextArea license = new JTextArea(getLicenseText());
      license.setEditable(false);
      license.setLineWrap(true);
      license.setWrapStyleWord(true);
      JScrollPane scroll = new JScrollPane(license);
      scroll.setPreferredSize(new Dimension(500, 500));
      panel.add(scroll);
      panel.add(new JLabel("Do you accept the OpenH264 License?"));

      if(JOptionPane.showOptionDialog(null, panel, "OpenH264 License", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null) != JOptionPane.YES_OPTION)
      {
         JOptionPane.showMessageDialog(null, "User did not accept OpenH264 license", "License not accepted", JOptionPane.ERROR_MESSAGE);
         System.exit(-1);
      }

   }

   private static void acceptLicenseConsole()
   {
      System.out.println(getLicenseText());
      System.out.println("Do you accept the OpenH264 License? [Y/N]");

      String in = System.console().readLine();

      if (!in.toLowerCase().startsWith("y"))
      {
         System.err.println("License not accepted");
         System.exit(-1);
      }
   }

   public static String getLicenseText()
   {
      InputStream in = OpenH264Downloader.class.getClassLoader().getResourceAsStream("OPENH264_BINARY_LICENSE.txt");
      Reader reader = new BufferedReader(new InputStreamReader(in));
      StringBuilder builder = new StringBuilder();

      try
      {
         char[] buf = new char[1024];
         int len;
         while ((len = reader.read(buf)) > 0)
         {
            builder.append(buf, 0, len);
         }

         reader.close();
         in.close();
      }
      catch (IOException e)
      {
         return e.getMessage();
      }

      return builder.toString();
   }
   
   public static void main(String[] args)
   {
      loadOpenH264();
   }

}
