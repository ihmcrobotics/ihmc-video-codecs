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

import java.io.File;
import java.io.IOException;

import us.ihmc.codecs.builder.MP4MJPEGMovieBuilder;
import us.ihmc.codecs.builder.MovieBuilder;
import us.ihmc.codecs.generated.YUVPicture;
import us.ihmc.codecs.yuv.JPEGDecoder;

public class CreateMP4WithJPEGExample extends Thread
{
   private static final int THREADS = 4;

   private final String filename;

   public CreateMP4WithJPEGExample(String filename)
   {
      this.filename = filename;
   }

   public void run()
   {
      try
      {
         int width = 1280;
         int height = 720;
         int framerate = 10;

         MovieBuilder builder = new MP4MJPEGMovieBuilder(new File(filename), width, height, framerate, 95);

         JPEGDecoder decoder = new JPEGDecoder();
         System.out.println("Writing movie " + filename);
         for (int i = 1; i < 1000; i += 1)
         {
            YUVPicture pic = decoder.readJPEG(new File("data/image_" + i + ".jpg"));
            builder.encodeFrame(pic);
            pic.delete();
         }
         System.out.println("Done writing " + filename);

         decoder.delete();
         builder.close();
      }
      catch (IOException e)
      {
         throw new RuntimeException(e);
      }
   }

   public static void main(String[] args) throws IOException
   {
      for (int i = 0; i < THREADS; i++)
      {
         new CreateMP4WithJPEGExample("testMJPEG_" + i + ".mp4").start();
      }
   }
}
