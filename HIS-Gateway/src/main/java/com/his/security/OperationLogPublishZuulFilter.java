package com.his.security;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class OperationLogPublishZuulFilter extends ZuulFilter {

    private final RabbitTemplate rabbitTemplate;

    public OperationLogPublishZuulFilter(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public String filterType() {
        return "post";
    }

    @Override
    public int filterOrder() {
        return 100;
    }

    @Override
    public boolean shouldFilter() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest req = ctx.getRequest();
        if (req == null) {
            return false;
        }
        String path = normalizePath(req.getRequestURI());
        if (!path.startsWith("/sms/")) {
            return false;
        }
        if ("/sms/staff/login".equals(path) || "/sms/staff/register".equals(path)) {
            return false;
        }
        String method = req.getMethod();
        return "POST".equalsIgnoreCase(method)
                || "PUT".equalsIgnoreCase(method)
                || "PATCH".equalsIgnoreCase(method)
                || "DELETE".equalsIgnoreCase(method);
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest req = ctx.getRequest();
        if (req == null) {
            return null;
        }
        try {
            Long userId = getUserId(ctx);
            if (userId == null) {
                return null;
            }
            String method = req.getMethod();
            String path = normalizePath(req.getRequestURI());
            String opType = toOpType(method);
            int status = ctx.getResponseStatusCode();
            String ip = resolveClientIp(req);

            Map<String, Object> event = new HashMap<>();
            event.put("userId", userId);
            event.put("createTime", LocalDateTime.now().toString());
            event.put("ip", "OP|" + opType + "|" + method + " " + path + "|status=" + status + "|ip=" + ip);

            MessagePostProcessor addMessageId = message -> {
                message.getMessageProperties().setMessageId(UUID.randomUUID().toString());
                return message;
            };
            rabbitTemplate.convertAndSend(
                    "his.auth.login-log.exchange",
                    "his.auth.operation-log",
                    event,
                    addMessageId
            );
        } catch (Exception ignored) {
        }
        return null;
    }

    private Long getUserId(RequestContext ctx) {
        Object val = ctx.get("currentUserId");
        if (val instanceof Long) {
            return (Long) val;
        }
        if (val instanceof Integer) {
            return ((Integer) val).longValue();
        }
        if (val instanceof String) {
            try {
                return Long.parseLong((String) val);
            } catch (Exception ignored) {
                return null;
            }
        }
        return null;
    }

    private String resolveClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (isBlank(ip)) ip = request.getHeader("X-Real-IP");
        if (isBlank(ip)) ip = request.getRemoteAddr();
        return isBlank(ip) ? "unknown" : ip;
    }

    private String toOpType(String method) {
        if ("POST".equalsIgnoreCase(method)) return "C";
        if ("PUT".equalsIgnoreCase(method) || "PATCH".equalsIgnoreCase(method)) return "U";
        if ("DELETE".equalsIgnoreCase(method)) return "D";
        return "O";
    }

    private String normalizePath(String path) {
        if (path == null) return "";
        String p = path.trim();
        if (p.length() > 1 && p.endsWith("/")) {
            p = p.substring(0, p.length() - 1);
        }
        return p;
    }

    private boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }
}
