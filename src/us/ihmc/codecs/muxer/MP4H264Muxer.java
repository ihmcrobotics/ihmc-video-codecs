package us.ihmc.codecs.muxer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import org.jcodec.codecs.h264.mp4.AvcCBox;
import org.jcodec.common.NIOUtils;
import org.jcodec.common.SeekableByteChannel;
import org.jcodec.common.model.Size;
import org.jcodec.containers.mp4.MP4Packet;
import org.jcodec.containers.mp4.TrackType;
import org.jcodec.containers.mp4.boxes.SampleEntry;
import org.jcodec.containers.mp4.muxer.FramesMP4MuxerTrack;
import org.jcodec.containers.mp4.muxer.MP4Muxer;
import org.openh264.RC_MODES;
import org.openh264.SEncParamExt;

import us.ihmc.codecs.YUVPicture;
import us.ihmc.codecs.h264.NALProcessor;
import us.ihmc.codecs.h264.NALType;
import us.ihmc.codecs.h264.OpenH264Encoder;

public class MP4H264Muxer implements NALProcessor
{
   private final MP4Muxer muxer;
   private final FramesMP4MuxerTrack track;
   private final SeekableByteChannel channel;

   private final ArrayList<ByteBuffer> spsList = new ArrayList<ByteBuffer>();
   private final ArrayList<ByteBuffer> ppsList = new ArrayList<ByteBuffer>();

   private final int width;
   private final int height;

   private final int timescale;
   private long pts = 0;
   private long dts = 0;
   private long frameNo = 0;
   
   private final FileChannel out = new FileOutputStream("test.h264").getChannel();

   /**
    * NALProcessor to mux a raw h264 stream into a mp4 file
    * 
    * @param file File to save to
    * @param fps frame rate of the video
    * @param width Width of the video
    * @param height Height of the video
    * @throws IOException 
    */
   public MP4H264Muxer(File file, int fps, int width, int height) throws IOException
   {
      this.width = width;
      this.height = height;

      channel = NIOUtils.writableFileChannel(file);
      muxer = new MP4Muxer(channel);

      timescale = fps;
      track = muxer.addTrack(TrackType.VIDEO, timescale);
   }

   public void compareAndAddUniqueSPS(ByteBuffer nalIn)
   {
      ByteBuffer nal = nalIn.duplicate();

      LIST: for (ByteBuffer sps : spsList)
      {
         sps = sps.duplicate();
         
         if (sps.remaining() == nal.remaining())
         {
            while (sps.remaining() > 0)
            {
               if (sps.get() != nal.get())
               {
                  continue LIST;
               }
            }
            return;
         }
      }

      spsList.add(nalIn);
   }

   public void compareAndAddUniquePPS(ByteBuffer nalIn)
   {
      ByteBuffer nal = nalIn.duplicate();

      LIST: for (ByteBuffer pps : ppsList)
      {
         pps = pps.duplicate();
         if (pps.remaining() == nal.remaining())
         {
            while (pps.remaining() > 0)
            {
               if (pps.get() != nal.get())
               {
                  continue LIST;
               }
            }
            return;
         }
      }

      ppsList.add(nalIn);
   }

   @Override
   public void processNal(NALType type, ByteBuffer nal) throws IOException
   {
      // Handle 0x00 0x00 0x00 0x00 0x01 start sequence if neccessary.
      if (nal.get(3) == 0 && nal.get(4) == 1)
      {
         nal.position(1);
         nal = nal.slice();
      }
      
      out.write(nal);
      nal.clear();
      
      switch (type)
      {
      case SPS:
         nal.position(5); // Skip header
         compareAndAddUniqueSPS(nal); 
         break;
      case PPS:
         nal.position(5); // Skip header
         compareAndAddUniquePPS(nal);
         break;
      default:
         boolean iframe = type == NALType.CODED_SLICE_IDR_PICTURE;
         nal.putInt(nal.remaining() - 4);
         nal.clear();
         MP4Packet packet = new MP4Packet(nal, pts, timescale, 1, frameNo, iframe, null, dts, 0);

         track.addFrame(packet);
         pts++;
         dts++;
         frameNo++;
         break;
      }
   }

   /**
    * Writes the header and closes the file
    * @throws IOException 
    */
   public void close() throws IOException
   {
      if (spsList.size() == 0)
      {
         throw new RuntimeException("No SPS available, aborting");
      }

      ByteBuffer sps = spsList.get(0);
      int profile = sps.get(5);
      int level = sps.get(7);

      if(spsList.size() > 31)
      {
         throw new IOException("Cannot handle more than 31 unique SPS NALs. Set EnableSpsPpsIdAddition to false");
      }
      
      if(ppsList.size() > 255)
      {
         throw new IOException("Cannot handle more than 255 unique PPS NALs. Set EnableSpsPpsIdAddition to false");         
      }

      AvcCBox avcCBox = new AvcCBox(profile, 0, level, spsList, ppsList);
      Size size = new Size(width, height);
      SampleEntry sampleEntry = MP4Muxer.videoSampleEntry("avc1", size, "OpenH264");
      sampleEntry.add(avcCBox);

      track.addSampleEntry(sampleEntry);
      muxer.writeHeader();

      channel.close();
      
      out.close();
      
     
   }

   public static void main(String[] args) throws IOException
   {
      int w = 1920;
      int h = 1080;

      MP4H264Muxer muxer = new MP4H264Muxer(new File("test.mp4"), 30, w, h);
      OpenH264Encoder encoder = new OpenH264Encoder();
      SEncParamExt params = encoder.createParamExt(w, h, Integer.MAX_VALUE, RC_MODES.RC_OFF_MODE);
      params.setUiIntraPeriod(100);
      params.setBEnableSpsPpsIdAddition(false);
      encoder.initialize(params);

      for (int i = 1; i < 1000; i += 1)
      {
         BufferedImage img = ImageIO.read(new File("data/image_" + i + ".jpg"));
         YUVPicture picture = new YUVPicture(img);
         encoder.encodeFrame(picture, muxer);
      }

      encoder.delete();
      muxer.close();
   }
   
   public class ByteBufferBackedInputStream extends InputStream {

      private final ByteBuffer buf;

      public ByteBufferBackedInputStream(ByteBuffer buf) {
          // make a coy of the buffer
          this.buf = buf.duplicate();
      }

      public int read() throws IOException {
          if (!buf.hasRemaining()) {
              return -1;
          }
          return buf.get() & 0xFF;
      }

      public int read(byte[] bytes, int off, int len)
              throws IOException {
          if (!buf.hasRemaining()) {
              return -1;
          }

          len = Math.min(len, buf.remaining());
          buf.get(bytes, off, len);
          return len;
      }
  }

}
