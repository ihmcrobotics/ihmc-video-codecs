package us.ihmc.codecs;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import com.google.code.libyuv.FilterModeEnum;
import com.google.code.libyuv.libyuv;

public class YUVPicture
{
   static
   {
      System.loadLibrary("libyuv");
   }

   private final int w, h;
   private final int yStride, uStride, vStride;
   private final ByteBuffer Y, U, V;

   public YUVPicture(BufferedImage orig)
   {
      w = orig.getWidth();
      h = orig.getHeight();

      yStride = w;
      uStride = vStride = yStride >> 1;

      Y = ByteBuffer.allocateDirect(yStride * h);
      U = ByteBuffer.allocateDirect(uStride * (h >> 1));
      V = ByteBuffer.allocateDirect(vStride * (h >> 1));

      ByteBuffer src = getBuffer(orig);

      int srcStride = w * 3;
      libyuv.RGB24ToI420(src, srcStride, Y, yStride, U, uStride, V, vStride, w, h);

   }
   
   public YUVPicture(ByteBuffer src, int yStride, int uStride, int vStride, int w, int h)
   {
      src.position(0);
      Y = src.slice();
      src.position(yStride * h);
      U = src.slice();
      src.position(yStride * h + uStride * (h >> 1));
      V = src.slice();
      
      System.out.println(Y);
      System.out.println(U);
      System.out.println(V);
      
      this.w = w;
      this.h = h;
      this.yStride = yStride;
      this.uStride = uStride;
      this.vStride = vStride;
   }

   public YUVPicture(ByteBuffer Y, ByteBuffer U, ByteBuffer V, int yStride, int uStride, int vStride, int w, int h)
   {
      this.Y = Y;
      this.U = U;
      this.V = V;
      this.yStride = yStride;
      this.uStride = uStride;
      this.vStride = vStride;
      this.w = w;
      this.h = h;
   }

   public YUVPicture scale(int newWidth, int newHeight, FilterModeEnum filterMode)
   {
      if((newWidth >> 1) << 1 != newWidth || (newHeight >> 1) << 1 != newHeight)
      {
         throw new RuntimeException("Resolution not divisible by 2");
      }
      
      int yStrideDest = newWidth;
      int uStrideDest = yStrideDest >> 1;
      int vStrideDest = yStrideDest >> 1;

      ByteBuffer Ydest = ByteBuffer.allocateDirect(yStrideDest * newHeight);
      ByteBuffer Udest = ByteBuffer.allocateDirect(uStrideDest * (newHeight >> 1));
      ByteBuffer Vdest = ByteBuffer.allocateDirect(vStrideDest * (newHeight >> 1));

      libyuv.I420Scale(Y, yStride, U, uStride, V, vStride, w, h, Ydest, yStrideDest, Udest, uStrideDest, Vdest, vStrideDest, newWidth, newHeight, filterMode);

      return new YUVPicture(Ydest, Udest, Vdest, yStrideDest, uStrideDest, vStrideDest, newWidth, newHeight);
   }

   public BufferedImage getImage()
   {
      ByteBuffer dstBuffer = ByteBuffer.allocateDirect(w * h * 3);
      int dstStride = w * 3;
      libyuv.I420ToRGB24(Y, yStride, U, uStride, V, vStride, dstBuffer, dstStride, w, h);

      BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
      WritableRaster raster = img.getRaster();
      DataBufferByte buffer = (DataBufferByte) raster.getDataBuffer();

      for (int i = 0; dstBuffer.hasRemaining(); i++)
      {
         buffer.setElem(i, dstBuffer.get());
      }

      return img;
   }

   public static ByteBuffer getBuffer(BufferedImage in)
   {
      if (in.getType() != BufferedImage.TYPE_3BYTE_BGR)
      {
         throw new RuntimeException("Can only handle 3 byte bgr images");
      }
      WritableRaster raster = in.getRaster();
      byte[] imageBuffer = ((DataBufferByte) raster.getDataBuffer()).getData();

      ByteBuffer direct = ByteBuffer.allocateDirect(imageBuffer.length);
      direct.put(imageBuffer);
      return direct;
   }

   public static void showImage(BufferedImage img)
   {
      JFrame window = new JFrame("img");
      window.getContentPane().setPreferredSize(new Dimension(img.getWidth(), img.getHeight()));

      window.getContentPane().add(new JLabel(new ImageIcon(img)));
      window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      window.pack();
      window.setVisible(true);
   }

   public int getWidth()
   {
      return w;
   }

   public int getHeight()
   {
      return h;
   }

   public int getYStride()
   {
      return yStride;
   }

   public int getUStride()
   {
      return uStride;
   }

   public int getVStride()
   {
      return vStride;
   }

   public ByteBuffer getY()
   {
      return Y;
   }

   public ByteBuffer getU()
   {
      return U;
   }

   public ByteBuffer getV()
   {
      return V;
   }

   public static final String testFrame = "out_1743.png";

   public static void main(String[] args) throws IOException
   {
      BufferedImage img = ImageIO.read(new File("data/" + testFrame));
      showImage(img);

      YUVPicture pic = new YUVPicture(img);
      YUVPicture scaled = pic.scale(480, 270, FilterModeEnum.kFilterBilinear);

      showImage(pic.getImage());
      showImage(scaled.getImage());

   }

}
