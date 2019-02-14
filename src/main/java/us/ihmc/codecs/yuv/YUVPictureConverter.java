package us.ihmc.codecs.yuv;

import java.awt.image.BufferedImage;

import us.ihmc.codecs.generated.RGBPicture;
import us.ihmc.codecs.generated.YUVPicture;
import us.ihmc.codecs.generated.YUVPicture.YUVSubsamplingType;

public class YUVPictureConverter
{
   private final RGBPictureConverter rgbPictureConverter = new RGBPictureConverter();

   /**
    * Convert YUVPicture to BufferedImage, minimizing object allocation
    * 
    * @param picture YUVPicture to convert
    * @return new BufferedImage.
    */
   public BufferedImage toBufferedImage(YUVPicture picture)
   {
      return toBufferedImage(picture, null);
   }

   /**
    * Convert YUVPicture to BufferedImage, minimizing object allocation
    * 
    * @param picture YUVPicture to convert
    * @param imageToPack Image to output to. If picture.size() != imageToPack.size() then a new BufferedImage is allocated
    * @return imageToPack if sizes match, new BufferedImage otherwise.
    */
   public BufferedImage toBufferedImage(YUVPicture picture, BufferedImage imageToPack)
   {
      RGBPicture rgb = picture.toRGB();
      BufferedImage img = rgbPictureConverter.toBufferedImage(rgb, imageToPack);
      rgb.delete();
      return img;
   }

   
   public YUVPicture fromBufferedImage(BufferedImage source, YUVSubsamplingType samplingType)
   {
      RGBPicture rgb = rgbPictureConverter.fromBufferedImage(source);
      YUVPicture target = rgb.toYUV(samplingType);
      rgb.delete();
      return target;
   }
}
