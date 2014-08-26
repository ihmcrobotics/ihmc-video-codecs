package us.ihmc.codecs;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;

public interface VideoDecoder
{
   /**
    * Decode a single video frame. Output
    * @param input A single element of video data.
    * @throws IOException
    * 
    * @return videoframe. Can be null when decoder is not ready.
    */
   public BufferedImage decodeFrame(ByteBuffer input) throws IOException;
}
