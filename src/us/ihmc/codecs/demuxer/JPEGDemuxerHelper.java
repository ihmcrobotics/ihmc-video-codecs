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

package us.ihmc.codecs.demuxer;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.jcodec.common.model.Packet;
import org.jcodec.containers.mp4.boxes.SampleEntry;

import us.ihmc.codecs.yuv.JPEGDecoder;
import us.ihmc.codecs.yuv.YUVPicture;

public class JPEGDemuxerHelper implements DemuxerHelper
{

   private final JPEGDecoder decoder = new JPEGDecoder();

   public JPEGDemuxerHelper(SampleEntry[] sampleEntries)
   {
   }

   @Override
   public YUVPicture getFrame(Packet frame) throws IOException
   {
      ByteBuffer buf = frame.getData();
      return decoder.decode(buf);
   }

   @Override
   public void delete()
   {
      decoder.delete();
   }

   @Override
   public void finalize()
   {
      delete();
   }

   @Override
   public void skipFrame(Packet frame) throws IOException
   {
      // Nothing to do here, all frames are complete
   }

}
