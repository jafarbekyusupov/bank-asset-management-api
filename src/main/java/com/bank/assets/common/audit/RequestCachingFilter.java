package com.bank.assets.common.audit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;

// wrapper for non-multipart reqs so body can be read multiple times
// by controller and auditInterceptor
@Component
public class RequestCachingFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain chain
    ) throws ServletException, IOException {
        String contentType = request.getContentType();
        boolean isMultipart = contentType != null && contentType.startsWith("multipart/");
        if (isMultipart) {
            chain.doFilter(request, response);
        } else {
            chain.doFilter(new ContentCachingRequestWrapper(request), response);
        }
    }
}
