package com.his.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class ExamResultResourceConfig implements WebMvcConfigurer {

    private final HisTempFileProperties tempFileProperties;

    public ExamResultResourceConfig(HisTempFileProperties tempFileProperties) {
        this.tempFileProperties = tempFileProperties;
    }

    @PostConstruct
    public void ensureUploadDir() throws IOException {
        Path dir = Paths.get(tempFileProperties.getExamResultDir()).toAbsolutePath().normalize();
        Files.createDirectories(dir);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path base = Paths.get(tempFileProperties.getExamResultDir()).toAbsolutePath().normalize();
        String location = "file:" + base.toString().replace('\\', '/') + "/";
        registry.addResourceHandler("/exam-result-files/**")
                .addResourceLocations(location);
    }
}
