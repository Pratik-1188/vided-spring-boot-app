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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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


        recorder.start();

        ExecutorService executor = Executors.newFixedThreadPool(2);

        executor.submit(() -> {
            try{
                for(Mat mat: videoSlideshowRequest.getImages()){
                    double zoomFactor = 1.000;
                    for (int i = 0; i < videoSlideshowRequest.getDuration() * (fps/2); i++) {

                        Mat zoomedMat = matEditor.zoom(mat, zoomFactor);

                        OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
                        Frame frame = converter.convert(zoomedMat);

                        recorder.record(frame);
                        recorder.record(frame);

                        zoomFactor += 0.0015;
                    }
                    zoomFactor = 1.000;
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
        });

        executor.submit(() ->{
            try{
                Path bgMusicRoot = Paths.get(resourcePath.getBgMusic().toUri()).toAbsolutePath();
                FFmpegFrameGrabber audioGrabber = new FFmpegFrameGrabber(bgMusicRoot.resolve(videoSlideshowRequest.getMusic() + ".mp3").toString());
                audioGrabber.start();

                Frame audioFrame;
                double targetTimeInMicroseconds = videoSlideshowRequest.getDuration() * videoSlideshowRequest.getImages().size() * 1_000_000; // Convert target time to microseconds
                while ((audioFrame = audioGrabber.grabFrame()) != null) {
                    if (audioGrabber.getTimestamp() > targetTimeInMicroseconds) {
                        break;
                    }
                    recorder.recordSamples(audioFrame.samples);
                }
                audioGrabber.stop();
                audioGrabber.release();
            }catch (Exception e){
                e.printStackTrace();
            }
        });

        // Shutdown executor after processing
        executor.shutdown();
        while (!executor.isTerminated()) {
            // Wait for both threads to complete
        }

        recorder.stop();
        recorder.release();

        // Read the temporary file into a byte array
        byte[] videoData = outputStream.toByteArray();

        // Return the video data as a response
        return new ResponseEntity<>(videoData, HttpStatus.CREATED);
    }
}
