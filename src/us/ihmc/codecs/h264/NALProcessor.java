package us.ihmc.codecs.h264;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface NALProcessor
{
   public void processNal(NALType type, ByteBuffer nal) throws IOException;
}
