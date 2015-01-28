/*
 *   Copyright 2014 Florida Institute for Human and Machine Cognition (IHMC)
 *    
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *    
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *    
 *    Written by Jesper Smith with assistance from IHMC team members
 */
package us.ihmc.codecs;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import us.ihmc.codecs.screenCapture.ScreenCapture;
import us.ihmc.codecs.screenCapture.ScreenCaptureFactory;

public class CreateScreenShotExample
{
   private static int width = 1920;
   private static int height = 1080;
   
   public static void main(String[] args) throws IOException
   {
      JFrame frame = new JFrame("ScreenShot");
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      final JLabel label = new JLabel();
      label.setPreferredSize(new Dimension(width, height));
      frame.getContentPane().add(label);
      frame.pack();
      frame.setVisible(true);

      ScreenCapture capture = ScreenCaptureFactory.getScreenCapture();
      Rectangle bounds = new Rectangle(0, 0, width, height);
      
      while(true)
      {
         long start = System.nanoTime();
         BufferedImage img = capture.createScreenCapture(bounds);
         System.out.println((System.nanoTime() - start) / 1e9);
         ImageIcon icon = new ImageIcon(img);
         label.setIcon(icon);
         try
         {
            Thread.sleep(100);
         }
         catch (InterruptedException e)
         {
         }
      }
   }

}
