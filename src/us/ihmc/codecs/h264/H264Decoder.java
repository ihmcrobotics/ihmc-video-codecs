package us.ihmc.codecs.h264;

import java.io.IOException;
import java.nio.ByteBuffer;

import us.ihmc.codecs.yuv.YUVPicture;

public interface H264Decoder
{
   /**
    * Decode a single video frame. 
    * 
    * @param input A single nal.
    * @throws IOException
    * 
    * @return YUVPicture. Can be null when decoder is not ready.
    */
   public YUVPicture decodeFrame(ByteBuffer input) throws IOException;

}
