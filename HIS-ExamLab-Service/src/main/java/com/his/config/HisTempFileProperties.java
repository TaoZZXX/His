package com.his.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "his.temp-file")
public class HisTempFileProperties {
    private String examResultDir = "../HIS-TempFile";
    public String getExamResultDir() { return examResultDir; }
    public void setExamResultDir(String examResultDir) { this.examResultDir = examResultDir; }
}
