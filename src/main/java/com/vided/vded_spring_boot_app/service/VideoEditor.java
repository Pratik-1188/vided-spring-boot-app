package com.vided.vded_spring_boot_app.service;

import com.vided.vded_spring_boot_app.config.OutputPath;
import com.vided.vded_spring_boot_app.config.ResourcePath;
import com.vided.vded_spring_boot_app.model.VideoSlideshowRequest;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.*;
import org.bytedeco.opencv.opencv_core.Mat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class VideoEditor {
    @Autowired
    private MatEditor matEditor;

    @Autowired
    OutputPath outputPath;

    @Autowired
    ResourcePath resourcePath;

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


        Path bgMusicRoot = Paths.get(resourcePath.getBgMusic().toUri()).toAbsolutePath();
        FFmpegFrameGrabber audioGrabber = new FFmpegFrameGrabber(bgMusicRoot.resolve(videoSlideshowRequest.getMusic() + ".mp3").toString());
        double targetTimeInMicroseconds = videoSlideshowRequest.getDuration() * videoSlideshowRequest.getImages().size() * 1_000_000; // Convert target time to microseconds

        recorder.start();
        audioGrabber.start();

        long videoTimestamp = 0;
        long audioTimestamp = audioGrabber.getTimestamp(); // Initial audio timestamp

        for (Mat mat : videoSlideshowRequest.getImages()) {
            double zoomFactor = 1.000;

            // Loop through frames for each image
            for (int i = 0; i < videoSlideshowRequest.getDuration() * (fps / 2); i++) {

                // Apply zoom effect to the image
                Mat zoomedMat = matEditor.zoom(mat, zoomFactor);

                // Convert zoomed Mat to Frame and record video frame
                OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
                Frame frame = converter.convert(zoomedMat);
                recorder.setTimestamp(videoTimestamp); // Set video timestamp
                recorder.record(frame);
                recorder.record(frame); // Duplicate for smoother effect

                // Update video timestamp by frame duration (in microseconds)
                videoTimestamp += (2_000_000 / fps);

                // Synchronize audio with video
                while (audioTimestamp < videoTimestamp) {
                    Frame audioFrame = audioGrabber.grabFrame();
                    if (audioFrame != null && audioFrame.samples != null) {
                        recorder.recordSamples(audioFrame.samples);
                        audioTimestamp = audioGrabber.getTimestamp(); // Update audio timestamp
                    } else {
                        break; // No more audio frames
                    }
                }

                // Increment zoom for each frame
                zoomFactor += 0.0015;
            }

            // Reset zoom for the next image
            zoomFactor = 1.000;
        }





//        while ((audioFrame = audioGrabber.grabFrame()) != null) {
//            if (audioGrabber.getTimestamp() > targetTimeInMicroseconds) {
//                break;
//            }
//            recorder.recordSamples(audioFrame.samples);
//        }

        audioGrabber.stop();
        audioGrabber.release();


        recorder.stop();
        recorder.release();

        // Read the temporary file into a byte array
        byte[] videoData = outputStream.toByteArray();

        // Return the video data as a response
        return new ResponseEntity<>(videoData, HttpStatus.CREATED);
    }
}
