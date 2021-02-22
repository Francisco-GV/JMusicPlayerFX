package com.frank.jmusicplayerfx.util;

import com.mortennobel.imagescaling.ResampleFilters;
import com.mortennobel.imagescaling.ResampleOp;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import java.awt.image.BufferedImage;

public class ImageScaler {
    public static Image scaleImage(Image image, int width, int height) {
        ResampleOp resizeOp = new ResampleOp(width, height);
        resizeOp.setFilter(ResampleFilters.getLanczos3Filter());

        BufferedImage swingImage = SwingFXUtils.fromFXImage(image, null);
        BufferedImage scaledImage = resizeOp.filter(swingImage, null);

        return SwingFXUtils.toFXImage(scaledImage, null);
    }
}