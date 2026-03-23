package com.his.controller;

import com.his.config.HisTempFileProperties;
import com.his.domain.Result;
import com.his.enums.ResultCode;
import com.his.utils.JwtUtil;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * 检查结果图片：写入 HIS-TempFile，库中保存网关可访问的 URL（/sms/exam-result-files/...）
 */
@RestController
@RequestMapping("/exam-lab")
public class ExamLabResultImageController {

    private static final long MAX_BYTES = 10 * 1024 * 1024;

    private final JwtUtil jwtUtil;
    private final HisTempFileProperties tempFileProperties;

    public ExamLabResultImageController(JwtUtil jwtUtil, HisTempFileProperties tempFileProperties) {
        this.jwtUtil = jwtUtil;
        this.tempFileProperties = tempFileProperties;
    }

    @PostMapping("/result-images/upload")
    public Result<Map<String, String>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("token") String token) {
        if (token == null || token.trim().isEmpty()) {
            return Result.error(ResultCode.TOKEN_INVALID, "token 无效");
        }
        if (!jwtUtil.validateToken(token.trim())) {
            return Result.error(ResultCode.TOKEN_INVALID, "token 无效或已过期");
        }
        if (jwtUtil.getUserIdFromToken(token.trim()) == null) {
            return Result.error(ResultCode.TOKEN_INVALID, "token 无效");
        }
        if (file == null || file.isEmpty()) {
            return Result.error(ResultCode.PARAM_ERROR, "请选择图片文件");
        }
        if (file.getSize() > MAX_BYTES) {
            return Result.error(ResultCode.PARAM_ERROR, "单张图片不超过 10MB");
        }
        String ext = resolveExtension(file);
        if (ext == null) {
            return Result.error(ResultCode.PARAM_ERROR, "仅支持 jpg、jpeg、png、gif、webp");
        }
        String stored = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        try {
            Path dir = Paths.get(tempFileProperties.getExamResultDir()).toAbsolutePath().normalize();
            Files.createDirectories(dir);
            Path target = dir.resolve(stored).normalize();
            if (!target.startsWith(dir)) {
                return Result.error(ResultCode.PARAM_ERROR, "非法路径");
            }
            file.transferTo(target.toFile());
        } catch (Exception e) {
            return Result.error(ResultCode.SERVER_ERROR, "保存文件失败");
        }
        Map<String, String> data = new HashMap<>(2);
        data.put("url", "/sms/exam-result-files/" + stored);
        data.put("fileName", stored);
        return Result.success("上传成功", data);
    }

    private static String resolveExtension(MultipartFile file) {
        String ct = file.getContentType();
        if (ct != null) {
            String c = ct.toLowerCase(Locale.ROOT);
            if (c.contains("jpeg") || c.contains("jpg")) {
                return "jpg";
            }
            if (c.contains("png")) {
                return "png";
            }
            if (c.contains("gif")) {
                return "gif";
            }
            if (c.contains("webp")) {
                return "webp";
            }
        }
        String original = file.getOriginalFilename();
        if (!StringUtils.hasText(original) || !original.contains(".")) {
            return null;
        }
        String e = original.substring(original.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        if (e.equals("jpeg")) {
            return "jpg";
        }
        if (e.equals("jpg") || e.equals("png") || e.equals("gif") || e.equals("webp")) {
            return e;
        }
        return null;
    }
}
