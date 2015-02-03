package us.ihmc.codecs.builder;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;

import org.jcodec.common.FileChannelWrapper;
import org.jcodec.common.NIOUtils;
import org.jcodec.common.model.Size;
import org.jcodec.containers.mp4.MP4Packet;
import org.jcodec.containers.mp4.TrackType;
import org.jcodec.containers.mp4.boxes.SampleEntry;
import org.jcodec.containers.mp4.muxer.FramesMP4MuxerTrack;
import org.jcodec.containers.mp4.muxer.MP4Muxer;

import us.ihmc.codecs.generated.YUVPicture;
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

   private final ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
   private final ImageWriteParam param = writer.getDefaultWriteParam();
   
   
   private final ByteBuffer buffer;
   private final ByteBufferImageOutputStream imageOutputStream;
   
   private YUVPictureConverter converter;
   
   public MP4MJPEGMovieBuilder(File file, int width, int height, int framerate, float quality) throws IOException
   {
      channel = NIOUtils.writableFileChannel(file);
      muxer = new MP4Muxer(channel);

      timescale = framerate;
      track = muxer.addTrack(TrackType.VIDEO, timescale);

      this.width = width;
      this.height = height;
      
      param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT); 
      param.setCompressionQuality(quality); 
      
      buffer = ByteBuffer.allocate(width * height * 4 * 4);
      imageOutputStream = new ByteBufferImageOutputStream(buffer);
      writer.setOutput(imageOutputStream);
   }

   @Override
   public void encodeFrame(BufferedImage frame) throws IOException
   {
      buffer.clear();
      writer.write(null, new IIOImage(frame, null, null), param);
      buffer.flip();
      encodeFrame(buffer);
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
      if(converter == null)
      {
         converter = new YUVPictureConverter();
      }
      encodeFrame(converter.toBufferedImage(frame));
   }

   @Override
   public void close() throws IOException
   {
      Size size = new Size(width, height);
      SampleEntry sampleEntry = MP4Muxer.videoSampleEntry("jpeg", size, "IHMCVideoCodecs");
      track.addSampleEntry(sampleEntry);
      muxer.writeHeader();
      channel.close();
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
