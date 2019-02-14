package com.thoughtmechanix.filters;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.thoughtmechanix.model.AbTestingRoute;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.filters.ProxyRequestHelper;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Component
public class SpecialRoutesFilter extends ZuulFilter {
    private static final int FILTER_ORDER = 1;
    private static final boolean SHOULD_FILTER = true;

    @Autowired
    FilterUtils filterUtils;
    @Autowired
    private RestTemplate restTemplate;

    @Override
    public String filterType() {
        return FilterUtils.ROUTE_FILTER_TYPE;
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

        RequestContext ctx = RequestContext.getCurrentContext();

        AbTestingRoute abRoutingInfo = getAbRoutingInfo(filterUtils.getServiceId());

        if (abRoutingInfo != null && useSpecialRoute(abRoutingInfo)) {
            String route = buildRouteString(ctx.getRequest().getRequestURI(),
                    abRoutingInfo.getEndpoint(), ctx.get("serviceId").toString());
            forwardToSpecialRoute(route);
        }
        return null;
    }

    private ProxyRequestHelper helper = new ProxyRequestHelper();

    /**
     * 获取请求方式
     */
    private String getVerb(HttpServletRequest request) {
        String sMethod = request.getMethod();
        return sMethod.toUpperCase();
    }
    /** 转发到新的服务商 */
    private void forwardToSpecialRoute(String route) {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        // 获取请求头
        MultiValueMap<String, String> headers = this.helper.buildZuulRequestHeaders(request);
        // 获取请求参数
        MultiValueMap<String, String> params = this.helper.buildZuulRequestQueryParams(request);
        // 获取请求方式
        String verb = getVerb(request);
        // 获取请求体
        InputStream requestEntity = getRequestBody(request);
        if (request.getContentLength() < 0) {
            ctx.setChunkedRequestBody();
        }
        // 构建忽略头信息
        this.helper.addIgnoredHeaders();
        CloseableHttpClient httpClient = null;
        HttpResponse response = null;
        try {
            httpClient = HttpClients.createDefault();
            response = forward(httpClient, verb, route, request, headers, params, requestEntity);
            setResponse(response);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException e) {

                }
            }
        }
    }

    private void setResponse(HttpResponse response) throws IOException {
        this.helper.setResponse(response.getStatusLine().getStatusCode(),
                response.getEntity() == null ? null : response.getEntity().getContent(),
                revertHeaders(response.getAllHeaders()));
    }
    /** 开始转发路由 */
    private HttpResponse forward(CloseableHttpClient httpClient,
                                 String verb, String uri,
                                 HttpServletRequest request,
                                 MultiValueMap<String, String> headers,
                                 MultiValueMap<String, String> params,
                                 InputStream requestEntity) throws Exception {
        ///////////////////////////////////////////组装转发参数////////////////////////////////////////////
        Map<String, Object> info = this.helper.debug(verb, uri, headers, params, requestEntity);
        URL host = new URL(uri);
        HttpHost httpHost = getHttpHost(host);

        HttpRequest httpRequest;
        int contentLength = request.getContentLength();
        InputStreamEntity entity = new InputStreamEntity(requestEntity, contentLength,
                request.getContentType() != null
                        ? ContentType.create(request.getContentType()) : null);
        switch (verb.toUpperCase()) {
            case "POST":
                HttpPost httpPost = new HttpPost(uri);
                httpRequest = httpPost;
                httpPost.setEntity(entity);
                break;
            case "PUT":
                HttpPut httpPut = new HttpPut(uri);
                httpRequest = httpPut;
                httpPut.setEntity(entity);
                break;
            case "PATCH":
                HttpPatch httpPatch = new HttpPatch(uri );
                httpRequest = httpPatch;
                httpPatch.setEntity(entity);
                break;
            default:
                httpRequest = new BasicHttpRequest(verb, uri);

        }
        //////////////////////////////////////执行转发///////////////////////////////////////////
        try {
            httpRequest.setHeaders(convertHeaders(headers));
            HttpResponse zuulResponse = forwardRequest(httpClient, httpHost, httpRequest);

            return zuulResponse;
        }
        finally {
        }
    }
    /** 执行转发  */
    private HttpResponse forwardRequest(HttpClient httpclient, HttpHost httpHost,
                                        HttpRequest httpRequest) throws IOException {
        return httpclient.execute(httpHost, httpRequest);
    }

    private MultiValueMap<String, String> revertHeaders(Header[] headers) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
        for (Header header : headers) {
            String name = header.getName();
            if (!map.containsKey(name)) {
                map.put(name, new ArrayList<String>());
            }
            map.get(name).add(header.getValue());
        }
        return map;
    }

    private HttpHost getHttpHost(URL host) {
        HttpHost httpHost = new HttpHost(host.getHost(), host.getPort(),
                host.getProtocol());
        return httpHost;
    }

    private Header[] convertHeaders(MultiValueMap<String, String> headers) {
        List<Header> list = new ArrayList<>();
        for (String name : headers.keySet()) {
            for (String value : headers.get(name)) {
                list.add(new BasicHeader(name, value));
            }
        }
        return list.toArray(new BasicHeader[0]);
    }

    /**
     * get request body
     */
    private InputStream getRequestBody(HttpServletRequest request) {
        InputStream requestEntity = null;
        try {
            requestEntity = request.getInputStream();
        } catch (IOException ex) {
            // no requestBody is ok.
        }
        return requestEntity;
    }

    private String buildRouteString(String oldEndpoint, String newEndpoint, String serviceName) {
        int index = oldEndpoint.indexOf(serviceName);

        String strippedRoute = oldEndpoint.substring(index + serviceName.length());
        System.out.println("Target route: " + String.format("%s/%s", newEndpoint, strippedRoute));
        return String.format("%s/%s", newEndpoint, strippedRoute);
    }
    /** 调用远程服务获取新的服务路由 */
    private AbTestingRoute getAbRoutingInfo(String serviceName) {
        ResponseEntity<AbTestingRoute> restExchange = null;
        try {
            restExchange = restTemplate.exchange("http://specialroutesservice/v1/route/abtesting/{serviceName}",
                    HttpMethod.GET, null, AbTestingRoute.class, serviceName);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return null;
            }
            throw e;
        }

        return restExchange.getBody();
    }

    /**
     * 判断是否路由到新的服务
     */
    private boolean useSpecialRoute(AbTestingRoute testingRoute) {
        Random random = new Random();

        if (testingRoute.getActive().equalsIgnoreCase("n")) {
            return false;
        }
        int value = random.nextInt((10 - 1) + 1) + 1;
        if (testingRoute.getWeight() < value)
            return true;
        return false;
    }
}
