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

package us.ihmc.codecs.builder;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import us.ihmc.codecs.generated.EUsageType;
import us.ihmc.codecs.generated.RC_MODES;
import us.ihmc.codecs.generated.YUVPicture;
import us.ihmc.codecs.generated.YUVPicture.YUVSubsamplingType;
import us.ihmc.codecs.h264.OpenH264Encoder;
import us.ihmc.codecs.muxer.MP4H264Muxer;
import us.ihmc.codecs.yuv.YUVPictureConverter;

public class MP4H264MovieBuilder implements MovieBuilder
{

   private final int width;
   private final int height;

   private final OpenH264Encoder encoder;
   private final MP4H264Muxer muxer;
   private final YUVPictureConverter converter = new YUVPictureConverter();

   /**
    * Simple to use class to create a new MP4 file with H264 data. 
    * 
    * @param file File to write to, needs enough space for the encoded data
    * @param width Width of the movie. Input frames automatically get resized
    * @param height Height of the movie. Input frames automatically get resized
    * @param framerate The number of frames per second of the resulting movie
    * @param bitrate Desired bitrate in kbit. Recommend 8000 kbits for full HD 
    * 
    * @throws IOException 
    */
   public MP4H264MovieBuilder(File file, int width, int height, int framerate, int bitrate, EUsageType usageType) throws IOException
   {
      this.width = width;
      this.height = height;

      encoder = new OpenH264Encoder();
      encoder.setIntraPeriod(100);
      encoder.setEnableSpsPpsIdAddition(false);
      encoder.setUsageType(usageType);
      encoder.setRCMode(RC_MODES.RC_QUALITY_MODE);
      encoder.setEnableDenoise(true);
      encoder.initialize(width, height, framerate, bitrate * 1024, usageType);

      muxer = new MP4H264Muxer(file, framerate, width, height);

   }

   @Override
   public void encodeFrame(BufferedImage frame) throws IOException
   {
      YUVPicture picture = converter.fromBufferedImage(frame, YUVSubsamplingType.YUV420);
      encoder.encodeFrame(picture);
   }

   @Override
   public void encodeFrame(YUVPicture picture) throws IOException
   {
      encoder.encodeFrame(picture);
      while(encoder.nextNAL())
      {
         ByteBuffer stream = encoder.getNAL();
         muxer.processNal(stream);
      }
   }

   @Override
   public void close() throws IOException
   {
      encoder.delete();
      muxer.close();
   }

   @Override
   public int getWidth()
   {
      return width;
   }

   @Override
   public int getHeight()
   {
      return height;
   }

}
