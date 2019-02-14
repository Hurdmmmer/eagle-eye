package com.thoughtmechanix.filters;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class TrackingFilter extends ZuulFilter {

    private static final int FILTER_ORDER = 1;
    private static final boolean SHOULD_FILTER = true;

    @Autowired
    private FilterUtils filterUtils;

    @Override
    public String filterType() {
        return FilterUtils.PRE_FILTER_TYPE;
    }

    @Override
    public int filterOrder() {
        return FILTER_ORDER;
    }

    @Override
    public boolean shouldFilter() {
        return SHOULD_FILTER;
    }

    @Override
    public Object run() throws ZuulException {
        if (isCorrelationIdPresent()) {
            log.info("tmx-correlation-id found in tracking filter: {}", filterUtils.getCorrelationId());
        } else {
            filterUtils.setCorrelationId(generateCorrelationId());
            log.info("tmx-correlation-id generated in tracking filter: {}", filterUtils.getCorrelationId());
        }
        RequestContext currentContext = RequestContext.getCurrentContext();
        log.info("Processing incoming request for: {}", currentContext.getRequest().getRequestURI());
        return null;
    }
    /**
     * Check correlation id exists
     * */
    private boolean isCorrelationIdPresent(){
        if (filterUtils.getCorrelationId() !=null){
            return true;
        }
        return false;
    }

    private String generateCorrelationId() {
        return UUID.randomUUID().toString();
    }
}
