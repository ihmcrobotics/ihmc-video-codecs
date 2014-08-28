package us.ihmc.codecs.h264;

import java.nio.ByteBuffer;

public interface NALProcessor
{
   public void processNal(ByteBuffer nal);
}
