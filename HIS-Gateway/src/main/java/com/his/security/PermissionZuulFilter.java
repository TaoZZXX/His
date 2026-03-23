package com.his.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.his.domain.Result;
import com.his.enums.ResultCode;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import com.his.redis.PermissionUrlRedisCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class PermissionZuulFilter extends ZuulFilter {

    private static final Logger log = LoggerFactory.getLogger(PermissionZuulFilter.class);

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final SecretKey secretKey;
    private final PermissionUrlRedisCache permissionUrlRedisCache;
    private final Set<Long> adminRoleIds = new HashSet<>();

    @Autowired
    public PermissionZuulFilter(JdbcTemplate jdbcTemplate,
                                 ObjectMapper objectMapper,
                                 PermissionUrlRedisCache permissionUrlRedisCache,
                                 @Value("${jwt.secret:his@2026#JwtSecretKey_1234567890_abcdefg}") String jwtSecret,
                                 @Value("${permission.admin.role-ids:}") String adminRoleIdsStr) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
        this.permissionUrlRedisCache = permissionUrlRedisCache;
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        parseAdminRoleIds(adminRoleIdsStr);
    }

    private void parseAdminRoleIds(String adminRoleIdsStr) {
        if (adminRoleIdsStr == null || adminRoleIdsStr.trim().isEmpty()) {
            return;
        }
        // 支持逗号分隔：例如 "1,2,3"
        String[] parts = adminRoleIdsStr.split(",");
        for (String p : parts) {
            if (p == null) continue;
            String s = p.trim();
            if (s.isEmpty()) continue;
            try {
                adminRoleIds.add(Long.parseLong(s));
            } catch (Exception e) {
                // ignore invalid id
                log.warn("invalid admin role id config: {}", s);
            }
        }
    }

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        // 越小越先执行
        return 0;
    }

    @Override
    public boolean shouldFilter() {
        RequestContext ctx = RequestContext.getCurrentContext();
        String requestUri = ctx.getRequest().getRequestURI();
        if (requestUri == null) {
            return false;
        }

        // 只拦截网关路由到后端的接口
        if (!requestUri.startsWith("/sms/")) {
            return false;
        }

        // 放行跨域预检
        String method = ctx.getRequest().getMethod();
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return false;
        }

        // 放行登录/注册，不做权限校验（token 也不要求）
        return !isAllowWithoutAuth(requestUri);
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        String requestUri = ctx.getRequest().getRequestURI();
        String method = ctx.getRequest().getMethod();

        // 网关只处理 /sms/** 的接口
        String requestPath = normalizePath(requestUri);
        String strippedPath = stripSmsPrefix(requestPath);
        String token = getToken(ctx, requestUri, method);
        if (token == null || token.trim().isEmpty()) {
            log.debug("deny: missing/invalid token. requestPath={}", requestPath);
            return deny(ctx, ResultCode.TOKEN_INVALID, "token 无效或缺失");
        }

        Claims claims;
        try {
            claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            return deny(ctx, ResultCode.TOKEN_INVALID, "token 无效");
        }

        Long userId = claims.get("userId", Long.class);
        if (userId == null) {
            log.debug("deny: token missing userId. requestPath={}", requestPath);
            return deny(ctx, ResultCode.TOKEN_INVALID, "token 无效");
        }

        Long roleId;
        try {
            roleId = jdbcTemplate.queryForObject(
                    "select role_id from sms_staff where id = ?",
                    Long.class,
                    userId
            );
        } catch (Exception e) {
            log.debug("deny: failed to resolve roleId. userId={}. requestPath={}", userId, requestPath);
            return deny(ctx, ResultCode.USER_NOT_EXIST, "用户不存在");
        }

        if (roleId == null) {
            log.debug("deny: roleId null. userId={}. requestPath={}", userId, requestPath);
            return deny(ctx, ResultCode.USER_NOT_EXIST, "用户不存在");
        }

        // 管理员角色（sms_role）不可删除：拦截 DELETE /sms/role/roles/{id}
        if ("DELETE".equalsIgnoreCase(method)) {
            Long targetRoleId = extractTargetRoleIdForDelete(requestPath);
            if (targetRoleId != null && adminRoleIds.contains(targetRoleId)) {
                log.debug("deny: admin role delete blocked. targetRoleId={} requestPath={}", targetRoleId, requestPath);
                return deny(ctx, ResultCode.PERMISSION_DENIED, "管理员角色不可删除");
            }
        }

        // 管理员角色直接放行：不依赖 sms_permission 表
        if (adminRoleIds.contains(roleId)) {
            log.debug("admin bypass: roleId={} requestPath={}", roleId, requestPath);
            return null;
        }

        // staff/info、staff/logout 只要求登录有效，不做权限校验
        if ("/sms/staff/info".equals(requestPath) || "/sms/staff/info/".equals(requestUri)
                || "/sms/staff/logout".equals(requestPath) || "/sms/staff/logout/".equals(requestUri)) {
            return null;
        }

        // 检查结果图片：GET 需带 token（Header 或 URL 参数，便于 img 标签 src）
        if ("GET".equalsIgnoreCase(method)) {
            String p = normalizePath(requestPath);
            if (p.startsWith("/sms/exam-result-files/")) {
                return null;
            }
        }
        // 检查结果图上传：已登录即可（细粒度权限可在 sms_permission 中再收紧）
        if ("POST".equalsIgnoreCase(method)) {
            String p = normalizePath(requestPath);
            if ("/sms/exam-lab/result-images/upload".equals(p)) {
                return null;
            }
        }

        // 药房工作台：已登录即可（与收费处缴费后衔接）
        if (requestPath.startsWith("/sms/pharmacy/")) {
            return null;
        }

        // 门诊财务日结：已登录即可（细粒度权限可在 sms_permission 中再收紧）
        if (requestPath.startsWith("/sms/finance/")) {
            return null;
        }

        // 首页仪表盘：已登录即可
        if (requestPath.startsWith("/sms/home/")) {
            return null;
        }

        // 科室维护：已登录即可（含 POST /sms/dept）
        if (requestPath.startsWith("/sms/dept")) {
            return null;
        }

        Set<String> permissionUrls = getPermissionUrls(roleId);
        if (permissionUrls == null || permissionUrls.isEmpty()) {
            log.debug("deny: empty permissions. roleId={}. requestPath={}", roleId, requestPath);
            return deny(ctx, ResultCode.PERMISSION_DENIED, "无权限访问");
        }

        boolean allowed = isAllowed(permissionUrls, requestPath, strippedPath);
        log.debug("perm check: roleId={} requestPath={} strippedPath={} allowed={} permissionCount={}",
                roleId, requestPath, strippedPath, allowed, permissionUrls.size());
        if (!allowed) {
            return deny(ctx, ResultCode.PERMISSION_DENIED, "无权限访问");
        }

        // 允许继续路由到后端
        return null;
    }

    private boolean isAllowWithoutAuth(String requestUri) {
        // 兼容带尾斜杠
        String p = normalizePath(requestUri);
        return "/sms/staff/login".equals(p)
                || "/sms/staff/register".equals(p);
    }

    private Long extractTargetRoleIdForDelete(String requestPath) {
        if (requestPath == null) {
            return null;
        }
        String prefix = "/sms/role/roles/";
        if (!requestPath.startsWith(prefix)) {
            return null;
        }
        String tail = requestPath.substring(prefix.length());
        if (tail == null || tail.trim().isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(tail);
        } catch (Exception e) {
            return null;
        }
    }

    private String getToken(RequestContext ctx, String requestUri, String method) {
        HttpServletRequest req = ctx.getRequest();
        if ("GET".equalsIgnoreCase(method)) {
            String p = normalizePath(requestUri);
            if (p.startsWith("/sms/exam-result-files/")) {
                String q = req.getParameter("token");
                if (q != null && !q.trim().isEmpty()) {
                    return q.trim();
                }
            }
        }
        String token = req.getHeader("X-Token");
        if (token != null && !token.trim().isEmpty()) {
            return token;
        }

        String authorization = req.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String bearer = authorization.substring("Bearer ".length());
            return bearer.trim().isEmpty() ? null : bearer.trim();
        }

        return null;
    }

    private boolean isAllowed(Set<String> permissionUrls, String requestPath, String strippedPath) {
        for (String raw : permissionUrls) {
            if (raw == null) {
                continue;
            }
            String permUrl = normalizePermissionUrl(raw);
            if (permUrl.isEmpty()) {
                continue;
            }

            if ("*".equals(permUrl)) {
                return true;
            }

            if (permUrl.equals(requestPath) || permUrl.equals(strippedPath)) {
                return true;
            }

            // 支持通配符匹配：例如 /sms/role/permissions/*
            if (permUrl.contains("*")) {
                if (matchesGlob(permUrl, requestPath) || matchesGlob(permUrl, strippedPath)) {
                    return true;
                }
                continue;
            }

            // 支持前缀匹配：例如 /sms/role/roles
            if (requestPath.startsWith(permUrl + "/") || requestPath.equals(permUrl)) {
                return true;
            }
            if (strippedPath.startsWith(permUrl + "/") || strippedPath.equals(permUrl)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesGlob(String glob, String value) {
        // glob: '*' 表示任意字符段
        String regex = globToRegex(glob);
        return value != null && value.matches(regex);
    }

    private String globToRegex(String glob) {
        StringBuilder sb = new StringBuilder();
        sb.append("^");
        for (int i = 0; i < glob.length(); i++) {
            char c = glob.charAt(i);
            if (c == '*') {
                sb.append(".*");
                continue;
            }
            // escape regex special chars
            if (c == '.' || c == '^' || c == '$' || c == '+' || c == '?' || c == '{' || c == '}' ||
                    c == '(' || c == ')' || c == '[' || c == ']' || c == '|' || c == '\\') {
                sb.append("\\");
            }
            sb.append(c);
        }
        sb.append("$");
        return sb.toString();
    }

    private Set<String> getPermissionUrls(Long roleId) {
        if (roleId == null) {
            return new HashSet<>();
        }

        long ver = permissionUrlRedisCache.getPermissionCacheVersion();

        // 1) try redis
        try {
            String cachedJson = permissionUrlRedisCache.getCachedPermissionUrlsJson(roleId, ver);
            if (cachedJson != null && !cachedJson.trim().isEmpty()) {
                List<String> list = objectMapper.readValue(cachedJson, new TypeReference<List<String>>() {});
                return new HashSet<>(list);
            }
        } catch (Exception e) {
            log.warn("redis get permissionUrls failed, roleId={}", roleId, e);
        }

        // 2) query db
        List<String> urls = jdbcTemplate.queryForList(
                "select p.url " +
                        "from sms_permission p " +
                        "inner join sms_role_permission_relation r on p.id = r.permission_id " +
                        "where r.role_id = ? " +
                        "  and p.url is not null " +
                        "  and p.url <> '' " +
                        "  and (p.status is null or p.status > 0)",
                String.class,
                roleId
        );

        Set<String> result = new HashSet<>();
        if (urls != null) {
            result.addAll(urls);
        }

        // 3) set redis (best-effort)
        try {
            List<String> list = new ArrayList<>(result);
            String json = objectMapper.writeValueAsString(list);
            permissionUrlRedisCache.cachePermissionUrlsJson(roleId, ver, json);
        } catch (Exception e) {
            log.warn("redis set permissionUrls failed, roleId={}", roleId, e);
        }

        return result;
    }

    private Object deny(RequestContext ctx, ResultCode code, String message) {
        Result<Object> result = Result.error(code, message);
        String body;
        try {
            body = objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            body = "{\"code\":" + code.getCode() + ",\"status\":\"" + code.getStatus() + "\",\"message\":\"" + message + "\"}";
        }

        ctx.setSendZuulResponse(false);
        ctx.setResponseStatusCode(HttpServletResponse.SC_OK); // 前端只看 Result.code，不靠 HTTP 状态码
        ctx.setResponseBody(body);
        ctx.getResponse().setContentType("application/json;charset=UTF-8");
        return null;
    }

    private String normalizePath(String path) {
        if (path == null) {
            return "";
        }
        String p = path.trim();
        // 去掉结尾斜杠（除非就是 "/"）
        if (p.length() > 1 && p.endsWith("/")) {
            p = p.substring(0, p.length() - 1);
        }
        return p;
    }

    private String stripSmsPrefix(String path) {
        if (path == null) {
            return "";
        }
        if (path.startsWith("/sms/")) {
            return path.substring(4); // "/sms" -> keep leading "/"
        }
        return path;
    }

    private String normalizePermissionUrl(String url) {
        if (url == null) {
            return "";
        }
        String u = url.trim();
        if (u.isEmpty()) {
            return "";
        }
        if (!u.startsWith("/")) {
            u = "/" + u;
        }
        if (u.length() > 1 && u.endsWith("/")) {
            u = u.substring(0, u.length() - 1);
        }
        return u;
    }

    // role permission URLs are cached in Redis
}

