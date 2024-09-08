package com.vided.vded_spring_boot_app.model;

import lombok.Data;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Size;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Data
public class VideoSlideshowRequest {
    private int duration;
    private String effect;
    private String music;
    private List<Mat> images;  // Store the images as Mat objects
    private String orientation;
    private Size videoSize;

    // Constructor
    public VideoSlideshowRequest(int duration, String effect, String music, List<MultipartFile> images) throws IOException {
        this.duration = duration;
        this.effect = effect;
        this.music = "Lukrembo  Marshmallow" ;
        this.images = new ArrayList<>();  // Initialize the List

        // Convert MultipartFile images to Mat objects
        for (MultipartFile image : images) {
            byte[] bytes = image.getBytes();
            Mat mat = opencv_imgcodecs.imdecode(new Mat(bytes), opencv_imgcodecs.IMREAD_UNCHANGED);
            this.images.add(mat);  // Add each Mat object to the list
        }

        // hardcoding for the time being **
        this.orientation = "Portrait";
        this.videoSize = new Size(1080, 1920);
    }
}
