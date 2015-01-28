package us.ihmc.codecs.screenCapture;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;

public class RobotScreenCapture implements ScreenCapture
{
   private final Robot robot;

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
   public BufferedImage createScreenCapture(Rectangle bounds)
   {
      return robot.createScreenCapture(bounds);
   }

}
