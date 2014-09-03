package us.ihmc.codecs;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import us.ihmc.codecs.demuxer.MP4VideoDemuxer;
import us.ihmc.codecs.yuv.YUV420Picture;
import us.ihmc.codecs.yuv.YUVPicture;

public class PlayMP4FileExample
{
   public static void main(String[] args) throws IOException
   {
      MP4VideoDemuxer demuxer = new MP4VideoDemuxer(new File("data/TripodVideo.mov"));
//      MP4VideoDemuxer demuxer = new MP4VideoDemuxer(new File("test.mp4"));

      JFrame frame = new JFrame("Decoder");
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      JLabel label = new JLabel();
      label.setPreferredSize(new Dimension(demuxer.getWidth(), demuxer.getHeight()));
      frame.getContentPane().add(label);
      frame.pack();
      frame.setVisible(true);

      System.out.println("Length of video " + demuxer.getDuration() + " seconds");

      YUVPicture picture;
      while ((picture = demuxer.getNextFrame()) != null)
      {
         ImageIcon icon = new ImageIcon(picture.getImage());
         label.setIcon(icon);
      }

      frame.dispose();
   }

}
