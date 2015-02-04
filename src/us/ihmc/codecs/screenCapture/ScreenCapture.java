package us.ihmc.codecs.screenCapture;

import java.awt.Rectangle;

import us.ihmc.codecs.generated.RGBPicture;

public interface ScreenCapture
{
   public RGBPicture createScreenCapture(Rectangle bounds);
}
