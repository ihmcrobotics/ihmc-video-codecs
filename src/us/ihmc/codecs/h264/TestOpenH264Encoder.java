package us.ihmc.codecs.h264;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import us.ihmc.codecs.YUVPicture;

/**
 *
 * Created by jesper on 8/19/14.
 */
public class TestOpenH264Encoder
{

   public static void showImage(BufferedImage img)
   {
      JFrame window = new JFrame("img");
      window.getContentPane().setPreferredSize(new Dimension(img.getWidth(), img.getHeight()));

      window.getContentPane().add(new JLabel(new ImageIcon(img)));
      window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      window.pack();
      window.setVisible(true);
   }

   public static void main(String[] args) throws IOException
   {
      OpenH264Encoder encoder = new OpenH264Encoder(1920, 1080);
      final OpenH264Decoder decoder = new OpenH264Decoder();

      for (int i = 1000; i < 5000; i += 100)
      {
         BufferedImage img = ImageIO.read(new File("data/out_" + i + ".png"));

         YUVPicture pic = new YUVPicture(img);
         encoder.encodeFrame(pic, new NALProcessor()
         {

            @Override
            public void processNAL(ByteBuffer nal)
            {
               try
               {
                  YUVPicture img = decoder.decodeFrame(nal);
                  if (img != null)
                  {
                     showImage(img.getImage());
                  }
               }
               catch (IOException e)
               {
                  throw new RuntimeException(e);
               }

            }
         });
      }
      
      encoder.delete();
      decoder.delete();
   }

}
