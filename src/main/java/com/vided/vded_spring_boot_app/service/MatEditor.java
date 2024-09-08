package com.vided.vded_spring_boot_app.service;

import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.bytedeco.opencv.opencv_core.Size;
import org.opencv.core.CvType;
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

    public Mat addTransparentPadding(Mat alphaMat, Size finalSize) {
        // Calculate padding to center the image
        int paddingX = (finalSize.width() - alphaMat.cols()) / 2;
        int paddingY = (finalSize.height() - alphaMat.rows()) / 2;

        // Create a new Mat with the final size, filled with transparency (4 channels, BGRA)
        Mat paddedMat = new Mat(finalSize.height(), finalSize.width(), CvType.CV_8UC4, new Scalar(255, 255, 255, 0));

        // Copy the original image to the center of the new padded Mat
        Mat roi = paddedMat.apply(new org.bytedeco.opencv.opencv_core.Rect(paddingX, paddingY, alphaMat.cols(), alphaMat.rows()));
        alphaMat.copyTo(roi);

        return paddedMat;
    }

    public Mat zoom(Mat mat, double zoomFactor){
        if(zoomFactor == 1){
            return mat;
        }

        Size originalMatSize = new Size(mat.cols(), mat.rows());

        int newWidth = (int) (mat.cols() * zoomFactor);
        int newHeight = (int) (mat.rows() * zoomFactor);

        Mat zoomedMat = new Mat();
        opencv_imgproc.resize(mat, zoomedMat, new Size(newWidth, newHeight));
        return cropCenter(zoomedMat, originalMatSize);
    }

    public Mat cropCenter(Mat mat, Size originalSize) {
        // Calculate the coordinates to center the cropped region
        int x = Math.max((mat.cols() - originalSize.width()) / 2, 0);
        int y = Math.max((mat.rows() - originalSize.height()) / 2, 0);

        // Ensure cropping region is within bounds
        int cropWidth = Math.min(originalSize.width(), mat.cols());
        int cropHeight = Math.min(originalSize.height(), mat.rows());

        Rect roi = new Rect(x, y, cropWidth, cropHeight);
        return new Mat(mat, roi).clone();
    }



}
