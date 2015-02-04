package us.ihmc.codecs.builder;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.jcodec.common.FileChannelWrapper;
import org.jcodec.common.NIOUtils;
import org.jcodec.common.model.Size;
import org.jcodec.containers.mp4.MP4Packet;
import org.jcodec.containers.mp4.TrackType;
import org.jcodec.containers.mp4.boxes.SampleEntry;
import org.jcodec.containers.mp4.muxer.FramesMP4MuxerTrack;
import org.jcodec.containers.mp4.muxer.MP4Muxer;

import us.ihmc.codecs.generated.YUVPicture;
import us.ihmc.codecs.generated.YUVPicture.YUVSubsamplingType;
import us.ihmc.codecs.yuv.JPEGEncoder;
import us.ihmc.codecs.yuv.YUVPictureConverter;

public class MP4MJPEGMovieBuilder implements MovieBuilder
{
   
   private FileChannelWrapper channel;
   private MP4Muxer muxer;
   private int timescale;
   private FramesMP4MuxerTrack track;
   
   private long pts = 0;
   private long dts = 0;
   private long frameNo = 0;

   private final int width;
   private final int height;
   private final int quality; 
   
   private YUVPictureConverter converter;
   private final JPEGEncoder encoder = new JPEGEncoder();
   
   /**
    * Create an MP4 file with MJPEG
    * 
    * @param file Output file
    * @param width Frame width
    * @param height Frame height
    * @param framerate
    * @param quality JPEG quality 0 - 100
    * @throws IOException
    */
   public MP4MJPEGMovieBuilder(File file, int width, int height, int framerate, int quality) throws IOException
   {
      channel = NIOUtils.writableFileChannel(file);
      muxer = new MP4Muxer(channel);

      timescale = framerate;
      track = muxer.addTrack(TrackType.VIDEO, timescale);

      this.width = width;
      this.height = height;
      this.quality = quality;
   }

   @Override
   public void encodeFrame(BufferedImage frame) throws IOException
   {
      if(converter == null)
      {
         converter = new YUVPictureConverter();
      }
      YUVPicture pic = converter.fromBufferedImage(frame, YUVSubsamplingType.YUV420);
      encodeFrame(pic);
      pic.delete();
   }

   public void encodeFrame(ByteBuffer buffer) throws IOException
   {
      MP4Packet packet = new MP4Packet(buffer, pts, timescale, 1, frameNo, true, null, dts, 0);
      track.addFrame(packet);
      pts++;
      dts++;
      frameNo++;
   }

   @Override
   public void encodeFrame(YUVPicture frame) throws IOException
   {
      ByteBuffer buffer = encoder.encode(frame, quality);
      ByteBuffer heapBuffer = ByteBuffer.allocate(buffer.remaining());
      heapBuffer.put(buffer);
      heapBuffer.flip();
      encodeFrame(heapBuffer);
   }

   @Override
   public void close() throws IOException
   {
      Size size = new Size(width, height);
      SampleEntry sampleEntry = MP4Muxer.videoSampleEntry("jpeg", size, "IHMCVideoCodecs");
      track.addSampleEntry(sampleEntry);
      muxer.writeHeader();
      channel.close();
      encoder.delete();
   }

   @Override
   public int getWidth()
   {
      return width;
   }

   @Override
   public int getHeight()
   {
      return height;
   }

}
