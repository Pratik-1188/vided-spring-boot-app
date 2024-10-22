package com.vided.vided_spring_boot_app.service;

import com.vided.vided_spring_boot_app.config.OutputPath;
import com.vided.vided_spring_boot_app.model.VideoSlideshowRequest;
import org.bytedeco.opencv.opencv_core.Mat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class VideoSlideshowService {
    private final int fps = 12;

    @Autowired
    private MatEditor matEditor;

    @Autowired
    private VideoEditor videoEditor;

    @Autowired
    private OutputPath outputPath;

    public ResponseEntity<byte[]> createSlideshow(VideoSlideshowRequest videoSlideshowRequest) throws Exception {
//        for (int i = 0; i < videoSlideshowRequest.getImages().size(); i++) {
//            Mat mat = videoSlideshowRequest.getImages().get(i);
//            mat = matEditor.scaleToFit(mat, videoSlideshowRequest.getOrientation(), videoSlideshowRequest.getVideoSize());
//            videoSlideshowRequest.getImages().set(i, mat);
//        }
        return videoEditor.createSlideshow(videoSlideshowRequest, fps);
    }
}
