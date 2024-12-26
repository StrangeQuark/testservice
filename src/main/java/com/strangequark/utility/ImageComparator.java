package com.strangequark.utility;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageComparator {
    public static boolean compareImages(File image1, File image2) throws IOException {
        BufferedImage bufferedImage1 = ImageIO.read(image1);
        BufferedImage bufferedImage2 = ImageIO.read(image2);

        int height = bufferedImage1.getHeight();
        int width = bufferedImage1.getWidth();

        if(height != bufferedImage2.getHeight() || width != bufferedImage2.getWidth())
            return false;

        for(int y = 0; y < height; y++) {
            for(int x = 0; x < width; x++) {
                if(bufferedImage1.getRGB(x, y) != bufferedImage2.getRGB(x, y))
                    return false;
            }
        }

        return true;
    }
}
