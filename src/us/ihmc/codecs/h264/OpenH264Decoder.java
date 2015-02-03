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

import us.ihmc.codecs.generated.OpenH264DecoderImpl;
import us.ihmc.codecs.generated.YUVPicture;
import us.ihmc.codecs.loader.NativeLibraryLoader;

/**
 * Wrapper class for the OpenH264 Decoder. Easy to use functions for decoding H264 streams. 
 * 
 * Make sure to call delete() after use.
 * 
 * 
 * @author Jesper Smith
 *
 */
public class OpenH264Decoder extends OpenH264DecoderImpl
{
   static
   {
      NativeLibraryLoader.loadIHMCVideoCodecsLibrary();
   }
   
   public YUVPicture decodeFrame(ByteBuffer buffer)
   {
      return decodeFrame(buffer, buffer.remaining());
   }
   
   public void skipFrame(ByteBuffer buffer)
   {
      skipFrame(buffer, buffer.remaining());
   }
}
