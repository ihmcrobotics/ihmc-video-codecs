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
package us.ihmc.codecs.yuv;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import us.ihmc.codecs.generated.JPEGDecoderImpl;
import us.ihmc.codecs.generated.YUVPicture;
import us.ihmc.codecs.loader.NativeLibraryLoader;
import us.ihmc.codecs.util.MemoryManagement;

/** 
 * JPEG decoder using libyuv
 * 
 * @author Jesper Smith
 *
 */
public class JPEGDecoder extends JPEGDecoderImpl
{
   static
   {
      NativeLibraryLoader.loadIHMCVideoCodecsLibrary();
   }

   public YUVPicture decode(ByteBuffer buffer)
   {
      return decode(buffer, buffer.remaining());
   }

   public YUVPicture readJPEG(File file) throws IOException
   {
      FileInputStream stream = new FileInputStream(file);
      FileChannel channel = stream.getChannel();
      ByteBuffer buffer = ByteBuffer.allocateDirect((int) channel.size());
      channel.read(buffer);
      buffer.flip();
      stream.close();
      YUVPicture picture = decode(buffer, buffer.remaining());
      MemoryManagement.deallocateNativeByteBuffer(buffer);
      return picture;
   }

}
