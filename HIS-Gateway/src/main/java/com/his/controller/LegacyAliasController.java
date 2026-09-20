package com.his.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

@RestController
public class LegacyAliasController {

    private final RestTemplate restTemplate;

    @Value("${legacy.alias.auth-base-url:http://localhost:8082}")
    private String authBaseUrl;

    @Value("${legacy.alias.registration-base-url:http://localhost:8083}")
    private String registrationBaseUrl;

    public LegacyAliasController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @RequestMapping(value = "/staff/logout", method = {RequestMethod.POST, RequestMethod.GET})
    public ResponseEntity<byte[]> logoutAlias(HttpServletRequest request, @RequestBody(required = false) byte[] body) {
        return proxy(request, body, trim(authBaseUrl) + "/staff/logout");
    }

    @RequestMapping(value = "/registration/availability", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<byte[]> availabilityAlias(HttpServletRequest request, @RequestBody(required = false) byte[] body) {
        return proxy(request, body, trim(registrationBaseUrl) + "/registration/availability");
    }

    private ResponseEntity<byte[]> proxy(HttpServletRequest request, byte[] body, String targetUrl) {
        String qs = request.getQueryString();
        String full = targetUrl + (qs == null || qs.isEmpty() ? "" : "?" + qs);
        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> names = request.getHeaderNames();
        while (names != null && names.hasMoreElements()) {
            String name = names.nextElement();
            if ("host".equalsIgnoreCase(name) || "content-length".equalsIgnoreCase(name)) {
                continue;
            }
            Enumeration<String> vals = request.getHeaders(name);
            while (vals != null && vals.hasMoreElements()) {
                headers.add(name, vals.nextElement());
            }
        }
        HttpMethod method = HttpMethod.resolve(request.getMethod());
        if (method == null) {
            method = HttpMethod.GET;
        }
        ResponseEntity<byte[]> res = restTemplate.exchange(full, method, new HttpEntity<>(body, headers), byte[].class);
        return ResponseEntity.status(res.getStatusCode()).headers(res.getHeaders()).body(res.getBody());
    }

    private static String trim(String url) {
        if (url == null || url.isEmpty()) {
            return "";
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
