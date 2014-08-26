package us.ihmc.codecs;

import java.awt.image.BufferedImage;
import java.io.IOException;

import us.ihmc.codecs.h264.NALProcessor;

/**
 * Common base interface for video codecs
 *
 * Created by jesper on 8/19/14.
 */
public interface VideoEncoder
{
    public void encodeFrame(BufferedImage image, NALProcessor nalProcessor) throws IOException;
}
