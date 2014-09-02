package us.ihmc.codecs.demuxer;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.jcodec.common.NIOUtils;
import org.jcodec.common.SeekableByteChannel;
import org.jcodec.common.model.Packet;
import org.jcodec.containers.mp4.demuxer.AbstractMP4DemuxerTrack;
import org.jcodec.containers.mp4.demuxer.MP4Demuxer;

import us.ihmc.codecs.YUVPicture;

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
      return videoTrack.getMeta().getDimensions().getWidth();
   }

   /**
    * 
    * @return Vertical dimension of video track
    */
   public int getHeight()
   {
      return videoTrack.getMeta().getDimensions().getHeight();
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

}
