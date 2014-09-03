package us.ihmc.codecs;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;

import org.openh264.RC_MODES;

import us.ihmc.codecs.h264.NALProcessor;
import us.ihmc.codecs.h264.NALType;
import us.ihmc.codecs.h264.OpenH264Decoder;
import us.ihmc.codecs.h264.OpenH264Encoder;
import us.ihmc.codecs.yuv.YUV420Picture;
import us.ihmc.codecs.yuv.YUVPicture;

import com.google.code.libyuv.FilterModeEnum;

/**
 *
 * Created by jesper on 8/19/14.
 */
public class OpenH264EncoderExample
{
   public static void main(String[] args) throws IOException, InvocationTargetException, InterruptedException
   {
      new OpenH264EncoderExample();
   }

   private JFrame window;
   private JLabel image;
   private JSlider resolution;
   private JSlider bitrateSlider;

   private OpenH264Encoder encoder = new OpenH264Encoder();
   private OpenH264Decoder decoder = new OpenH264Decoder();

   private int width = 1920, height = 1080;
   private int bitrate = 5000000;

   public OpenH264EncoderExample() throws IOException, InvocationTargetException, InterruptedException
   {
      
      encoder.initialize(width, height, bitrate, RC_MODES.RC_LOW_BW_MODE);
      SwingUtilities.invokeAndWait(new Runnable()
      {
         @Override
         public void run()
         {
            createUI();
         }
      });

      for (int i = 1; i < 19036; i += 1)
      {
         BufferedImage img = ImageIO.read(new File("data/image_" + i + ".jpg"));

         checkResolution();
         checkBitrate();

         YUV420Picture pic = new YUV420Picture(img);
         if (pic.getWidth() != width || pic.getHeight() != height)
         {
            pic = pic.scale(width, height, FilterModeEnum.kFilterBilinear);
         }

         encoder.encodeFrame(pic, new NALProcessor()
         {

            @Override
            public void processNal(NALType type, ByteBuffer nal)
            {
               if(type == NALType.SPS || type == NALType.PPS)
               {
                  System.out.println(type);
               }
               try
               {
                  final YUVPicture img = decoder.decodeFrame(nal);
                  if (img != null)
                  {
                     SwingUtilities.invokeLater(new Runnable()
                     {
                        @Override
                        public void run()
                        {
                           image.setIcon(new ImageIcon(img.getImage()));
                        }
                     });
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

   private void checkResolution()
   {
      int newResolution = resolution.getValue() * 2;
      if (newResolution != width)
      {
         width = newResolution;
         height = (((newResolution * 9) / 16) >> 1) << 1;

         encoder.setResolution(width, height);
         image.setPreferredSize(new Dimension(width, height));

      }
   }

   private void checkBitrate()
   {
      int newBitrate = bitrateSlider.getValue();
      if (newBitrate != bitrate)
      {
         encoder.setTargetBitRate(newBitrate);
         this.bitrate = newBitrate;
      }
   }

   public void createUI()
   {
      window = new JFrame();
      Container main = window.getContentPane();
      main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));

      JPanel imagePanel = new JPanel();
      imagePanel.setPreferredSize(new Dimension(1920, 1080));
      image = new JLabel();
      image.setPreferredSize(new Dimension(1920, 1080));
      imagePanel.add(image);
      window.add(imagePanel);

      JPanel resolutionPanel = new JPanel();
      resolutionPanel.setLayout(new BoxLayout(resolutionPanel, BoxLayout.X_AXIS));
      resolutionPanel.add(new JLabel("Resolution: "));
      resolution = new JSlider(240, 960);
      resolution.setValue(width);
      resolutionPanel.add(resolution);
      main.add(resolutionPanel);

      JPanel bitratePanel = new JPanel();
      bitratePanel.setLayout(new BoxLayout(bitratePanel, BoxLayout.X_AXIS));
      bitratePanel.add(new JLabel("Bitrate: "));
      bitrateSlider = new JSlider(100, 10000000);
      bitrateSlider.setValue(bitrate);
      bitratePanel.add(bitrateSlider);
      main.add(bitratePanel);

      window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      window.pack();
      window.setVisible(true);
   }

}
