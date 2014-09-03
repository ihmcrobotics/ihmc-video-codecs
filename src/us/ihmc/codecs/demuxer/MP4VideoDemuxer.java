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

import java.io.File;
import java.io.IOException;

import org.jcodec.common.NIOUtils;
import org.jcodec.common.SeekableByteChannel;
import org.jcodec.common.model.Packet;
import org.jcodec.containers.mp4.boxes.VideoSampleEntry;
import org.jcodec.containers.mp4.demuxer.AbstractMP4DemuxerTrack;
import org.jcodec.containers.mp4.demuxer.MP4Demuxer;

import us.ihmc.codecs.yuv.YUVPicture;

public class MP4VideoDemuxer
{
   private final AbstractMP4DemuxerTrack videoTrack;
   private final DemuxerHelper demuxerHelper;

   public MP4VideoDemuxer(File file) throws IOException
   {

      SeekableByteChannel input = NIOUtils.readableFileChannel(file);
      MP4Demuxer demuxer = new MP4Demuxer(input);

      videoTrack = demuxer.getVideoTrack();
      String fourcc = videoTrack.getFourcc();

      if (fourcc.equals("avc1"))
      {
         demuxerHelper = new AVCDemuxerHelper(videoTrack.getSampleEntries());
      }
      else if (fourcc.equals("jpeg"))
      {
         demuxerHelper = new JPEGDemuxerHelper(videoTrack.getSampleEntries());
      }
      else
      {
         throw new RuntimeException("Cannot decode fourcc " + fourcc);
      }
   }

   /**
    * Return the next frame in the video 
    * 
    * @return YUVPicture if able to decode, null on last frame
    * 
    * @throws IOException
    */
   public YUVPicture getNextFrame() throws IOException
   {
      Packet nextFrame = videoTrack.nextFrame();
      if (nextFrame != null)
      {
         return demuxerHelper.getFrame(nextFrame);
      }
      else
      {
         return null;
      }
   }

   /**
    * 
    * @return Horizontal dimension of video track
    */
   public int getWidth()
   {
      return ((VideoSampleEntry)videoTrack.getSampleEntries()[0]).getWidth();
   }

   /**
    * 
    * @return Vertical dimension of video track
    */
   public int getHeight()
   {
      return ((VideoSampleEntry)videoTrack.getSampleEntries()[0]).getHeight();
   }

   /**
    * 
    * @return Timescale of track
    */
   public long getTimescale()
   {
      return videoTrack.getTimescale();
   }

   /**
    * 
    * @return Duration of video track in seconds
    */
   public double getDuration()
   {
      return videoTrack.getDuration().scalar();
   }
   
   /**
    * Delete native resources held by the demuxer
    */
   
   public void delete()
   {
      demuxerHelper.delete();
   }

}
