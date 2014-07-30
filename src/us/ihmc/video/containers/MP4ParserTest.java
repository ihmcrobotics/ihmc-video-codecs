package us.ihmc.video.containers;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.TrackBox;
import com.coremedia.iso.boxes.mdat.SampleList;
import com.googlecode.mp4parser.BasicContainer;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Sample;

public class MP4ParserTest
{
   public MP4ParserTest() throws IOException
   {
      JFrame frame = new JFrame("JPEG DECODE");
      frame.setVisible(true);
      JLabel label = new JLabel();
      label.setPreferredSize(new Dimension(1920, 1080));
      frame.add(label);
      frame.pack();
      frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
      
      IsoFile isoFile = new IsoFile("/home/jesper/robotLogs/CraneVideo.mov");
      
      for(Box box : isoFile.getMovieBox().getBoxes())
      {
         if(box instanceof TrackBox)
         {
            SampleList sl = new SampleList((TrackBox)box);
            System.out.println(sl.get(0));
            for(Sample sample : sl)
            {
               
               ByteBuffer buffer = sample.asByteBuffer();
               buffer.clear();
               byte[] data = new byte[buffer.limit()];
               buffer.get(data);
               ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
               
               BufferedImage img = ImageIO.read(inputStream);
               label.setIcon(new ImageIcon(img));
            }
            
         }
      }
      
      isoFile.close();
      
      BasicContainer basicContainer = new BasicContainer();
      
      

   }
   
   public static void main(String[] args) throws IOException
   {
      new MP4ParserTest();
   }
}
