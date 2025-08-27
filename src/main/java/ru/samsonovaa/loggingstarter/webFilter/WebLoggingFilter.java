package ru.samsonovaa.loggingstarter.webFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingResponseWrapper;

@Component
public class WebLoggingFilter extends HttpFilter {
    private static final Logger log = LoggerFactory.getLogger(WebLoggingFilter.class);

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String method = request.getMethod();
        String requestURI = request.getRequestURI() + formatQueryString(request);
        String requestHeaders = inlineRequestHeaders(request);

        log.info("Запрос: {} {} {}", method, requestURI, requestHeaders);

        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        try {
            super.doFilter(request, responseWrapper, chain);
            String responseBody = "body=" + new String(responseWrapper.getContentAsByteArray(), StandardCharsets.UTF_8);
            String responseHeaders = inlineResponseHeaders(response);

            log.info("Ответ: {} {} {} {} {}", method, requestURI, responseHeaders, response.getStatus(), responseBody);
        } finally {
            responseWrapper.copyBodyToResponse();
        }
    }

    private String inlineRequestHeaders(HttpServletRequest request) {
        Map<String, String> headersMap = Collections.list(request.getHeaderNames()).stream()
                .collect(Collectors.toMap(it -> it, request::getHeader));

        return inlineHeaders(headersMap);
    }

    private String inlineResponseHeaders(HttpServletResponse response) {
        Map<String, String> headersMap = response.getHeaderNames().stream()
                .collect(Collectors.toMap(it -> it, response::getHeader));

        return inlineHeaders(headersMap);
    }

    private String inlineHeaders(Map<String, String> headersMap) {
        String headers = headersMap.entrySet().stream()
                .map(entry -> {
                    String headerName = entry.getKey();
                    String headerValue = entry.getValue();

                    return headerName + "=" + headerValue;
                })
                .collect(Collectors.joining(","));

        return "headers={" + headers + "}";
    }

    private static String formatQueryString(HttpServletRequest request) {
        return Optional.ofNullable(request.getQueryString())
                .map(qs -> "?" + qs)
                .orElse(Strings.EMPTY);
    }
}
