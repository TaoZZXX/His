package com.his.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SmsSkd {
    private Long id;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime date;
    private Integer status;
    private Long remain;
    private Integer noon;
    private Long staffId;
    private Long deptId;
    private String skLimit;
}
