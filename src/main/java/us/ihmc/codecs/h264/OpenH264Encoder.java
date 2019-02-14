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

package us.ihmc.codecs.h264;

import java.io.IOException;
import java.nio.ByteBuffer;

import us.ihmc.codecs.generated.EUsageType;
import us.ihmc.codecs.generated.OpenH264EncoderImpl;
import us.ihmc.codecs.generated.YUVPicture;
import us.ihmc.codecs.loader.NativeLibraryLoader;
import us.ihmc.codecs.util.ByteBufferProvider;

/**
 * Easy to use class for the OpenH264 encoder. 
 * 
 * Make sure to call delete() when done.
 * 
 * @author Jesper Smith
 *
 */
public class OpenH264Encoder extends OpenH264EncoderImpl implements H264Encoder
{
   static
   {
      NativeLibraryLoader.loadIHMCVideoCodecsLibrary();
   }
   
   private ByteBufferProvider byteBufferProvider = new ByteBufferProvider();

   /**
    * Convenience initialization function, calls setSize, setMaxFrameRate, setBitRate, setRCMode before initialize()
    * 
    * @param width
    * @param height
    * @param framerate
    * @param bitrate
    * @param usageType
    */
   public void initialize(int width, int height, double framerate, int bitrate, EUsageType usageType)
   {
      setSize(width, height);
      setMaxFrameRate((float)framerate);
      setBitRate(bitrate);
      initialize();
   }
   
   @Override
   public ByteBuffer getNAL() throws IOException
   {
      int nalSize = getNALSize();
      if(nalSize > 0)
      {
         ByteBuffer buffer = byteBufferProvider.getOrCreateBuffer(nalSize);
         getNAL(buffer, buffer.capacity());
         buffer.limit(nalSize);
         return buffer;
      }
      else
      {
         throw new IOException("Cannot read NAL");
      }
   }

   @Override
   public void encodeFrame(YUVPicture picture) throws IOException
   {
      if(!encodeFrameImpl(picture))
      {
         throw new IOException("Cannot encode frame");
      }
   }

}
