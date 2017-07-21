/*
 * Copyright 2017 Florida Institute for Human and Machine Cognition (IHMC)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package us.ihmc.codecs.builder;

import us.ihmc.codecs.generated.ELevelIdc;
import us.ihmc.codecs.generated.EProfileIdc;
import us.ihmc.codecs.generated.EUsageType;
import us.ihmc.codecs.generated.RC_MODES;

/**
 * Settings for the H264 encoder. 
 * 
 * Sane defaults are set
 * 
 * @author Jesper Smith
 *
 */
public class H264Settings
{
   private int intraPeriod = 100;
   private int bitrate = 10000;
   private EUsageType usageType = EUsageType.CAMERA_VIDEO_REAL_TIME;
   private EProfileIdc profileIdc = EProfileIdc.PRO_UNKNOWN;
   private ELevelIdc levelIdc = ELevelIdc.LEVEL_UNKNOWN;
   private RC_MODES rcMode = RC_MODES.RC_QUALITY_MODE;

   /**
    * 
    * @return bitrate in kbit/s
    */
   public int getBitrate()
   {
      return bitrate;
   }

   /**
    * 
    * @param bitrate in kbit/s
    */
   public void setBitrate(int bitrate)
   {
      this.bitrate = bitrate;
   }

   public EUsageType getUsageType()
   {
      return usageType;
   }

   public void setUsageType(EUsageType usageType)
   {
      this.usageType = usageType;
   }

   public EProfileIdc getProfileIdc()
   {
      return profileIdc;
   }

   public void setProfileIdc(EProfileIdc profileIdc)
   {
      this.profileIdc = profileIdc;
   }

   public ELevelIdc getLevelIdc()
   {
      return levelIdc;
   }

   public void setLevelIdc(ELevelIdc levelIdc)
   {
      this.levelIdc = levelIdc;
   }

   public int getIntraPeriod()
   {
      return intraPeriod;
   }

   public void setIntraPeriod(int intraPeriod)
   {
      this.intraPeriod = intraPeriod;
   }

   public RC_MODES getRcMode()
   {
      return rcMode;
   }

   public void setRcMode(RC_MODES rcMode)
   {
      this.rcMode = rcMode;
   }
   
   

}