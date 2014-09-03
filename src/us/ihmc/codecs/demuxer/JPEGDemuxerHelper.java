package us.ihmc.codecs.demuxer;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.jcodec.common.model.Packet;
import org.jcodec.containers.mp4.boxes.SampleEntry;

import us.ihmc.codecs.yuv.YUV420Picture;
import us.ihmc.codecs.yuv.YUV422Picture;
import us.ihmc.codecs.yuv.YUV444Picture;
import us.ihmc.codecs.yuv.YUVPicture;
import us.ihmc.codecs.yuv.YUVPicture.YUVSubsamplingType;

import com.google.code.libyuv.MJpegDecoder;

public class JPEGDemuxerHelper implements DemuxerHelper
{
   static
   {
      System.loadLibrary("libyuv");
   }

   private final MJpegDecoder decoder = new MJpegDecoder();

   public static final int kColorSpaceUnknown = MJpegDecoder.getKColorSpaceUnknown();
   public static final int kColorSpaceGrayscale = MJpegDecoder.getKColorSpaceGrayscale();
   public static final int kColorSpaceRgb = MJpegDecoder.getKColorSpaceRgb();
   public static final int kColorSpaceYCbCr = MJpegDecoder.getKColorSpaceYCbCr();
   public static final int kColorSpaceCMYK = MJpegDecoder.getKColorSpaceCMYK();
   public static final int kColorSpaceYCCK = MJpegDecoder.getKColorSpaceYCCK();

   public JPEGDemuxerHelper(SampleEntry[] sampleEntries)
   {
      // TODO Auto-generated constructor stub
   }

   @Override
   public YUVPicture getFrame(Packet frame) throws IOException
   {

      ByteBuffer buf = frame.getData();

      //      System.out.println(decoder.GetWidth());
      //      System.out.println(decoder.GetHeight());

      ByteBuffer dup = buf.duplicate();
      ByteBuffer direct = ByteBuffer.allocateDirect(buf.remaining());
      for (int i = 0; i < direct.capacity(); i++)
      {
         direct.put(dup.get());
      }
      direct.clear();
      decoder.LoadFrame(direct, direct.remaining());
      int width = decoder.GetWidth();
      int height = decoder.GetHeight();

      int colorSpace = decoder.GetColorSpace();

      if (colorSpace == kColorSpaceUnknown)
      {
         throw new IOException("Unkown colorspace in MJPEG track");
      }
      else if (colorSpace == kColorSpaceRgb)
      {
         throw new IOException("Cannot handle RGB color space MJPEG track");
      }
      else if (colorSpace == kColorSpaceCMYK)
      {
         throw new IOException("Cannot handle CMYK color space MJPEG track");
      }
      else if (colorSpace == kColorSpaceYCCK)
      {
         throw new IOException("Cannot handle YCCK color space MJPEG track");
      }

      int numberOfPlanes = decoder.GetNumComponents();
      YUVSubsamplingType samplingType;

      if (numberOfPlanes != 3)
      {
         throw new IOException("Can only handle YUV colour MJPEG tracks with 3 components");
      }
      int planeWidths[] = { decoder.GetComponentWidth(0), decoder.GetComponentWidth(1), decoder.GetComponentWidth(2) };
      int planeHeights[] = { decoder.GetComponentHeight(0), decoder.GetComponentHeight(1), decoder.GetComponentHeight(2) };
      samplingType = YUVSubsamplingType.getSubsamplingType(numberOfPlanes, planeWidths, planeHeights);

      ByteBuffer Y = ByteBuffer.allocateDirect(planeWidths[0] * planeHeights[0]);
      ByteBuffer U = ByteBuffer.allocateDirect(planeWidths[1] * planeHeights[1]);
      ByteBuffer V = ByteBuffer.allocateDirect(planeWidths[2] * planeHeights[2]);

      decoder.Decode(Y, U, V);
      switch (samplingType)
      {
      case YUV420:
         return new YUV420Picture(Y, U, V, planeWidths[0], planeWidths[1], planeWidths[2], width, height);
      case YUV422:
         return new YUV422Picture(Y, U, V, planeWidths[0], planeWidths[1], planeWidths[2], width, height);
      case YUV444:
         return new YUV444Picture(Y, U, V, planeWidths[0], planeWidths[1], planeWidths[2], width, height);
      case UNSUPPORTED:
      default:
         throw new IOException("Unsupported sampling type");
      }

   }

   @Override
   public void delete()
   {
      decoder.delete();
   }

}
