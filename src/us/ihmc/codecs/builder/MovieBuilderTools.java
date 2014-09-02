package us.ihmc.codecs.builder;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import com.google.code.libyuv.FilterModeEnum;

import us.ihmc.codecs.YUVPicture;

/* package-private */class MovieBuilderTools
{
   /* package-private */static BufferedImage to3ByteBGR(BufferedImage original)
   {
      if (original.getType() == BufferedImage.TYPE_3BYTE_BGR)
      {
         return original;
      }

      BufferedImage target = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
      Graphics2D g = target.createGraphics();
      g.drawImage(original, 0, 0, null);
      g.dispose();
      return target;
   }
   
   /* package-private */static YUVPicture toYUV(BufferedImage original, int width, int height)
   {
      BufferedImage bgr = to3ByteBGR(original);
      YUVPicture picture = new YUVPicture(bgr);
      
      if(picture.getWidth() == width && picture.getHeight() == height)
      {
         return picture;
      }
      else
      {
         return picture.scale(width, height, FilterModeEnum.kFilterBilinear);
      }
   }
}
