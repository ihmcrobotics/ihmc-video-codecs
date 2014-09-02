package us.ihmc.codecs.muxer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.openh264.RC_MODES;
import org.openh264.SEncParamExt;

import com.googlecode.mp4parser.authoring.builder.UnbufferedH264TrackImpl;
import com.googlecode.mp4parser.authoring.builder.UnbufferedDefaultMP4Builder;

import us.ihmc.codecs.YUVPicture;
import us.ihmc.codecs.h264.OpenH264Encoder;

public class MP4H264Movie
{
   public static void main(String[] args) throws IOException
   {
      int w = 1920;
      int h = 1080;

      UnbufferedDefaultMP4Builder builder = new UnbufferedDefaultMP4Builder(new File("testIsoParser.mp4"), 25);
      UnbufferedH264TrackImpl track = new UnbufferedH264TrackImpl(builder, "eng", 25, 1);
      
      OpenH264Encoder encoder = new OpenH264Encoder();
      SEncParamExt params = encoder.createParamExt(w, h, Integer.MAX_VALUE, RC_MODES.RC_OFF_MODE);
      params.setUiIntraPeriod(10);
      params.setBEnableSpsPpsIdAddition(false);
      encoder.initialize(params);

      for (int i = 1; i < 1000; i += 1)
      {
         BufferedImage img = ImageIO.read(new File("data/image_" + i + ".jpg"));
         YUVPicture picture = new YUVPicture(img);
         encoder.encodeFrame(picture, track);
      }

      encoder.delete();
      builder.writeMetaData();
   }

}
