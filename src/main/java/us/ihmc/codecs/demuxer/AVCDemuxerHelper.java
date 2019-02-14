/*
 *   Copyright 2014 Florida Institute for Human and Machine Cognition (IHMC)
 *    
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *    
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *    
 *    Written by Jesper Smith with assistance from IHMC team members
 */

package us.ihmc.codecs.demuxer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.jcodec.codecs.h264.mp4.AvcCBox;
import org.jcodec.common.model.Packet;
import org.jcodec.containers.mp4.MP4Packet;
import org.jcodec.containers.mp4.boxes.Box;
import org.jcodec.containers.mp4.boxes.LeafBox;
import org.jcodec.containers.mp4.boxes.SampleEntry;
import org.jcodec.containers.mp4.boxes.VideoSampleEntry;

import us.ihmc.codecs.generated.YUVPicture;
import us.ihmc.codecs.h264.OpenH264Decoder;

public class AVCDemuxerHelper implements DemuxerHelper
{
   public static final byte[] NAL_HEADER = { 0, 0, 0, 1 };
   public static final byte[] SPS_HEADER = { 0, 0, 0, 1, 0x67 };
   public static final byte[] PPS_HEADER = { 0, 0, 0, 1, 0x68 };

   private OpenH264Decoder decoder;
   private final SampleEntry[] sampleEntries;
   private int curENo = -1;
   private AvcCBox avcCBox;

   /* package-private */public AVCDemuxerHelper(SampleEntry[] sampleEntries) throws IOException
   {
      this.sampleEntries = sampleEntries;
   }

   /* package-private */public YUVPicture getFrame(Packet frame) throws IOException
   {
      updateState(frame);

      List<ByteBuffer> buffers = splitMOVPacket(frame.getData(), avcCBox);
      YUVPicture picture = null;
      for (int i = 0; i < buffers.size(); i++)
      {
         YUVPicture decode = decoder.decodeFrame(buffers.get(i));
         if (decode != null)
         {
            picture = decode;
         }
      }
      return picture;

   }

   private void updateState(Packet packet) throws IOException
   {
      int eNo = ((MP4Packet) packet).getEntryNo();
      if (eNo != curENo)
      {
         curENo = eNo;
         avcCBox = parseAVCC((VideoSampleEntry) sampleEntries[curENo]);

         if (decoder != null)
         {
            decoder.delete();
         }
         decoder = new OpenH264Decoder();
         for (ByteBuffer sps : avcCBox.getSpsList())
         {
            decoder.decodeFrame(toDirectByteBuffer(sps, SPS_HEADER));
         }
         for (ByteBuffer pps : avcCBox.getPpsList())
         {
            decoder.decodeFrame(toDirectByteBuffer(pps, PPS_HEADER));
         }
      }
   }

   public static ByteBuffer toDirectByteBuffer(ByteBuffer src, byte[] lead)
   {
      ByteBuffer direct = ByteBuffer.allocateDirect(lead.length + src.remaining());
      direct.put(lead);
      direct.put(src);
      return direct;
   }

   public static AvcCBox parseAVCC(VideoSampleEntry vse)
   {
      Box lb = Box.findFirst(vse, Box.class, "avcC");
      if (lb instanceof AvcCBox)
         return (AvcCBox) lb;
      else
      {
         AvcCBox avcC = new AvcCBox();
         avcC.parse(((LeafBox) lb).getData().duplicate());
         return avcC;
      }
   }

   public static List<ByteBuffer> splitMOVPacket(ByteBuffer buf, AvcCBox avcC)
   {
      List<ByteBuffer> result = new ArrayList<ByteBuffer>();
      int nls = avcC.getNalLengthSize();

      ByteBuffer dup = buf.duplicate();
      if(buf.isDirect())
      {
         while(dup.remaining() >= nls)
         {
            ByteBuffer slice = buf.slice();
            int len = readLen(dup, nls);
            if(len == 0)
            {
               break;
            }
            slice.limit(len + 4);
            slice.putInt(0, 1);
            result.add(slice);
            
            dup.position(dup.position() + len);
         }
      }
      else
      {
         while (dup.remaining() >= nls)
         {
            int len = readLen(dup, nls);
            if (len == 0)
               break;
            ByteBuffer direct = ByteBuffer.allocateDirect(len + 4);
            direct.put(NAL_HEADER);
            for (int i = 0; i < len; i++)
            {
               direct.put(dup.get());
            }
            direct.flip();
            result.add(direct);
         }
      }
      return result;
   }

   private static int readLen(ByteBuffer dup, int nls)
   {
      switch (nls)
      {
      case 1:
         return dup.get() & 0xff;
      case 2:
         return dup.getShort() & 0xffff;
      case 3:
         return ((dup.getShort() & 0xffff) << 8) | (dup.get() & 0xff);
      case 4:
         return dup.getInt();
      default:
         throw new IllegalArgumentException("NAL Unit length size can not be " + nls);
      }
   }

   @Override
   public synchronized void delete()
   {
      if(decoder != null)
      {
         decoder.delete();
         decoder = null;
      }
   }

   @Override
   public void skipFrame(Packet frame) throws IOException
   {
      updateState(frame);

      List<ByteBuffer> buffers = splitMOVPacket(frame.getData(), avcCBox);
      for (int i = 0; i < buffers.size(); i++)
      {
         decoder.skipFrame(buffers.get(i));
      }
   }

   @Override
   public AvcCBox getAvcCBox()
   {
      return avcCBox;
   }

}
