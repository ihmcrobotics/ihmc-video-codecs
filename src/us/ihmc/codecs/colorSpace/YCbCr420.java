package us.ihmc.codecs.colorSpace;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Helper methods to convert BufferedImages to YUV420 format
 *
 * Uses the Rec. 709 color format
 *
 * See
 *  http://www.mathworks.com/help/vision/ref/colorspaceconversion.html
 *  http://www.equasys.de/colorconversion.html
 *
 * Created by jesper on 8/19/14.
 */
public class YCbCr420 {

    public static final int Y(int r, int g, int b)
    {
        return 16 + (int)(0.183 * r + 0.614 * g + 0.062 * b);
    }

    public static final int Cb(int r, int g, int b)
    {
        return 128 + (int)(-0.101 * r + -0.339 * g + 0.439 * b);
    }

    public static final int Cr(int r, int g, int b)
    {
        return 128 + (int)(0.439 * r + -0.399 * g + -0.040 * b);
    }


    public static final int r(int Y, int Cb, int Cr)
    {
        // Add 0.5 for correct rounding
        return (int)((Y - 16) * 1.164 + (Cr - 128) * 1.793 + 0.5);
    }

    public static final int g(int Y, int Cb, int Cr)
    {
        // Add 0.5 for correct rounding
        return (int)((Y - 16) * 1.164 + (Cb - 128) * -0.213 + (Cr - 128) * -0.533 + 0.5);
    }

    public static final int b(int Y, int Cb, int Cr)
    {
        // Add 0.5 for correct rounding
        return (int)((Y-16) * 1.164 + (Cb - 128) * 2.112 + 0.5);
    }




    public static BufferedImage convertYCbCr420ToRGB888(ByteBuffer Yb, ByteBuffer CBb, ByteBuffer CRb, int w, int h, int YStride, int CbCrStride)
    {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
        WritableRaster raster = img.getRaster();
        DataBufferByte buffer = (DataBufferByte) raster.getDataBuffer();

        System.out.println(r(63,112,239));
        System.out.println(g(63,112,239));
        System.out.println(b(63,112,239));

        for (int yi = 0; yi < h; yi += 2) {
            for(int xi = 0; xi < w; xi+=2) {
                int Cb = CBb.get() & 0xFF;
                int Cr = CRb.get() & 0xFF;

                int Y = Yb.get() & 0xFF;
                int bufferIndex = ((yi * w) + xi) * 3;
                buffer.setElem(bufferIndex, b(Y, Cb, Cr));
                buffer.setElem(bufferIndex + 1, g(Y, Cb, Cr));
                buffer.setElem(bufferIndex + 2, r(Y, Cb, Cr));

                Y = Yb.get() & 0xFF;
                bufferIndex = ((yi * w) + xi + 1) * 3;
                buffer.setElem(bufferIndex, b(Y, Cb, Cr));
                buffer.setElem(bufferIndex + 1, g(Y, Cb, Cr));
                buffer.setElem(bufferIndex + 2, r(Y, Cb, Cr));

                Y = Yb.get() & 0xFF;
                bufferIndex = (((yi + 1) * w) + xi) * 3;
                buffer.setElem(bufferIndex, b(Y, Cb, Cr));
                buffer.setElem(bufferIndex + 1, g(Y, Cb, Cr));
                buffer.setElem(bufferIndex + 2, r(Y, Cb, Cr));

                Y = Yb.get() & 0xFF;
                bufferIndex = (((yi + 1) * w) + xi + 1) * 3;
                buffer.setElem(bufferIndex, b(Y, Cb, Cr));
                buffer.setElem(bufferIndex + 1, g(Y, Cb, Cr));
                buffer.setElem(bufferIndex + 2, r(Y, Cb, Cr));

            }
        }
        return img;
    }



    public static void convert(BufferedImage in, ByteBuffer Yb, ByteBuffer CBb, ByteBuffer CRb)
    {
        int w = in.getWidth();
        int h = in.getHeight();

        if(in.getType() != BufferedImage.TYPE_3BYTE_BGR)
        {
            throw new RuntimeException("Can only handle 3 byte bgr images");
        }

        WritableRaster raster = in.getRaster();
        byte[] imageBuffer = ((DataBufferByte) raster.getDataBuffer()).getData();

        for(int yi = 0; yi < h - 1; yi+=2)
        {
            for(int xi = 0; xi < w - 1; xi+=2)
            {
                // Unroll the loop to handle chroma sub-sampling. Use simple average of the box
                int bufferIndex = ((yi * w) + xi) * 3;
                int b = imageBuffer[bufferIndex] & 0xFF;
                int g = imageBuffer[bufferIndex + 1] & 0xFF;
                int r = imageBuffer[bufferIndex + 2] & 0xFF;

                int Y1 = Y(r,g,b);
                int Cb1 = Cb(r,g,b);
                int Cr1 = Cr(r,g,b);

                bufferIndex = ((yi * w) + xi + 1) * 3;
                b = imageBuffer[bufferIndex] & 0xFF;
                g = imageBuffer[bufferIndex + 1] & 0xFF;
                r = imageBuffer[bufferIndex + 2] & 0xFF;

                int Y2 = Y(r,g,b);
                int Cb2 = Cb(r,g,b);
                int Cr2 = Cr(r,g,b);

                bufferIndex = (((yi + 1) * w) + xi) * 3;
                b = imageBuffer[bufferIndex] & 0xFF;
                g = imageBuffer[bufferIndex + 1] & 0xFF;
                r = imageBuffer[bufferIndex + 2] & 0xFF;

                int Y3 = Y(r,g,b);
                int Cb3 = Cb(r,g,b);
                int Cr3 = Cr(r,g,b);

                bufferIndex = (((yi + 1) * w) + xi + 1) * 3;
                b = imageBuffer[bufferIndex] & 0xFF;
                g = imageBuffer[bufferIndex + 1] & 0xFF;
                r = imageBuffer[bufferIndex + 2] & 0xFF;

                int Y4 = Y(r,g,b);
                int Cb4 = Cb(r,g,b);
                int Cr4 = Cr(r,g,b);

                Yb.put((byte)Y1);
                Yb.put((byte)Y2);
                Yb.put((byte)Y3);
                Yb.put((byte)Y4);

                CBb.put((byte)((Cb1 + Cb2 + Cb3 + Cb4)/4));
                CRb.put((byte)((Cr1 + Cr2 + Cr3 + Cr4)/4));

            }
        }
    }
}
