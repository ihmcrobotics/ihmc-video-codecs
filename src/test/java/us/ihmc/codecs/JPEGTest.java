package us.ihmc.codecs;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Random;

import org.junit.jupiter.api.Test;

import us.ihmc.codecs.generated.FilterModeEnum;
import us.ihmc.codecs.generated.YUVPicture;
import us.ihmc.codecs.yuv.JPEGDecoder;
import us.ihmc.codecs.yuv.JPEGEncoder;

public class JPEGTest
{
   @Test
   public void testEncodeDecode() throws IOException
   {
      JPEGDecoder decoder = new JPEGDecoder();
      JPEGEncoder encoder = new JPEGEncoder();

      Random random = new Random(9872412L);
      for (int i = 0; i < 1000; i++)
      {
         YUVPicture jpeg = decoder.readJPEG(JPEGExample.class.getClassLoader().getResource("testImage.jpg"));
         int w = random.nextInt(3000);
         int h = random.nextInt(3000);
         jpeg.scale(w + 1, h + 1, FilterModeEnum.kFilterLinear);
         ByteBuffer buffer = encoder.encode(jpeg, 90);

         YUVPicture res = decoder.decode(buffer);
         assertEquals(jpeg.getWidth(), res.getWidth());
         assertEquals(jpeg.getHeight(), res.getHeight());

         jpeg.delete();
         res.delete();
      }
   }
}
