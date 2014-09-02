package us.ihmc.codecs.builder;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Simple to use interface for creating movies
 * 
 * @author Jesper Smith
 *
 */
public interface MovieBuilder
{
   /**
    * Encode a single image. Automatically gets resized to the movie width/height.
    * 
    * @param frame BufferedImage with frame data.
    * @throws IOException 
    */
   public void encodeFrame(BufferedImage frame) throws IOException;
   
   /**
    * Close the stream and write headers
    * @throws IOException 
    */
   public void close() throws IOException;
   
   /**
    * 
    * @return Width of the movie
    */
   public int getWidth();
   
   /**
    * 
    * @return Height of the movie
    */
   public int getHeight();
   
}
