package us.ihmc.codecs.screenCapture;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

public interface ScreenCapture
{
   public BufferedImage createScreenCapture(Rectangle bounds);
}
