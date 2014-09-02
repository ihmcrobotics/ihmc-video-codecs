package com.googlecode.mp4parser.authoring.builder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import us.ihmc.codecs.h264.NALProcessor;
import us.ihmc.codecs.h264.NALType;

import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.CompositionTimeToSample;
import com.coremedia.iso.boxes.SampleDependencyTypeBox;
import com.coremedia.iso.boxes.SampleDescriptionBox;
import com.coremedia.iso.boxes.VideoMediaHeaderBox;
import com.coremedia.iso.boxes.h264.AvcConfigurationBox;
import com.coremedia.iso.boxes.sampleentry.VisualSampleEntry;
import com.googlecode.mp4parser.authoring.AbstractTrack;
import com.googlecode.mp4parser.authoring.Sample;
import com.googlecode.mp4parser.authoring.TrackMetaData;
import com.googlecode.mp4parser.authoring.tracks.CleanInputStream;
import com.googlecode.mp4parser.authoring.tracks.H264TrackImpl.SliceHeader;
import com.googlecode.mp4parser.h264.model.PictureParameterSet;
import com.googlecode.mp4parser.h264.model.SeqParameterSet;
import com.googlecode.mp4parser.h264.read.CAVLCReader;

public class UnbufferedH264TrackImpl extends AbstractTrack implements NALProcessor
{

   private final UnbufferedDefaultMP4Builder builder;

   private int width;
   private int height;
   private long timescale;
   private int frametick;

   TrackMetaData trackMetaData = new TrackMetaData();
   SampleDescriptionBox sampleDescriptionBox;

   List<CompositionTimeToSample.Entry> ctts;
   List<SampleDependencyTypeBox.Entry> sdtp;
   List<Integer> stss;

   SeqParameterSet seqParameterSet = null;
   PictureParameterSet pictureParameterSet = null;
   LinkedList<byte[]> seqParameterSetList = new LinkedList<byte[]>();
   LinkedList<byte[]> pictureParameterSetList = new LinkedList<byte[]>();

   private SEIMessage seiMessage;
   int frameNrInGop = 0;
   private String lang = "eng";
   private long[] decodingTimes;
   

   private final ArrayList<ByteBuffer> samples = new ArrayList<ByteBuffer>();
   private int frameNr = 0;

   public UnbufferedH264TrackImpl(UnbufferedDefaultMP4Builder builder, String lang, long timescale, int frametick)
   {
      this.builder = builder;
      builder.addTrack(this);

      this.lang = lang;
      this.timescale = timescale; //e.g. 23976
      this.frametick = frametick;

      ctts = new LinkedList<CompositionTimeToSample.Entry>();
      sdtp = new LinkedList<SampleDependencyTypeBox.Entry>();
      stss = new LinkedList<Integer>();

   }

   @Override
   public SampleDescriptionBox getSampleDescriptionBox()
   {
      return sampleDescriptionBox;
   }

   @Override
   public long[] getSampleDurations()
   {
      return decodingTimes;
   }

   @Override
   public TrackMetaData getTrackMetaData()
   {
      return trackMetaData;
   }

   @Override
   public String getHandler()
   {
      return "vide";
   }

   @Override
   public List<Sample> getSamples()
   {
      throw new RuntimeException("Samples are already written to file");
   }

   @Override
   public Box getMediaHeaderBox()
   {
      return new VideoMediaHeaderBox();
   }

