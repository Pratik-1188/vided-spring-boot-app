package com.vided.vded_spring_boot_app.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
@RequestMapping("/videoslideshow")
@CrossOrigin("http://localhost:5173/")
public class VideoSlideshowController {

    private static final Logger logger = LoggerFactory.getLogger(VideoSlideshowController.class);

    @PostMapping
    public String uploadVideo(
            @RequestParam("duration") int duration,
            @RequestParam("effect") String effect,
            @RequestParam("music") String music,
            @RequestParam("images") List<MultipartFile> images) {

        // Log the request parameters
        logger.info("Duration: " + duration);
        logger.info("Effect: " + effect);
        logger.info("Music: " + music);
        logger.info("Number of images: " + images.size());

        // Print details of each image file
        for (MultipartFile image : images) {
            logger.info("Image name: " + image.getOriginalFilename());
            logger.info("Image size: " + image.getSize());
        }

        return "Video request processed successfully";
    }
}