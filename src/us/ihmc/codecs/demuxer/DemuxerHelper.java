package us.ihmc.codecs.demuxer;

import java.io.IOException;

import org.jcodec.common.model.Packet;

import us.ihmc.codecs.YUVPicture;

public interface DemuxerHelper
{
   /* package-private */ public YUVPicture getFrame(Packet frame) throws IOException;
}
