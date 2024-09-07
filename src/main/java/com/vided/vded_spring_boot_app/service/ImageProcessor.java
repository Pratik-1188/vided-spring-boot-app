package com.vided.vded_spring_boot_app.service;

import com.vided.vded_spring_boot_app.model.VideoSlideshowRequest;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ImageProcessor {
    @Autowired
    private VideoProcessor videoProcessor;
    public void createVideo(VideoSlideshowRequest videoSlideshowRequest) throws FFmpegFrameRecorder.Exception {
        opencv_imgcodecs.imwrite("one.jpeg", videoSlideshowRequest.getImages().getFirst());
        opencv_imgcodecs.imwrite("two.jpeg", videoSlideshowRequest.getImages().get(1));
        opencv_imgcodecs.imwrite("three.jpeg", videoSlideshowRequest.getImages().getLast());
        videoProcessor.createVideo(videoSlideshowRequest);
    }
}
