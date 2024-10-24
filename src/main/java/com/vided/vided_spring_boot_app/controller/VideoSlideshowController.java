package com.vided.vided_spring_boot_app.controller;

import com.vided.vided_spring_boot_app.config.ResourcePath;
import com.vided.vided_spring_boot_app.service.VideoSlideshowService;
import com.vided.vided_spring_boot_app.model.VideoSlideshowRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import org.slf4j.Logger;

import java.io.OutputStream;
import java.util.List;

@RestController
@RequestMapping("/videoslideshow")
@CrossOrigin("http://localhost:5173")
public class VideoSlideshowController {
    @Autowired
    private VideoSlideshowService videoSlideshowService;

    @Autowired
    ResourcePath resourcePath;

    private static final Logger logger = LoggerFactory.getLogger(VideoSlideshowController.class);

    @PostMapping
    public ResponseEntity<byte[]> handelVideoSlideshowRequest(
            @RequestParam("duration") int duration,
            @RequestParam("effect") String effect,
            @RequestParam("music") String music,
            @RequestParam("images") List<MultipartFile> images) throws Exception {

        var videoSlideshowRequest = new VideoSlideshowRequest(duration, effect, music, images, resourcePath);
        return videoSlideshowService.createSlideshow(videoSlideshowRequest);

    }
}
