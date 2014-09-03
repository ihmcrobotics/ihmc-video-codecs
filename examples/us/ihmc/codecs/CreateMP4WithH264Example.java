/*
 *   Copyright 2014 Florida Institute for Human and Machine Cognition (IHMC)
 *    
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *    
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *    
 *    Written by Jesper Smith with assistance from IHMC team members
 */

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
