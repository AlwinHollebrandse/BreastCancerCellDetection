package com.alwin;

import java.awt.image.BufferedImage;
import java.util.concurrent.Semaphore;

public class OverHeadInterface {
    public interface FuncInterface {
        void function(BufferedImage newImage, Semaphore semaphore, int x, int y);
    }
}
