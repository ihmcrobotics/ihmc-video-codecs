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
 * // Colors that break: 255,0,21
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
         //        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_3BYTE_BGR);
         //        Graphics2D g = img.createGraphics();
         //        g.setColor(new Color(255, 0, 21));
         //        g.fillRect(0, 0, 100, 100);
         //        g.dispose();
         //        showImage(img);

         YUVPicture pic = new YUVPicture(img);
         encoder.encodeFrame(pic, new NALProcessor()
         {

            @Override
            public void processNAL(ByteBuffer nal)
            {
               System.out.println("ENCODED");
               try
               {
                  YUVPicture img = decoder.decodeFrame(nal);
                  System.out.println("Decoded 1 frame");
               }
               catch (IOException e)
               {
                  throw new RuntimeException(e);
               }

            }
         });
         //           channel.write(buf);

         //        Yb.clear(); CBb.clear(); CRb.clear();
         //
         //
         //        BufferedImage out = YCbCr420.convertYCbCr420ToRGB888(Yb, CBb, CRb, img.getWidth(), img.getHeight(), -1, -1);
         //        showImage(out);
      }
   }
   

}
