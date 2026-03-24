package com.his.config;

import com.his.domain.SmsLoginLog;
import com.his.mapper.SmsLoginLogMapper;
import com.his.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;

@Component
public class OperationLogInterceptor implements HandlerInterceptor {

    @Autowired
    private SmsLoginLogMapper smsLoginLogMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        try {
            String method = request.getMethod();
            if ("GET".equalsIgnoreCase(method) || "OPTIONS".equalsIgnoreCase(method)) {
                return;
            }
            String uri = request.getRequestURI();
            if (uri == null || !uri.startsWith("/")) {
                return;
            }
            if (uri.startsWith("/error") || uri.startsWith("/actuator")) {
                return;
            }
            if (uri.contains("/staff/login") || uri.contains("/staff/register") || uri.contains("/staff/info")) {
                return;
            }
            boolean isDelete = "DELETE".equalsIgnoreCase(method);
            if (!isDelete && isQueryLike(method, uri)) {
                return;
            }
            Long userId = resolveUserId(request);
            String action = buildCompactAction(method, uri, response.getStatus(), resolveClientIp(request));
            SmsLoginLog row = new SmsLoginLog();
            row.setUserId(userId);
            row.setCreateTime(LocalDateTime.now());
            row.setIp(action);
            smsLoginLogMapper.insert(row);
        } catch (Exception ignored) {
        }
    }

    private Long resolveUserId(HttpServletRequest request) {
        String token = request.getHeader("X-Token");
        if (token == null || token.trim().isEmpty()) {
            String auth = request.getHeader("Authorization");
            if (auth != null && auth.startsWith("Bearer ")) {
                token = auth.substring(7);
            }
        }
        if (token == null || token.trim().isEmpty()) {
            return null;
        }
        return jwtUtil.getUserIdFromToken(token);
    }

    private boolean isQueryLike(String method, String uri) {
        if (uri == null) {
            return false;
        }
        if ("GET".equalsIgnoreCase(method) || "OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }
        String u = uri.toLowerCase();
        return u.endsWith("/getallbypage")
                || u.contains("/query")
                || u.contains("/search")
                || u.contains("/list")
                || u.contains("/page");
    }

    private String buildCompactAction(String method, String uri, int status, String ip) {
        String m = method == null ? "UNK" : method.toUpperCase();
        String op = opCode(m);
        String p = normalizeUri(uri);
        String i = normalizeIp(ip);
        String action = "OP|" + op + "|" + m + " " + p + "|S=" + status + "|I=" + i;
        if (action.length() <= 64) {
            return action;
        }
        String shortIp = i.length() > 15 ? i.substring(0, 15) : i;
        action = "OP|" + op + "|" + m + " " + p + "|S=" + status + "|I=" + shortIp;
        if (action.length() <= 64) {
            return action;
        }
        int keep = Math.max(8, 64 - ("OP|" + op + "|" + m + " |S=" + status + "|I=" + shortIp).length());
        if (p.length() > keep) {
            p = p.substring(0, keep);
        }
        return "OP|" + op + "|" + m + " " + p + "|S=" + status + "|I=" + shortIp;
    }

    private String opCode(String method) {
        if ("DELETE".equalsIgnoreCase(method)) {
            return "D";
        }
        if ("POST".equalsIgnoreCase(method)) {
            return "C";
        }
        if ("PUT".equalsIgnoreCase(method) || "PATCH".equalsIgnoreCase(method)) {
            return "U";
        }
        return "O";
    }

    private String normalizeUri(String uri) {
        if (uri == null) {
            return "/";
        }
        String p = uri.replaceAll("/\\d+", "/:id");
        p = p.replace("/registration/", "/reg/")
                .replace("/doctor/desk/", "/doc/")
                .replace("/exam-lab/", "/exam/")
                .replace("/pharmacy/", "/pharm/")
                .replace("/module-backfill/", "/mb/")
                .replace("/finance/daily-settlement/", "/fin/");
        return p;
    }

    private String normalizeIp(String ip) {
        if (ip == null || ip.trim().isEmpty()) {
            return "unknown";
        }
        String v = ip.trim();
        if (v.startsWith("::ffff:")) {
            v = v.substring(7);
        }
        return v;
    }

    private String resolveClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.trim().isEmpty()) {
            String[] parts = xff.split(",");
            if (parts.length > 0 && !parts[0].trim().isEmpty()) {
                return parts[0].trim();
            }
        }
        String xr = request.getHeader("X-Real-IP");
        if (xr != null && !xr.trim().isEmpty()) {
            return xr.trim();
        }
        String ip = request.getRemoteAddr();
        return ip == null || ip.trim().isEmpty() ? "unknown" : ip.trim();
    }
}