   @Override
   public void close() throws IOException
   {
      if (!readVariables())
      {
         throw new IOException();
      }
      
      decodingTimes = new long[frameNr];
      Arrays.fill(decodingTimes, frametick);
      
      sampleDescriptionBox = new SampleDescriptionBox();
      VisualSampleEntry visualSampleEntry = new VisualSampleEntry("avc1");
      visualSampleEntry.setDataReferenceIndex(1);
      visualSampleEntry.setDepth(24);
      visualSampleEntry.setFrameCount(1);
      visualSampleEntry.setHorizresolution(72);
      visualSampleEntry.setVertresolution(72);
      visualSampleEntry.setWidth(width);
      visualSampleEntry.setHeight(height);
      visualSampleEntry.setCompressorname("AVC Coding");

      AvcConfigurationBox avcConfigurationBox = new AvcConfigurationBox();

      avcConfigurationBox.setSequenceParameterSets(seqParameterSetList);
      avcConfigurationBox.setPictureParameterSets(pictureParameterSetList);
      avcConfigurationBox.setAvcLevelIndication(seqParameterSet.level_idc);
      avcConfigurationBox.setAvcProfileIndication(seqParameterSet.profile_idc);
      avcConfigurationBox.setBitDepthLumaMinus8(seqParameterSet.bit_depth_luma_minus8);
      avcConfigurationBox.setBitDepthChromaMinus8(seqParameterSet.bit_depth_chroma_minus8);
      avcConfigurationBox.setChromaFormat(seqParameterSet.chroma_format_idc.getId());
      avcConfigurationBox.setConfigurationVersion(1);
      avcConfigurationBox.setLengthSizeMinusOne(3);
      avcConfigurationBox.setProfileCompatibility(seqParameterSetList.get(0)[1]);

      visualSampleEntry.addBox(avcConfigurationBox);
      sampleDescriptionBox.addBox(visualSampleEntry);

      trackMetaData.setCreationTime(new Date());
      trackMetaData.setModificationTime(new Date());
      trackMetaData.setLanguage(lang);
      trackMetaData.setTimescale(timescale);
      trackMetaData.setWidth(width);
      trackMetaData.setHeight(height);
      
      

   }


   private void write(NALType type, ArrayList<ByteBuffer> samples) throws IOException
   {
      int stdpValue = 22;
      frameNr++;
      boolean IdrPicFlag = false;
      if (type == NALType.CODED_SLICE_IDR_PICTURE)
      {
         stdpValue += 16;
         IdrPicFlag = true;
      }
      // cleans the buffer we just added
      InputStream bs = cleanBuffer(new ByteBufferBackedInputStream(samples.get(samples.size() - 1)));
      SliceHeader sh = new SliceHeader(bs, seqParameterSet, pictureParameterSet, IdrPicFlag);
      if (sh.slice_type == SliceHeader.SliceType.B)
      {
         stdpValue += 4;
      }
      //      LOG.fine("Adding sample with size " + bb.capacity() + " and header " + sh);
      if (type == NALType.CODED_SLICE_IDR_PICTURE)
      { // IDR Picture
         stss.add(frameNr);
      }
      if (seiMessage == null || seiMessage.n_frames == 0)
      {
         frameNrInGop = 0;
      }
      int offset = 0;
      if (seiMessage != null && seiMessage.clock_timestamp_flag)
      {
         offset = seiMessage.n_frames - frameNrInGop;
      }
      else if (seiMessage != null && seiMessage.removal_delay_flag)
      {
         offset = seiMessage.dpb_removal_delay / 2;
      }
      ctts.add(new CompositionTimeToSample.Entry(1, offset * frametick));
      sdtp.add(new SampleDependencyTypeBox.Entry(stdpValue));
      frameNrInGop++;

      builder.writeSample(this, samples.toArray(new ByteBuffer[samples.size()]));

   }

   private void prependLength(ByteBuffer nal)
   {
      nal.putInt(0, nal.remaining() - 4);
   }
   
   @Override
   public void processNal(NALType type, ByteBuffer nal) throws IOException
   {
      switch (type)
      {
      case CODED_SLICE_NON_IDR_PICTURE:
      case CODED_SLICE_DATA_PARTITION_A:
      case CODED_SLICE_DATA_PARTITION_B:
      case CODED_SLICE_DATA_PARTITION_C:
      case CODED_SLICE_IDR_PICTURE:
         prependLength(nal);
         samples.add(nal);
         write(type, samples);
         samples.clear();
         break;
      case SEI:
         seiMessage = new SEIMessage(cleanBuffer(new ByteBufferBackedInputStream(nal)), seqParameterSet);
      case ACCESS_UNIT_DELIMITER: // Fall trough, SEI should also be added to samples
         prependLength(nal);
         samples.add(nal);
         break;
      case SPS:
         if (seqParameterSet == null)
         {
            InputStream is = cleanBuffer(new ByteBufferBackedInputStream(nal));
            is.read();
            is.read();
            is.read();
            is.read();
            is.read();
            seqParameterSet = SeqParameterSet.read(is);
            // make a copy
            seqParameterSetList.add(toArray(nal));
         }
         break;
      case PPS:
         if (pictureParameterSet == null)
         {
            InputStream is = new ByteBufferBackedInputStream(nal);
            is.read();
            is.read();
            is.read();
            is.read();
            is.read();
            pictureParameterSet = PictureParameterSet.read(is);
            pictureParameterSetList.add(toArray(nal));
         }

         break;
      default:
         System.err.println("Unexpected NAL type: " + type);
         break;
      }
   }

