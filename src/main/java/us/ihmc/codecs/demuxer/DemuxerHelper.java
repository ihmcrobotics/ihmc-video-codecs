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

import org.jcodec.codecs.h264.mp4.AvcCBox;
import org.jcodec.common.model.Packet;

import us.ihmc.codecs.generated.YUVPicture;

public interface DemuxerHelper
{
   /* package-private */ YUVPicture getFrame(Packet frame) throws IOException;
   /* package-private */ void skipFrame(Packet frame) throws IOException;
   /* package-private */ void delete();
   AvcCBox getAvcCBox();
}
