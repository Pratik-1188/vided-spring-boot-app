package com.vided.vded_spring_boot_app.service;

import com.vided.vded_spring_boot_app.config.OutputPath;
import com.vided.vded_spring_boot_app.config.ResoursePath;
import com.vided.vded_spring_boot_app.model.VideoSlideshowRequest;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.*;
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

    @Autowired
    ResoursePath resoursePath;

    public ResponseEntity<String> createSlideshow(VideoSlideshowRequest videoSlideshowRequest, int fps) throws FFmpegFrameRecorder.Exception, FrameGrabber.Exception {
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

        // Set audio properties
        recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
        recorder.setAudioChannels(2); // Stereo sound
        recorder.setSampleRate(44100);
        recorder.setAudioBitrate(192 * 1000);

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

        Path bgMusicRoot = Paths.get(resoursePath.getBgMusic().toUri()).toAbsolutePath();
        FFmpegFrameGrabber audioGrabber = new FFmpegFrameGrabber(bgMusicRoot.resolve(videoSlideshowRequest.getMusic() + ".mp3").toString());
        audioGrabber.start();

        Frame audioFrame;
        double targetTimeInMicroseconds = videoSlideshowRequest.getDuration() * videoSlideshowRequest.getImages().size() * 1_000_000; // Convert target time to microseconds
        while ((audioFrame = audioGrabber.grabFrame()) != null) {
            if (audioGrabber.getTimestamp() > targetTimeInMicroseconds) {
                break; // Stop reading audio after reaching the target time
            }
            recorder.recordSamples(audioFrame.samples);
        }

        audioGrabber.stop();
        audioGrabber.release();


        recorder.stop();
        recorder.release();
        return  new ResponseEntity<>(outputDirectory.resolve("output.mp4").toString(), HttpStatus.CREATED);
    }
}