   static byte[] toArray(ByteBuffer buf)
   {
      buf = buf.duplicate();
      byte[] b = new byte[buf.remaining()];
      buf.get(b, 0, b.length);
      return b;
   }

   protected InputStream cleanBuffer(InputStream is)
   {
      return new CleanInputStream(is);
   }

   private boolean readVariables()
   {
      width = (seqParameterSet.pic_width_in_mbs_minus1 + 1) * 16;
      int mult = 2;
      if (seqParameterSet.frame_mbs_only_flag)
      {
         mult = 1;
      }
      height = 16 * (seqParameterSet.pic_height_in_map_units_minus1 + 1) * mult;
      if (seqParameterSet.frame_cropping_flag)
      {
         int chromaArrayType = 0;
         if (!seqParameterSet.residual_color_transform_flag)
         {
            chromaArrayType = seqParameterSet.chroma_format_idc.getId();
         }
         int cropUnitX = 1;
         int cropUnitY = mult;
         if (chromaArrayType != 0)
         {
            cropUnitX = seqParameterSet.chroma_format_idc.getSubWidth();
            cropUnitY = seqParameterSet.chroma_format_idc.getSubHeight() * mult;
         }

         width -= cropUnitX * (seqParameterSet.frame_crop_left_offset + seqParameterSet.frame_crop_right_offset);
         height -= cropUnitY * (seqParameterSet.frame_crop_top_offset + seqParameterSet.frame_crop_bottom_offset);
      }
      return true;
   }

   public class SEIMessage
   {

      int payloadType = 0;
      int payloadSize = 0;

      boolean removal_delay_flag;
      int cpb_removal_delay;
      int dpb_removal_delay;

      boolean clock_timestamp_flag;
      int pic_struct;
      int ct_type;
      int nuit_field_based_flag;
      int counting_type;
      int full_timestamp_flag;
      int discontinuity_flag;
      int cnt_dropped_flag;
      int n_frames;
      int seconds_value;
      int minutes_value;
      int hours_value;
      int time_offset_length;
      int time_offset;

      SeqParameterSet sps;

