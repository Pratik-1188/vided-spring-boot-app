package com.vided.vded_spring_boot_app.service;

import com.vided.vded_spring_boot_app.config.OutputPath;
import com.vided.vded_spring_boot_app.config.ResourcePath;
import com.vided.vded_spring_boot_app.model.VideoSlideshowRequest;
import lombok.extern.flogger.Flogger;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.*;
import org.bytedeco.opencv.opencv_core.Mat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class VideoEditor {
    @Autowired
    private MatEditor matEditor;

    @Autowired
    OutputPath outputPath;

    @Autowired
    ResourcePath resourcePath;

    private static final Logger logger = LoggerFactory.getLogger(VideoEditor.class);

    public ResponseEntity<byte[]> createSlideshow(VideoSlideshowRequest videoSlideshowRequest, int fps) throws Exception {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(
                outputStream,
                videoSlideshowRequest.getVideoSize().width(),
                videoSlideshowRequest.getVideoSize().height()
        );

        // Set up video properties
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_VP8);
        recorder.setFormat("webm");
        recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
        recorder.setFrameRate(fps);
        recorder.setVideoBitrate(3000 * 1000);

        // Set up audio properties
        recorder.setAudioCodec(avcodec.AV_CODEC_ID_VORBIS);
        recorder.setAudioChannels(2);
        recorder.setSampleRate(44100);
        recorder.setAudioBitrate(192 * 1000);

        recorder.start();

        logger.info("start video processing");

        try {
            // Process video frames
            int numLoops = videoSlideshowRequest.getDuration() * (fps);
            OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
            Frame frame;

            for (Mat mat : videoSlideshowRequest.getImages()) {
                double zoomFactor = 1.000;
                for (int i = 0; i < numLoops; i++) {
                    Mat zoomedMat = matEditor.zoom(mat, zoomFactor);
                    frame = converter.convert(zoomedMat);
                    recorder.record(frame);
                    zoomFactor -= 0.0016;
                }
            }

            logger.info("All frames collected");

            // Process audio after all video frames are recorded
            Path bgMusicRoot = Paths.get(resourcePath.getBgMusic().toUri()).toAbsolutePath();
            FFmpegFrameGrabber audioGrabber = new FFmpegFrameGrabber(bgMusicRoot.resolve(videoSlideshowRequest.getMusic() + ".mp3").toString());
            double targetTimeInMicroseconds = videoSlideshowRequest.getDuration() * videoSlideshowRequest.getImages().size() * 1_000_000; // Convert target time to microseconds

            logger.info("Grabbed Audio file from disk");

            logger.info("Starting audio processing");

            audioGrabber.start();
            Frame audioFrame;
            while ((audioFrame = audioGrabber.grabSamples()) != null) {
                if (audioGrabber.getTimestamp() > targetTimeInMicroseconds) {
                    break;
                }
                recorder.record(audioFrame);
            }
            audioGrabber.stop();
            audioGrabber.release();

            logger.info("All audio samples collected");

        } catch (Exception e) {
            e.printStackTrace();
        }

        recorder.stop();
        recorder.release();

        byte[] videoData = outputStream.toByteArray();

        logger.info("Returning bytes");

        return new ResponseEntity<>(videoData, HttpStatus.CREATED);
    }
}
