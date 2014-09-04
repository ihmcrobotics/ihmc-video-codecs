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

package us.ihmc.codecs.builder;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import us.ihmc.codecs.yuv.YUV420Picture;

import com.google.code.libyuv.FilterModeEnum;

/* package-private */class MovieBuilderTools
{
   private MovieBuilderTools()
   {
      // Disallow construction
   }
   
   /* package-private */static BufferedImage to3ByteBGR(BufferedImage original)
   {
      if (original.getType() == BufferedImage.TYPE_3BYTE_BGR)
      {
         return original;
      }

      BufferedImage target = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
      Graphics2D g = target.createGraphics();
      g.drawImage(original, 0, 0, null);
      g.dispose();
      return target;
   }
   
   /* package-private */static YUV420Picture toYUV(BufferedImage original, int width, int height)
   {
      BufferedImage bgr = to3ByteBGR(original);
      YUV420Picture picture = new YUV420Picture(bgr);
      
      if(picture.getWidth() == width && picture.getHeight() == height)
      {
         return picture;
      }
      else
      {
         return picture.scale(width, height, FilterModeEnum.kFilterBilinear);
      }
   }
}
