package us.ihmc.codecs.builder;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import org.openh264.RC_MODES;
import org.openh264.SEncParamExt;

import us.ihmc.codecs.h264.OpenH264Encoder;
import us.ihmc.codecs.muxer.MP4H264Muxer;
import us.ihmc.codecs.yuv.YUV420Picture;

public class MP4H264MovieBuilder implements MovieBuilder
{
   
   private final int width;
   private final int height;
   
   private final OpenH264Encoder encoder;
   private final MP4H264Muxer muxer;
   
   /**
    * Simple to use class to create a new MP4 file with H264 data. 
    * 
    * @param file File to write to, needs enough space for the encoded data
    * @param width Width of the movie. Input frames automatically get resized
    * @param height Height of the movie. Input frames automatically get resized
    * @param framerate The number of frames per second of the resulting movie
    * @param bitrate Desired bitrate in kbit. Recommend 8000 kbits for full HD 
    * 
    * @throws IOException 
    */
   public MP4H264MovieBuilder(File file, int width, int height, int framerate, int bitrate) throws IOException
   {
      this.width = width;
      this.height = height;
      
      encoder = new OpenH264Encoder();
      SEncParamExt params = encoder.createParamExt(width, height, bitrate * 1000, RC_MODES.RC_QUALITY_MODE);
      params.setUiIntraPeriod(100);
      params.setBEnableSpsPpsIdAddition(false);
      encoder.initialize(params);
      
      
      muxer = new MP4H264Muxer(file, framerate, width, height);
      
   }

   @Override
   public void encodeFrame(BufferedImage frame) throws IOException
   {
      YUV420Picture picture = MovieBuilderTools.toYUV(frame, width, height);
      encoder.encodeFrame(picture, muxer);
   }

   @Override
   public void close() throws IOException
   {
      encoder.delete();
      muxer.close();
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
