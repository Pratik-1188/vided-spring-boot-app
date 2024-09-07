package com.vided.vded_spring_boot_app.service;

import com.vided.vded_spring_boot_app.model.VideoSlideshowRequest;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.opencv_core.Mat;
import org.springframework.stereotype.Service;

@Service
public class VideoProcessor {
    public void createVideo(VideoSlideshowRequest videoSlideshowRequest) throws FFmpegFrameRecorder.Exception {
        Mat mat = videoSlideshowRequest.getImages().getFirst();
        OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
        Frame frame = converter.convert(mat);

        System.out.println(frame.imageWidth);

        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder("mnt/out.mp4", 1080, 1920);
        recorder.start();
        for (int i = 0; i < 100; i++) {
            recorder.record(frame);

        }
        recorder.stop();
        recorder.release();
    }
}
