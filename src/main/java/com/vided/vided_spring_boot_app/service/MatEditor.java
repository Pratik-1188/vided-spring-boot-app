package com.vided.vided_spring_boot_app.service;

import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.Size;
import org.springframework.stereotype.Service;

@Service
public class MatEditor {


    public Mat scaleToFit(Mat mat, String orientation, Size targetSize){
        double currentAspectRatio = (double) mat.cols() / mat.rows();

        int newWidth, newHeight;
        if(orientation.equals("Portrait")){
            newWidth = targetSize.width();
            newHeight = (int) (newWidth / currentAspectRatio);

            if(newHeight > targetSize.height()){
                newHeight = targetSize.height();
                newWidth = (int) (newHeight * currentAspectRatio);
            }
        }else{
            newHeight = targetSize.height();
            newWidth = (int) (newHeight * currentAspectRatio);

            if(newWidth > targetSize.width()){
                newWidth = targetSize.width();
                newHeight = (int) (newWidth / currentAspectRatio);
            }
        }

        Mat result = new Mat();
        opencv_imgproc.resize(mat, result, new Size(newWidth, newHeight));
        return  result;
    }

    public Mat zoom(Mat mat, double zoomFactor){
        if(zoomFactor == 1){
            return mat;
        }

        // calculate new size
        int newWidth = (int) (mat.cols() * zoomFactor);
        int newHeight = (int) (mat.rows() * zoomFactor);

        int x = (mat.cols() - newWidth) / 2;
        int y = (mat.rows() - newHeight) / 2;
        Rect roi = new Rect(x, y, newWidth, newHeight);

        return new Mat(mat, roi);
    }

//    public Mat addTransparentPadding(Mat alphaMat, Size finalSize) {
//        // Calculate padding to center the image
//        int paddingX = (finalSize.width() - alphaMat.cols()) / 2;
//        int paddingY = (finalSize.height() - alphaMat.rows()) / 2;
//
//        // Create a new Mat with the final size, filled with transparency (4 channels, BGRA)
//        Mat paddedMat = new Mat(finalSize.height(), finalSize.width(), CvType.CV_8UC4, new Scalar(255, 255, 255, 0));
//
//        // Copy the original image to the center of the new padded Mat
//        Mat roi = paddedMat.apply(new org.bytedeco.opencv.opencv_core.Rect(paddingX, paddingY, alphaMat.cols(), alphaMat.rows()));
//        alphaMat.copyTo(roi);
//
//        return paddedMat;
//    }
}
