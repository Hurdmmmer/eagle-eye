package com.thoughtmechanix.licenses.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Component
@Slf4j
public class UserContextFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;

        UserContextHolder.getContext().setCorrelationId(request.getHeader(UserContext.CORRELATION_ID));

        UserContextHolder.getContext().setUserId(request.getHeader(UserContext.USER_ID));
        UserContextHolder
                .getContext()
                .setAuthToken(request.getHeader(UserContext.AUTH_TOKEN));
        UserContextHolder
                .getContext()
                .setOrgId(request.getHeader(UserContext.ORG_ID));

        filterChain.doFilter(request, servletResponse);
    }

    @Override
    public void destroy() {

    }
}
