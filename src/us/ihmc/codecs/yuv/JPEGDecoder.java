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
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

import us.ihmc.codecs.generated.JPEGDecoderImpl;
import us.ihmc.codecs.generated.YUVPicture;
import us.ihmc.codecs.loader.NativeLibraryLoader;
import us.ihmc.codecs.util.ByteBufferProvider;

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

   private final ByteBufferProvider byteBufferProvider = new ByteBufferProvider();

   public YUVPicture decode(ByteBuffer buffer)
   {
      if (!buffer.isDirect())
      {
         throw new RuntimeException("Buffer must be allocated direct.");
      }
      return decode(buffer, buffer.remaining());
   }

   public YUVPicture readJPEG(URL jpeg) throws IOException
   {
      InputStream stream = jpeg.openStream();
      ReadableByteChannel channel = Channels.newChannel(stream);
      ByteBuffer buffer = byteBufferProvider.getOrCreateBuffer(stream.available());

      while (channel.read(buffer) >= 0)
      {
         if (buffer.remaining() == 0)
         {
            buffer = byteBufferProvider.growByteBuffer();
         }
      }

      buffer.flip();
      stream.close();
      YUVPicture picture = decode(buffer, buffer.remaining());
      return picture;
   }

   public YUVPicture readJPEG(File file) throws IOException
   {
      FileInputStream stream = new FileInputStream(file);
      FileChannel channel = stream.getChannel();
      ByteBuffer buffer = byteBufferProvider.getOrCreateBuffer((int) channel.size());
      channel.read(buffer);
      buffer.flip();
      stream.close();
      YUVPicture picture = decode(buffer, buffer.remaining());
      return picture;
   }

}