      public SEIMessage(InputStream is, SeqParameterSet sps) throws IOException
      {
         this.sps = sps;
         is.read();
         int datasize = is.available();
         int read = 0;
         while (read < datasize)
         {
            payloadType = 0;
            payloadSize = 0;
            int last_payload_type_bytes = is.read();
            read++;
            while (last_payload_type_bytes == 0xff)
            {
               payloadType += last_payload_type_bytes;
               last_payload_type_bytes = is.read();
               read++;
            }
            payloadType += last_payload_type_bytes;
            int last_payload_size_bytes = is.read();
            read++;

            while (last_payload_size_bytes == 0xff)
            {
               payloadSize += last_payload_size_bytes;
               last_payload_size_bytes = is.read();
               read++;
            }
            payloadSize += last_payload_size_bytes;
            if (datasize - read >= payloadSize)
            {
               if (payloadType == 1)
               { // pic_timing is what we are interested in!
                  if (sps.vuiParams != null
                        && (sps.vuiParams.nalHRDParams != null || sps.vuiParams.vclHRDParams != null || sps.vuiParams.pic_struct_present_flag))
                  {
                     byte[] data = new byte[payloadSize];
                     is.read(data);
                     read += payloadSize;
                     CAVLCReader reader = new CAVLCReader(new ByteArrayInputStream(data));
                     if (sps.vuiParams.nalHRDParams != null || sps.vuiParams.vclHRDParams != null)
                     {
                        removal_delay_flag = true;
                        cpb_removal_delay = reader.readU(sps.vuiParams.nalHRDParams.cpb_removal_delay_length_minus1 + 1, "SEI: cpb_removal_delay");
                        dpb_removal_delay = reader.readU(sps.vuiParams.nalHRDParams.dpb_output_delay_length_minus1 + 1, "SEI: dpb_removal_delay");
                     }
                     else
                     {
                        removal_delay_flag = false;
                     }
                     if (sps.vuiParams.pic_struct_present_flag)
                     {
                        pic_struct = reader.readU(4, "SEI: pic_struct");
                        int numClockTS;
                        switch (pic_struct)
                        {
                        case 0:
                        case 1:
                        case 2:
                        default:
                           numClockTS = 1;
                           break;

                        case 3:
                        case 4:
                        case 7:
                           numClockTS = 2;
                           break;

                        case 5:
                        case 6:
                        case 8:
                           numClockTS = 3;
                           break;
                        }
                        for (int i = 0; i < numClockTS; i++)
                        {
                           clock_timestamp_flag = reader.readBool("pic_timing SEI: clock_timestamp_flag[" + i + "]");
                           if (clock_timestamp_flag)
                           {
                              ct_type = reader.readU(2, "pic_timing SEI: ct_type");
                              nuit_field_based_flag = reader.readU(1, "pic_timing SEI: nuit_field_based_flag");
                              counting_type = reader.readU(5, "pic_timing SEI: counting_type");
                              full_timestamp_flag = reader.readU(1, "pic_timing SEI: full_timestamp_flag");
                              discontinuity_flag = reader.readU(1, "pic_timing SEI: discontinuity_flag");
                              cnt_dropped_flag = reader.readU(1, "pic_timing SEI: cnt_dropped_flag");
                              n_frames = reader.readU(8, "pic_timing SEI: n_frames");
                              if (full_timestamp_flag == 1)
                              {
                                 seconds_value = reader.readU(6, "pic_timing SEI: seconds_value");
                                 minutes_value = reader.readU(6, "pic_timing SEI: minutes_value");
                                 hours_value = reader.readU(5, "pic_timing SEI: hours_value");
                              }
                              else
                              {
                                 if (reader.readBool("pic_timing SEI: seconds_flag"))
                                 {
                                    seconds_value = reader.readU(6, "pic_timing SEI: seconds_value");
                                    if (reader.readBool("pic_timing SEI: minutes_flag"))
                                    {
                                       minutes_value = reader.readU(6, "pic_timing SEI: minutes_value");
                                       if (reader.readBool("pic_timing SEI: hours_flag"))
                                       {
                                          hours_value = reader.readU(5, "pic_timing SEI: hours_value");
                                       }
                                    }
                                 }
                              }
                              if (true)
                              {
                                 if (sps.vuiParams.nalHRDParams != null)
                                 {
                                    time_offset_length = sps.vuiParams.nalHRDParams.time_offset_length;
                                 }
                                 else if (sps.vuiParams.vclHRDParams != null)
                                 {
                                    time_offset_length = sps.vuiParams.vclHRDParams.time_offset_length;
                                 }
                                 else
                                 {
                                    time_offset_length = 24;
                                 }
                                 time_offset = reader.readU(24, "pic_timing SEI: time_offset");
                              }
                           }
                        }
                     }

                  }
                  else
                  {
                     for (int i = 0; i < payloadSize; i++)
                     {
                        is.read();
                        read++;
                     }
                  }
               }
               else
               {
                  for (int i = 0; i < payloadSize; i++)
                  {
                     is.read();
                     read++;
                  }
               }
            }
            else
            {
               read = datasize;
            }
         }
      }
   }

   public class ByteBufferBackedInputStream extends InputStream
   {

      private final ByteBuffer buf;

      public ByteBufferBackedInputStream(ByteBuffer buf)
      {
         // make a coy of the buffer
         this.buf = buf.duplicate();
      }

      public int read() throws IOException
      {
         if (!buf.hasRemaining())
         {
            return -1;
         }
         return buf.get() & 0xFF;
      }

      public int read(byte[] bytes, int off, int len) throws IOException
      {
         if (!buf.hasRemaining())
         {
            return -1;
         }

         len = Math.min(len, buf.remaining());
         buf.get(bytes, off, len);
         return len;
      }
   }

}
