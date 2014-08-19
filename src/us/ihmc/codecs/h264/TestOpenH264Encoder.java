package us.ihmc.codecs.h264;

import us.ihmc.codecs.colorSpace.YCbCr420;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 *
 * // Colors that break: 255,0,21
 *
 * Created by jesper on 8/19/14.
 */
public class TestOpenH264Encoder {

    public static final String testFrame = "out_1743.png";

    public static void showImage(BufferedImage img)
    {
        JFrame window = new JFrame("img");
        window.getContentPane().setPreferredSize(new Dimension(img.getWidth(), img.getHeight()));

        window.getContentPane().add(new JLabel(new ImageIcon(img)));
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.pack();
        window.setVisible(true);
    }

    public static void main(String[] args) throws IOException
    {
        BufferedImage img = ImageIO.read(new File("data/" + testFrame));
//        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_3BYTE_BGR);
//        Graphics2D g = img.createGraphics();
//        g.setColor(new Color(255, 0, 21));
//        g.fillRect(0, 0, 100, 100);
//        g.dispose();
        showImage(img);

        ByteBuffer Yb = ByteBuffer.allocateDirect(img.getWidth() * img.getHeight());
        ByteBuffer CBb = ByteBuffer.allocateDirect(Yb.capacity() >> 2);
        ByteBuffer CRb = ByteBuffer.allocateDirect(Yb.capacity() >> 2);

        YCbCr420.convert(img, Yb, CBb, CRb);

        OpenH264Encoder encoder = new OpenH264Encoder(img.getWidth(), img.getHeight());
        encoder.encodeFrame(img);

        Yb.clear(); CBb.clear(); CRb.clear();


        BufferedImage out = YCbCr420.convertYCbCr420ToRGB888(Yb, CBb, CRb, img.getWidth(), img.getHeight(), -1, -1);
        showImage(out);

    }

}
