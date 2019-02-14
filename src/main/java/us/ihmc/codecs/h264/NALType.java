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

package us.ihmc.codecs.h264;

import java.nio.ByteBuffer;

public enum NALType
{
   // Taken from http://www.szatmary.org/blog/25
   UNSPECIFIED, CODED_SLICE_NON_IDR_PICTURE, CODED_SLICE_DATA_PARTITION_A, CODED_SLICE_DATA_PARTITION_B, CODED_SLICE_DATA_PARTITION_C, CODED_SLICE_IDR_PICTURE, SEI, SPS, PPS, ACCESS_UNIT_DELIMITER, END_OF_SEQUENCE, END_OF_STREAM, FILLER_DATA, SEQUENCE_PARAMETER_SET_EXTENSION, PREFIX_NAL_UNIT, SUBSET_SEQUENCE_PARAMETER_SET, RESERVED, CODED_SLICE_OF_AN_AUXILLIARY_CODEC_PICTURE_WITHOUT_PARTITIONING, CODED_SLICE_EXTENSION, CODED_SLICE_EXTENSION_FOR_DEPTH_VIEW_COMPONENTS;

   public static NALType fromBitStream(ByteBuffer stream)
   {
      int nalUnit;
      if (stream.get(3) == 1)
      {
         nalUnit = stream.get(4) & 0x1f;
      }
      else
      {
         nalUnit = stream.get(5) & 0x1f;
      }

      switch (nalUnit)
      {
      case 0:
         return UNSPECIFIED;
      case 1:
         return CODED_SLICE_NON_IDR_PICTURE;
      case 2:
         return CODED_SLICE_DATA_PARTITION_A;
      case 3:
         return CODED_SLICE_DATA_PARTITION_B;
      case 4:
         return CODED_SLICE_DATA_PARTITION_C;
      case 5:
         return CODED_SLICE_IDR_PICTURE;
      case 6:
         return SEI;
      case 7:
         return SPS;
      case 8:
         return PPS;
      case 9:
         return ACCESS_UNIT_DELIMITER;
      case 10:
         return END_OF_SEQUENCE;
      case 11:
         return END_OF_STREAM;
      case 12:
         return FILLER_DATA;
      case 13:
         return SEQUENCE_PARAMETER_SET_EXTENSION;
      case 14:
         return PREFIX_NAL_UNIT;
      case 15:
         return SUBSET_SEQUENCE_PARAMETER_SET;
      case 16:
      case 17:
      case 18:
         return RESERVED;
      case 19:
         return CODED_SLICE_OF_AN_AUXILLIARY_CODEC_PICTURE_WITHOUT_PARTITIONING;
      case 20:
         return CODED_SLICE_EXTENSION;
      case 21:
         return CODED_SLICE_EXTENSION_FOR_DEPTH_VIEW_COMPONENTS;
      case 22:
      case 23:
         return RESERVED;
      case 24:
      case 25:
      case 26:
      case 27:
      case 28:
      case 29:
      case 30:
      case 31:
         return UNSPECIFIED;
      default:
         throw new RuntimeException("Unknown NAL unit type: " + nalUnit);
      }
   }
}
