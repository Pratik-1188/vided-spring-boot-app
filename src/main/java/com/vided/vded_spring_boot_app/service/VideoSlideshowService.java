package com.vided.vded_spring_boot_app.service;

import com.vided.vded_spring_boot_app.config.OutputPath;
import com.vided.vded_spring_boot_app.model.VideoSlideshowRequest;


import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Mat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;



@Service
public class VideoSlideshowService {
    private int fps = 16;
    
    @Autowired
    private MatEditor matEditor;

    @Autowired
    private VideoEditor videoEditor;
    
    @Autowired
    private OutputPath outputPath;

    public ResponseEntity<String> createSlideshow(VideoSlideshowRequest videoSlideshowRequest) throws FFmpegFrameRecorder.Exception {

    
        // Scaling and Padding
        for (int i = 0; i < videoSlideshowRequest.getImages().size(); i++) {
            Mat mat = videoSlideshowRequest.getImages().get(i);

            // Scale the mat to fit video size
            mat = matEditor.scaleToFit(mat, videoSlideshowRequest.getOrientation(), videoSlideshowRequest.getVideoSize());

            // Add alpha channel if needed
//            if (mat.channels() < 4) {
//                opencv_imgproc.cvtColor(mat, mat, opencv_imgproc.COLOR_BGR2BGRA);  // Modify in-place
//            }

            // Add transparent padding
//            mat = matEditor.addTransparentPadding(mat, videoSlideshowRequest.getVideoSize());

            // Replace the original mat with the padded one
            videoSlideshowRequest.getImages().set(i, mat);
        }


        return videoEditor.createSlideshow(videoSlideshowRequest, fps);


    }
}
