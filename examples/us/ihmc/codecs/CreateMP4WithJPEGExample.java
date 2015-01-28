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

import us.ihmc.codecs.builder.JPEGMovieBuilder;
import us.ihmc.codecs.builder.MovieBuilder;
import us.ihmc.codecs.yuv.JPEGDecoder;
import us.ihmc.codecs.yuv.YUVPicture;

public class CreateMP4WithJPEGExample
{
   public static void main(String[] args) throws IOException
   {
      int width = 1280;
      int height = 720;
      int framerate = 10;

      MovieBuilder builder = new JPEGMovieBuilder(new File("test.mp4"), width, height, framerate, 0.9f);
      
      JPEGDecoder decoder = new JPEGDecoder();
      System.out.println("Writing movie");
      for (int i = 1; i < 100; i += 1)
      {
         System.out.print(".");
         System.out.flush();
         if(i % 100 == 0)
         {
            System.out.println();
         }
         
         YUVPicture pic = decoder.readJPEG(new File("data/image_" + i + ".jpg"));
         builder.encodeFrame(pic);
      }
      System.out.println();
      
      decoder.delete();
      builder.close();
   }
}
