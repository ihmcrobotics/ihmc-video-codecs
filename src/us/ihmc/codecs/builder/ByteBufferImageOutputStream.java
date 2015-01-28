package us.ihmc.codecs.builder;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.stream.ImageOutputStreamImpl;

public class ByteBufferImageOutputStream extends ImageOutputStreamImpl
{
   private final ByteBuffer sink;

   public ByteBufferImageOutputStream(ByteBuffer sink)
   {
      this.sink = sink;
   }

   @Override
   public void write(int b) throws IOException
   {
      sink.put((byte) b);
   }

   @Override
   public void write(byte[] b, int off, int len) throws IOException
   {
      sink.put(b, off, len);
   }

   @Override
   public int read() throws IOException
   {
      return sink.get();
   }

   @Override
   public int read(byte[] b, int off, int len) throws IOException
   {
      int pos = sink.position();
      sink.get(b, off, len);
      return sink.position() - pos;
   }

}
