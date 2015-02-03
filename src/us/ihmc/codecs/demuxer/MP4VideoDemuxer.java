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
import java.nio.ByteBuffer;

import org.jcodec.common.DemuxerTrackMeta;
import org.jcodec.common.NIOUtils;
import org.jcodec.common.SeekableByteChannel;
import org.jcodec.common.model.Packet;
import org.jcodec.containers.mp4.boxes.VideoSampleEntry;
import org.jcodec.containers.mp4.demuxer.AbstractMP4DemuxerTrack;
import org.jcodec.containers.mp4.demuxer.FramesMP4DemuxerTrack;
import org.jcodec.containers.mp4.demuxer.MP4Demuxer;

import us.ihmc.codecs.generated.YUVPicture;
import us.ihmc.codecs.util.MemoryManagement;

/**
 * Demuxer for MP4 files. Automatically detects the format and chooses the correct decoder.
 * 
 * Supported codecs:
 *    - H264 (Baseline)
 *    - MJPEG (YUV only)
 * 
 * @author Jesper Smith
 *
 */
public class MP4VideoDemuxer
{
   private final AbstractMP4DemuxerTrack videoTrack;
   private final DemuxerHelper demuxerHelper;
   private final ByteBuffer buffer;

   /**
    * Create a new demuxer
    * 
    * @param file File to oper
    * @throws IOException
    */
   public MP4VideoDemuxer(File file) throws IOException
   {
      SeekableByteChannel input = NIOUtils.readableFileChannel(file);
      MP4Demuxer demuxer = new MP4Demuxer(input);
      videoTrack = demuxer.getVideoTrack();
      if(videoTrack instanceof FramesMP4DemuxerTrack)
      {
         buffer = ByteBuffer.allocateDirect(((FramesMP4DemuxerTrack) videoTrack).getMaxSize());
      }
      else
      {
         throw new IOException("Incompatible video track");
      }
      
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
         throw new IOException("Cannot decode fourcc " + fourcc);
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
      buffer.clear();
      Packet nextFrame = videoTrack.nextFrame(buffer);
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
      return ((VideoSampleEntry) videoTrack.getSampleEntries()[0]).getWidth();
   }

   /**
    * 
    * @return Vertical dimension of video track
    */
   public int getHeight()
   {
      return ((VideoSampleEntry) videoTrack.getSampleEntries()[0]).getHeight();
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

   private long getPreviousKeyFrame(long start)
   {
      DemuxerTrackMeta meta = videoTrack.getMeta();
      if(meta == null)
      {
         // No information about keyframes available. Assume every frame is selfcontained.
         return start;
      }
      int[] seekFrames = meta.getSeekFrames();
      if (seekFrames == null)
      {
         return start;
      }
      int prev = seekFrames[0];
      for (int i = 1; i < seekFrames.length; i++)
      {
         if (seekFrames[i] > start)
         {
            break;
         }
         prev = seekFrames[i];
      }
      return prev;
   }

   /**
    * Seek to frame. Next call to nextFrame will return frameNo
    * 
    * @throws IOException Cannot decode intraframes
    */
   public void seekToFrame(long frameNo) throws IOException
   {
      if(!videoTrack.gotoFrame(frameNo))
      {
         throw new IOException("Invalid frame no: " + frameNo);
      }
      decodeLeadingFrames();
   }

   private void decodeLeadingFrames() throws IOException
   {
      long frameNo = videoTrack.getCurFrame();
      long keyFrame = getPreviousKeyFrame(frameNo);
      
      if(keyFrame == frameNo)
      {
         return;
      }

      if(!videoTrack.gotoFrame(keyFrame))
      {
         throw new IOException("Invalid frame no: " + keyFrame);
      }
      
      
      Packet frame;
      do
      {
         buffer.clear();
         frame = videoTrack.nextFrame(buffer);
         if(frame == null)
         {
            throw new IOException("Cannot decode frame");
         }
         demuxerHelper.skipFrame(frame); 
      }
      while(frame.getFrameNo() < (frameNo - 1));
   }

   /** 
    * Seek to PTS. Next call to nextFrame will return frame at given PTS
    * 
    * @throws IOException Cannot decode intraframes
    */
   public void seekToPTS(long pts) throws IOException
   {
      videoTrack.seek(pts);
      decodeLeadingFrames();
   }
   
   /**
    * Seek to time(s). Next call to nextFrame will return frame at given time.
    * 
    * @throws IOException Cannot decode intraframes
    */
   public void seek(double seconds) throws IOException
   {
      videoTrack.seek(seconds);
      decodeLeadingFrames();
   }
   
   /**
    * @return Current frame number the video is on
    */
   public long getCurrentFrame()
   {
      return videoTrack.getCurFrame();
   }

   /**
    * Delete native resources held by the demuxer
    */

   public void delete()
   {
      MemoryManagement.deallocateNativeByteBuffer(buffer);
      demuxerHelper.delete();
   }

}
