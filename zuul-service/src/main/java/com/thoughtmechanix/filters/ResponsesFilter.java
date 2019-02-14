package com.thoughtmechanix.filters;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.logging.Filter;

@Component
@Slf4j
public class ResponsesFilter extends ZuulFilter {

    private final static int FILTER_ORDER = 1;
    private final static String FILTER_TYPE = "post";
    private final static boolean SHOULD_FILTER = true;

    @Autowired
    private FilterUtils filterUtils;

    @Override
    public String filterType() {
        return FilterUtils.POST_FILTER_TYPE;
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
        RequestContext currentContext = RequestContext.getCurrentContext();
        log.info("Adding the correlation id to the outbound headers. {}", filterUtils.getCorrelationId());
        currentContext.getResponse().addHeader(FilterUtils.CORRELATION_ID, filterUtils.getCorrelationId());

        log.info("Completing outgoing request for {}.", currentContext.getRequest().getRequestURI());
        return null;
    }
}
