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
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import us.ihmc.codecs.demuxer.MP4VideoDemuxer;
import us.ihmc.codecs.generated.YUVPicture;

public class PlayMP4FileExample
{
   public static void main(String[] args) throws IOException
   {
//      MP4VideoDemuxer demuxer = new MP4VideoDemuxer(new File("data/TripodVideo.mov"));
      MP4VideoDemuxer demuxer = new MP4VideoDemuxer(new File("/home/jesper/robotLogs/20150130_095820_DRCSimulationFactory/NetworkStreamVideo.mov"));
      

      JFrame frame = new JFrame("Decoder");
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      final JLabel label = new JLabel();
      label.setPreferredSize(new Dimension(demuxer.getWidth(), demuxer.getHeight()));
      frame.getContentPane().add(label);
      frame.pack();
      frame.setVisible(true);

      demuxer.seek(300);
      
      
      YUVPicture picture;
      while ((picture = demuxer.getNextFrame()) != null)
      {
         final BufferedImage img = picture.getImage(); 
         SwingUtilities.invokeLater(new Runnable()
         {
            @Override
            public void run()
            {
               ImageIcon icon = new ImageIcon(img);
               label.setIcon(icon);
            }
         });
      }

      frame.dispose();
   }

}
