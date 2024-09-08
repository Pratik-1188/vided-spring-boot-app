package com.vided.vded_spring_boot_app.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
@ConfigurationProperties(prefix = "vided.resource")
@Data
public class ResoursePath {
    private Path bgMusic;
}
