package us.ihmc.codecs;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import us.ihmc.codecs.builder.MP4H264MovieBuilder;
import us.ihmc.codecs.builder.MovieBuilder;

public class CreateMP4WithH264Example
{
   public static void main(String[] args) throws IOException
   {
      int width = 1920;
      int height = 1080;
      int framerate = 25;
      int bitrate = 8000;

      MovieBuilder builder = new MP4H264MovieBuilder(new File("test.mp4"), width, height, framerate, bitrate);
      
      System.out.println("Writing movie");
      for (int i = 200; i < 400; i += 1)
      {
         System.out.print(".");
         System.out.flush();
         if(i % 80 == 0)
         {
            System.out.println();
         }
         BufferedImage img = ImageIO.read(new File("data/image_" + i + ".jpg"));
         builder.encodeFrame(img);
      }
      System.out.println();
      
      builder.close();
   }
}
