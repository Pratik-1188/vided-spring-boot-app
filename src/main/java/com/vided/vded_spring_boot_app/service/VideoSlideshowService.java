package com.vided.vded_spring_boot_app.service;

import com.vided.vded_spring_boot_app.config.OutputPath;
import com.vided.vded_spring_boot_app.model.VideoSlideshowRequest;

import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Mat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class VideoSlideshowService {
    @Autowired
    private MatEditor matEditor;

    @Autowired
    private OutputPath outputPath;

    public ResponseEntity<String> createSlideshow(VideoSlideshowRequest videoSlideshowRequest) {


        for (int i = 0; i < videoSlideshowRequest.getImages().size(); i++) {
            Mat mat = videoSlideshowRequest.getImages().get(i);

            // Scale the mat to fit video size
            mat = matEditor.scaleToFit(mat, videoSlideshowRequest.getOrientation(), videoSlideshowRequest.getVideoSize());

            // Add alpha channel if needed
            if (mat.channels() < 4) {
                opencv_imgproc.cvtColor(mat, mat, opencv_imgproc.COLOR_BGR2BGRA);  // Modify in-place
            }

            // Add transparent padding
            mat = matEditor.addTransparentPadding(mat, videoSlideshowRequest.getVideoSize());

            // Replace the original mat with the padded one
            videoSlideshowRequest.getImages().set(i, mat);


//            Path outputDirectory = Paths.get(outputPath.getVideoSlideshow().toUri()).toAbsolutePath();
//            opencv_imgcodecs.imwrite(outputDirectory.resolve(String.format("one_%d.jpeg", i)).toString(), videoSlideshowRequest.getImages().get(i));
//            System.out.println(outputDirectory.resolve(String.format("one_%d.jpeg", i)));
        }


        return new ResponseEntity<>("hi", HttpStatus.CREATED);
    }
}
