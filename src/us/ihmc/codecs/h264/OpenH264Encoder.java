package us.ihmc.codecs.h264;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import org.openh264.EVideoFormatType;
import org.openh264.ISVCDecoder;
import org.openh264.ISVCEncoder;
import org.openh264.RC_MODES;
import org.openh264.SDecodingParam;
import org.openh264.SEncParamBase;
import org.openh264.SFrameBSInfo;
import org.openh264.SLayerBSInfo;
import org.openh264.SSourcePicture;
import org.openh264.codec_api;

import us.ihmc.codecs.colorSpace.YCbCr420;

/**
 * Created by jesper on 8/19/14.
 */
public class OpenH264Encoder implements H264Encoder {

    private final ISVCEncoder isvcEncoder;
    private final ISVCDecoder isvcDecoder;

    public OpenH264Encoder(int height, int width) throws IOException
    {
        System.loadLibrary("codec_api");
        isvcEncoder = codec_api.WelsCreateSVCEncoder();
        SEncParamBase paramBase = new SEncParamBase();
        paramBase.setFMaxFrameRate(30);
        paramBase.setIRCMode(RC_MODES.RC_QUALITY_MODE);
        paramBase.setIPicHeight(height);
        paramBase.setIPicWidth(width);
        paramBase.setITargetBitrate(500000);
        isvcEncoder.Initialize(paramBase);
        
        isvcDecoder = codec_api.WelsCreateDecoder();
        SDecodingParam pParam = new SDecodingParam();
        isvcDecoder.Initialize(pParam);

    }


    @Override
    public void encodeFrame(BufferedImage frame, NALProcessor nalProcessor) throws IOException
    {
        SSourcePicture picture = new SSourcePicture();

        ByteBuffer Yb = ByteBuffer.allocateDirect(frame.getWidth() * frame.getHeight());
        ByteBuffer CBb = ByteBuffer.allocateDirect(Yb.capacity() >> 2);
        ByteBuffer CRb = ByteBuffer.allocateDirect(Yb.capacity() >> 2);

        // This should not be required
        Yb.order(ByteOrder.nativeOrder());
        CBb.order(ByteOrder.nativeOrder());
        CRb.order(ByteOrder.nativeOrder());


        YCbCr420.convert(frame, Yb, CBb, CRb);

        picture.setIPicWidth(frame.getWidth());
        picture.setIPicHeight(frame.getHeight());
        picture.setIColorFormat(EVideoFormatType.videoFormatI420.swigValue());

        int YStride = frame.getWidth();
        int CbCrStride = YStride >> 1;
        int[] stride = { YStride, CbCrStride, CbCrStride, 0 };
        picture.setIStride(stride);
        picture.setPData(0, Yb);
        picture.setPData(1, CBb);
        picture.setPData(2, CRb);

        SFrameBSInfo info = new SFrameBSInfo();
        int e = isvcEncoder.EncodeFrame(picture, info);
        if(e != 0)
        {
           throw new IOException("Cannot encode frame: " + e);
        }
        
        ByteBuffer buf = ByteBuffer.allocateDirect(info.getBufferSize());
        info.getBuffer(buf);
        
        for(int i = 0; i < info.getILayerNum(); i++)
        {
           SLayerBSInfo sLayerInfo = info.getSLayerInfo(i);
           int[] nalLengthInByte = sLayerInfo.getPNalLengthInByte();
           
           for(int n = 0; n < sLayerInfo.getINalCount(); n++)
           {
              ByteBuffer nalBuffer = ByteBuffer.allocateDirect(nalLengthInByte[n]);
              sLayerInfo.getNAL(n, nalBuffer);
              nalProcessor.processNAL(nalBuffer);
           }
        }
        
        picture.delete();
        info.delete();
    }


    public void dispose()
    {
        isvcEncoder.delete();
    }

}
