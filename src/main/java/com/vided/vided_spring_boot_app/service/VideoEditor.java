package com.vided.vided_spring_boot_app.service;

import com.vided.vided_spring_boot_app.config.OutputPath;
import com.vided.vided_spring_boot_app.config.ResourcePath;
import com.vided.vided_spring_boot_app.model.VideoSlideshowRequest;
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
import java.util.ArrayList;
import java.util.List;

@Service
public class VideoEditor {

    private static final Logger logger = LoggerFactory.getLogger(VideoEditor.class);

    @Autowired
    private MatEditor matEditor;

    @Autowired
    private OutputPath outputPath;

    @Autowired
    private ResourcePath resourcePath;

    public ResponseEntity<byte[]> createSlideshow(VideoSlideshowRequest request, int fps) throws Exception {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             FFmpegFrameRecorder recorder = setupRecorder(outputStream, request, fps)) {

            processFrames(request, recorder, fps);
            processAudio(request, recorder);

            recorder.stop();

            byte[] videoData = outputStream.toByteArray();
            return new ResponseEntity<>(videoData, HttpStatus.CREATED);

        } catch (Exception e) {
            logger.error("Failed to create slideshow", e);
            throw e;
        }
    }

    private FFmpegFrameRecorder setupRecorder(ByteArrayOutputStream outputStream, VideoSlideshowRequest request, int fps) throws Exception {
        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputStream, request.getVideoSize().width(), request.getVideoSize().height());
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_VP8);
        recorder.setFormat("webm");
        recorder.setOption("preset", "ultrafast");
        recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
        recorder.setFrameRate(fps);
        recorder.setVideoBitrate(2500 * 1000);

        recorder.setAudioCodec(avcodec.AV_CODEC_ID_VORBIS);
        recorder.setAudioChannels(2);
        recorder.setSampleRate(44100);
        recorder.setAudioBitrate(192 * 1000);
        recorder.start();
        logger.info("Recorder setup complete");
        return recorder;
    }

    private void processFrames(VideoSlideshowRequest request, FFmpegFrameRecorder recorder, int fps) throws Exception {
        int totalFrames = request.getDuration() * fps;
        int halfFramesCount = totalFrames/2;

        OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
        List<Double> zoomFactors = computeZoomFactors(halfFramesCount);

        for (Mat mat : request.getImages()) {
            List<Frame> zoomedFrames = new ArrayList<>();
            for (double factor : zoomFactors) {
                Mat zoomedMat = matEditor.zoom(mat, factor);
                zoomedFrames.add(converter.convert(zoomedMat));
            }
            for (Frame frame : zoomedFrames){
                recorder.record(frame);
            }
            for (int i = halfFramesCount-1; i >= 0; i--) {
                recorder.record(zoomedFrames.get(i));
            }
        }
        logger.info("All frames processed");
    }

    private List<Double> computeZoomFactors(int halfFramesCount) {
        List<Double> zoomFactors = new ArrayList<>(halfFramesCount);
        double zoomFactor = 1.0;
        for (int i = 0; i < halfFramesCount; i++) {
            zoomFactors.add(zoomFactor);
            zoomFactor -= 0.0015;
        }
        return zoomFactors;
    }

    private void processAudio(VideoSlideshowRequest request, FFmpegFrameRecorder recorder) throws Exception {
        Path bgMusicPath = Paths.get(resourcePath.getBgMusic().toUri()).resolve(request.getMusic() + ".mp3");
        try (FFmpegFrameGrabber audioGrabber = new FFmpegFrameGrabber(bgMusicPath.toString())) {
            audioGrabber.start();
            double targetTime = request.getDuration() * request.getImages().size() * 1_000_000;

            Frame audioFrame;
            while ((audioFrame = audioGrabber.grabSamples()) != null) {
                if (audioGrabber.getTimestamp() > targetTime) break;
                recorder.record(audioFrame);
            }
            logger.info("Audio processed");
        }
    }
}
