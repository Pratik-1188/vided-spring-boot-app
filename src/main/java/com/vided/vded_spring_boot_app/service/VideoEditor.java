package com.vided.vded_spring_boot_app.service;

import com.vided.vded_spring_boot_app.config.OutputPath;
import com.vided.vded_spring_boot_app.model.VideoSlideshowRequest;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;

import org.bytedeco.opencv.opencv_core.Mat;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class VideoEditor {
    @Autowired
    private MatEditor matEditor;

    @Autowired
    OutputPath outputPath;

    public ResponseEntity<String> createSlideshow(VideoSlideshowRequest videoSlideshowRequest, int fps) throws FFmpegFrameRecorder.Exception {
        Path outputDirectory = Paths.get(outputPath.getVideoSlideshow().toUri()).toAbsolutePath();
        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(
                outputDirectory.resolve("output.mp4").toString(),
                videoSlideshowRequest.getVideoSize().width(),
                videoSlideshowRequest.getVideoSize().height()
        );

        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
        recorder.setFormat("mp4");
        recorder.setFrameRate(fps);
        recorder.setVideoBitrate(3000 * 1000);
        recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);

        recorder.start();
        for(Mat mat: videoSlideshowRequest.getImages()){
            double zoomFactor = 1.000;
            for (int i = 0; i < videoSlideshowRequest.getDuration() * fps; i++) {

                Mat zoomedMat = matEditor.zoom(mat, zoomFactor);

                OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
                Frame frame = converter.convert(zoomedMat);
                recorder.record(frame);
                zoomFactor += 0.001;
            }
            zoomFactor = 1.000;
        }
        recorder.stop();
        recorder.release();
        return  new ResponseEntity<>(outputDirectory.resolve("output.mp4").toString(), HttpStatus.CREATED);
    }
}
