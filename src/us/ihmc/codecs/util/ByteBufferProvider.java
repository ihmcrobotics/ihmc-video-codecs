package us.ihmc.codecs.util;

import java.nio.ByteBuffer;

public class ByteBufferProvider
{
   private ByteBuffer directBuffer;

   public ByteBuffer getOrCreateBuffer(int size)
   {
      if (directBuffer == null)
      {
         directBuffer = ByteBuffer.allocateDirect(size);
      }
      else if (directBuffer.capacity() < size)
      {
         directBuffer = ByteBuffer.allocateDirect(size);
      }

      directBuffer.clear();
      return directBuffer;
   }
   
   public ByteBuffer growByteBuffer()
   {
      if(directBuffer == null)
      {
         throw new RuntimeException("No buffer allocated");
      }
      
      int newCapacity = directBuffer.capacity() + (directBuffer.capacity() >> 1);
      ByteBuffer newBuffer = ByteBuffer.allocateDirect(newCapacity);
      directBuffer.flip();
      newBuffer.put(directBuffer);
      directBuffer = newBuffer;
      return directBuffer; 
      
   }
}
