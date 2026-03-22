package com.his.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 根目录 HIS-TempFile 下存放临时/上传文件（可配置绝对路径或相对工作目录）
 */
@Data
@Component
@ConfigurationProperties(prefix = "his.temp-file")
public class HisTempFileProperties {

    /**
     * 检查结果图片目录；默认仓库根目录下 HIS-TempFile（自 HIS-System 模块启动时工作目录多为 HIS-System，故用 ../HIS-TempFile）
     */
    private String examResultDir = "../HIS-TempFile";
}
