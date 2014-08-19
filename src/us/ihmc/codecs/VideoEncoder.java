package us.ihmc.codecs;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

/**
 * Common base interface for video codecs
 *
 * Created by jesper on 8/19/14.
 */
public interface VideoEncoder
{
    public ByteBuffer encodeFrame(BufferedImage image);
}
