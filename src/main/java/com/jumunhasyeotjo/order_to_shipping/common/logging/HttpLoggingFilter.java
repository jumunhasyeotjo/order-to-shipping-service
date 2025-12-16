package com.jumunhasyeotjo.order_to_shipping.common.logging;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class HttpLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {

        ContentCachingRequestWrapper cachedRequest =
            new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper cachedResponse =
            new ContentCachingResponseWrapper(response);

        long start = System.currentTimeMillis();

        try {
            filterChain.doFilter(cachedRequest, cachedResponse);
        } finally {
            long duration = System.currentTimeMillis() - start;

            logRequest(cachedRequest);
            logResponse(cachedResponse, duration);

            cachedResponse.copyBodyToResponse();
        }
    }

    private void logRequest(ContentCachingRequestWrapper request) {
        String body = getBody(request.getContentAsByteArray());

        log.info(
            "HTTP REQUEST | method={} uri={} query={} body={}",
            request.getMethod(),
            request.getRequestURI(),
            request.getQueryString(),
            mask(body)
        );
    }

    private void logResponse(
        ContentCachingResponseWrapper response,
        long duration
    ) {
        String body = getBody(response.getContentAsByteArray());

        log.info(
            "HTTP RESPONSE | status={} duration={}ms body={}",
            response.getStatus(),
            duration,
            mask(body)
        );
    }

    private String getBody(byte[] content) {
        if (content == null || content.length == 0) return "";
        return new String(content, StandardCharsets.UTF_8);
    }

    private String mask(String body) {
        if (body == null) return null;
        return body
            .replaceAll("\"password\"\\s*:\\s*\".*?\"", "\"password\":\"***\"")
            .replaceAll("\"cardNumber\"\\s*:\\s*\".*?\"", "\"cardNumber\":\"****\"");
    }
}