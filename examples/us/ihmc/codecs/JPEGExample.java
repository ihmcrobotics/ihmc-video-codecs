package us.ihmc.codecs;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import us.ihmc.codecs.generated.FilterModeEnum;
import us.ihmc.codecs.generated.YUVPicture;
import us.ihmc.codecs.yuv.JPEGDecoder;
import us.ihmc.codecs.yuv.JPEGEncoder;

public class JPEGExample
{
   public static void main(String[] args) throws IOException
   {
      JPEGDecoder decoder = new JPEGDecoder();
      YUVPicture jpeg = decoder.readJPEG(JPEGExample.class.getClassLoader().getResource("testImage.jpg"));
      jpeg.scale(jpeg.getWidth() * 2, jpeg.getHeight() * 2, FilterModeEnum.kFilterBilinear);
      JPEGEncoder encoder = new JPEGEncoder();
      int maxSize = (int) encoder.maxSize(jpeg);
      ByteBuffer buffer = ByteBuffer.allocateDirect((int) maxSize);
      int size = encoder.encode(jpeg, buffer, maxSize, 90);
      buffer.limit(size);

      RandomAccessFile file = new RandomAccessFile("test.jpg", "rw");
      FileChannel channel = file.getChannel();
      channel.write(buffer);
      file.close();

   }
}
