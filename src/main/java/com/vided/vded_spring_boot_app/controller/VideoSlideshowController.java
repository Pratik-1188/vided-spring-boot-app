package com.vided.vded_spring_boot_app.controller;

import com.vided.vded_spring_boot_app.model.VideoSlideshowRequest;
import com.vided.vded_spring_boot_app.service.ImageProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/videoslideshow")
@CrossOrigin("http://localhost:5173/")
public class VideoSlideshowController {
    @Autowired
    private ImageProcessor imageProcessor;

    private static final Logger logger = LoggerFactory.getLogger(VideoSlideshowController.class);

    @PostMapping
    public String uploadVideo(
            @RequestParam("duration") int duration,
            @RequestParam("effect") String effect,
            @RequestParam("music") String music,
            @RequestParam("images") List<MultipartFile> images) throws IOException {

        // Log the request parameters
        logger.info("Duration: " + duration);
        logger.info("Effect: " + effect);
        logger.info("Music: " + music);
        logger.info("Number of images: " + images.size());
        for (MultipartFile image : images) {
            logger.info("Image name: " + image.getOriginalFilename());
            logger.info("Image size: " + image.getSize());
        }

        var videoSlideshowRequest = new VideoSlideshowRequest(duration, effect, music, images);
        imageProcessor.createVideo(videoSlideshowRequest);
        return "Video request processed successfully";
    }
}
