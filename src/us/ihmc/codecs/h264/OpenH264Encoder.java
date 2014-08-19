package us.ihmc.codecs.h264;

import org.openh264.*;
import us.ihmc.codecs.colorSpace.YCbCr420;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * Created by jesper on 8/19/14.
 */
public class OpenH264Encoder implements H264Encoder {

    private final ISVCEncoder isvcEncoder;

    public OpenH264Encoder(int height, int width) throws IOException
    {
        System.out.println(System.getProperty("java.library.path"));
        System.loadLibrary("codec_api");
        isvcEncoder = codec_api.WelsCreateSVCEncoder();
        SEncParamBase paramBase = new SEncParamBase();
        paramBase.setFMaxFrameRate(30);
        paramBase.setIRCMode(RC_MODES.RC_QUALITY_MODE);
        paramBase.setIPicHeight(height);
        paramBase.setIPicWidth(width);
        paramBase.setITargetBitrate(500000);
        isvcEncoder.Initialize(paramBase);

    }


    public ByteBuffer encodeFrame(BufferedImage frame)
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
        picture.setPData(0, CBb);
        picture.setPData(0, CRb);

        System.out.println(Arrays.toString(picture.getIStride()));
//        isvcEncoder.EncodeFrame()
        SFrameBSInfo info = new SFrameBSInfo();
        isvcEncoder.EncodeFrame(picture, info);

        ByteBuffer res = info.getSLayerInfo().getPBsBuf();

        info.getSLayerInfo().setPNalLengthInByte(new int[] { 1, 2});
        System.out.println(Arrays.toString(info.getSLayerInfo().getPNalLengthInByte()));
        picture.delete();
        info.delete();
        return null;
    }


    public void dispose()
    {
        isvcEncoder.delete();
    }

}
