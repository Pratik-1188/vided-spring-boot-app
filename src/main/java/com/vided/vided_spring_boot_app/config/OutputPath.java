package com.vided.vided_spring_boot_app.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.nio.file.Path;


@Data
@Component
@ConfigurationProperties(prefix = "vided.output.path")
public class OutputPath {
    private Path videoSlideshow;
}

