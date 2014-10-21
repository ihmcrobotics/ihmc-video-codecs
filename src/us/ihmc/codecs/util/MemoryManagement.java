package us.ihmc.codecs.util;

import java.nio.ByteBuffer;

import sun.misc.Cleaner;
import sun.nio.ch.DirectBuffer;

public class MemoryManagement
{

   public static void deallocateNativeByteBuffer(ByteBuffer buffer)
   {
      if(!buffer.isDirect()) return;
      Cleaner cleaner = ((DirectBuffer) buffer).cleaner();
      cleaner.clean();
   }

}
