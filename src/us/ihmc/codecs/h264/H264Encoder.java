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

import us.ihmc.codecs.yuv.YUV420Picture;

/**
 * Common interface to a H264 encoder
 *
 * Created by jesper on 8/19/14.
 */
public interface H264Encoder
{

   /** 
    * Encode a single YUVPicture frame
    * 
    * @param picture 
    * @param nalProcessor
    * @throws IOException
    */
   void encodeFrame(YUV420Picture picture, NALProcessor nalProcessor) throws IOException;

   /**
    * Change the resolution of the frames after the encoder is initialized. Can be called during encoding.
    * 
    * @param width Width of incoming frames
    * @param height Height of incoming frames
    */
   public void setResolution(int width, int height);
   
   /**
    * Set target bitrate after the encoder is initialized. Can be called during encoding
    * 
    * @param bitrate new desired bitrate
    */
   public void setTargetBitRate(int bitrate);

   /**
    * Set target frame rate for bandwidth control
    * @param fps
    */
   public void setFrameRate(float fps);

   /**
    * Set the IDR Period. 0 means no intra period. Must be a multiple of 2^temporal_layer.
    * 
    * A higher number means 
    * 
    * @param period
    */
   public void setIDRPeriod(int period);
   
   /**
    * Send IDR frame. Use on reconnect
    * 
    */
   
   void sendIntraFrame();
}
