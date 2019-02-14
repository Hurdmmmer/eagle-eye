package com.thoughtmechanix.licenses;

import com.thoughtmechanix.licenses.utils.UserContextInterceptor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

/**
 * ①因为我们在示例中使用多个客户端类型，所以我将在代码
 * 中包含它们。
 * 然而，应用程序不需要
 * @EnableDiscoveryClient 和 @EnableFeignClients ，
 * 当 使 用 支 持 Ribbon 的 RestTemplate 时可以删除。
 * */
@SpringBootApplication

@EnableDiscoveryClient   // 使用 DiscoveryClient 要开启的注解, 不使用时， 可以删除
@EnableFeignClients      // 使用 FeignClient 要开启的注解
@EnableEurekaClient
@EnableHystrix           // 开启断路器
@EnableCircuitBreaker    // 开启断路器
@EnableHystrixDashboard
public class LicensesServiceApplication {

    // 使用 RestTemplate 要注册的Bean @LoadBalanced 使用负载均衡
    @LoadBalanced
    @Bean
    public RestTemplate getRestTemplate(){
        RestTemplate restTemplate = new RestTemplate();
        // 将定义的 拦截器注入到 RestTemplate 中
        // 那么每次访问请求时都会执行拦截器
        List<ClientHttpRequestInterceptor> interceptors = restTemplate.getInterceptors();
        if (CollectionUtils.isEmpty(interceptors)) {
            restTemplate.setInterceptors(Collections.singletonList(new UserContextInterceptor()));
        } else {
            interceptors.add(new UserContextInterceptor());
            restTemplate.setInterceptors(interceptors);
        }
        return restTemplate;
    }

    public static void main(String[] args) {
        SpringApplication.run(LicensesServiceApplication.class, args);
    }
}
