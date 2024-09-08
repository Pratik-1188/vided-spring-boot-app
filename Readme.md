
### Summary of Common Resolutions:

| **Aspect Ratio** | **Resolution (Landscape)** | **Resolution (Portrait)** |
|------------------|----------------------------|---------------------------|
| **16:9**         | 1920 x 1080 (1080p)        | N/A                       |
| **16:9**         | 1280 x 720 (720p)          | N/A                       |
| **9:16**         | N/A                        | 1080 x 1920 (1080p)        |
| **9:16**         | N/A                        | 720 x 1280 (720p)          |


> Save mat
```java
import java.nio.file.Path;
import java.nio.file.Paths;
import org.bytedeco.opencv.global.opencv_imgcodecs;

Path outputDirectory = Paths.get(outputPath.getVideoSlideshow().toUri()).toAbsolutePath();
opencv_imgcodecs.imwrite(outputDirectory.resolve(String.format("one_%d.jpeg", i)).toString(), videoSlideshowRequest.getImages().get(i));
System.out.println(outputDirectory.resolve(String.format("one_%d.jpeg", i)));
```