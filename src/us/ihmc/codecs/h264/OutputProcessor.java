package us.ihmc.codecs.h264;

import java.awt.image.BufferedImage;

public interface OutputProcessor
{
   public void processFrame(BufferedImage frame);
}
