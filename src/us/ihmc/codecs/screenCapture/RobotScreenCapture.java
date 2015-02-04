package us.ihmc.codecs.screenCapture;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;

import us.ihmc.codecs.generated.RGBPicture;
import us.ihmc.codecs.yuv.RGBPictureConverter;

public class RobotScreenCapture implements ScreenCapture
{
   private final Robot robot;
   private final RGBPictureConverter converter = new RGBPictureConverter();
   
   public RobotScreenCapture()
   {
      try
      {
         this.robot = new Robot();
      }
      catch (AWTException e)
      {
         throw new RuntimeException(e);
      }
   }

   @Override
   public RGBPicture createScreenCapture(Rectangle bounds)
   {
      return converter.fromBufferedImage(robot.createScreenCapture(bounds));
   }

}
