package com.vided.vided_spring_boot_app.model;

import com.vided.vided_spring_boot_app.config.ResourcePath;
import lombok.Data;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Size;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Data
public class VideoSlideshowRequest {
    private int duration;
    private String effect;
    private String music;
    private List<Mat> images;
    private String orientation;
    private Size videoSize;

    public VideoSlideshowRequest(int duration, String effect, String music, List<MultipartFile> images, ResourcePath resourcePath) throws IOException {
        this.duration = 4;
        this.effect = effect;
        this.music = getRandomBgMusic(resourcePath);
        this.images = new ArrayList<>();  // Initialize the List

        // Convert MultipartFile images to Mat objects
        for (MultipartFile image : images) {
            byte[] bytes = image.getBytes();
            Mat mat = opencv_imgcodecs.imdecode(new Mat(bytes), opencv_imgcodecs.IMREAD_UNCHANGED);
            this.images.add(mat);  // Add each Mat object to the list
        }
        this.orientation = "Portrait";
        this.videoSize = new Size(720, 1280);
    }

    public String getRandomBgMusic(ResourcePath resourcePath) {
            Random random = new Random();
            int randomNumber = random.nextInt(6) + 1;
            return (String.valueOf(randomNumber));
    }
}
