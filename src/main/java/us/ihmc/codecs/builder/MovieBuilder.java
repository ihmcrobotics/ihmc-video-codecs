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
import java.io.IOException;
import java.nio.ByteBuffer;

import us.ihmc.codecs.generated.YUVPicture;

/**
 * Simple to use interface for creating movies
 * 
 * @author Jesper Smith
 *
 */
public interface MovieBuilder
{
   /**
    * Encode a single image. Automatically gets resized to the movie width/height.
    * 
    * @param frame BufferedImage with frame data.
    * @throws IOException 
    */
   public void encodeFrame(BufferedImage frame) throws IOException;

   /**
    * Encode a single image. Automatically gets resized to the movie width/height.
    * 
    * @param frame YUVPicture with frame data.
    * @throws IOException 
    */
   public void encodeFrame(YUVPicture frame) throws IOException;
   
   /**
    * Encode a raw frame. 
    * 
    * This is implementation specific and not guaranteed to be implemented.
    * 
    * @param buffer
    * @throws IOException
    */
   public void encodeFrame(ByteBuffer buffer) throws IOException;
   
   /**
    * Close the stream and write headers
    * @throws IOException 
    */
   public void close() throws IOException;
   
   /**
    * 
    * @return Width of the movie
    */
   public int getWidth();
   
   /**
    * 
    * @return Height of the movie
    */
   public int getHeight();
   
}
