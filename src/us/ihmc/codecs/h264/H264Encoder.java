package us.ihmc.codecs.h264;

import java.io.IOException;

import us.ihmc.codecs.YUVPicture;

/**
 * Common interface to a H264 encoder
 *
 * Created by jesper on 8/19/14.
 */
public interface H264Encoder
{

   /** 
    * Encode a single YUVPicture frame
    * 
    * @param picture 
    * @param nalProcessor
    * @throws IOException
    */
   void encodeFrame(YUVPicture picture, NALProcessor nalProcessor) throws IOException;

   /**
    * Change the resolution of the frames after the encoder is initialized. Can be called during encoding.
    * 
    * @param width Width of incoming frames
    * @param height Height of incoming frames
    */
   public void setResolution(int width, int height);
   
   /**
    * Set target bitrate after the encoder is initialized. Can be called during encoding
    * 
    * @param bitrate new desired bitrate
    */
   public void setTargetBitRate(int bitrate);

   /**
    * Set target frame rate for bandwidth control
    * @param fps
    */
   void setFrameRate(float fps);

   /**
    * Set the IDR Period. 0 means no intra period. Must be a multiple of 2^temporal_layer.
    * 
    * A higher number means 
    * 
    * @param period
    */
   void setIDRPeriod(int period);
}
