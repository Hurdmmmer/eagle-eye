package com.thoughtmechanix.licenses.services;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import com.thoughtmechanix.licenses.client.OrganizationDiscoveryClient;
import com.thoughtmechanix.licenses.client.OrganizationFeignClient;
import com.thoughtmechanix.licenses.client.OrganizationRestTemplateClient;
import com.thoughtmechanix.licenses.config.ServiceConfig;
import com.thoughtmechanix.licenses.model.License;
import com.thoughtmechanix.licenses.model.Organization;
import com.thoughtmechanix.licenses.repository.LicenseRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
@Slf4j
public class LicenseService {

    @Autowired
    private LicenseRepository licenseRepository;

    @Autowired
    private ServiceConfig serviceConfig;

    @Autowired
    OrganizationFeignClient organizationFeignClient;

    @Autowired
    OrganizationRestTemplateClient organizationRestClient;

    @Autowired
    OrganizationDiscoveryClient organizationDiscoveryClient;
    @Autowired
    private ServiceConfig config;

    public void saveLicense(License license) {
        license.setLicenseId(UUID.randomUUID().toString());
        licenseRepository.save(license);

    }

    public License getLicense(String organizationId, String licenseId) {
        License license = licenseRepository.findByOrganizationIdAndLicenseId(organizationId, licenseId);
        license.setComment(serviceConfig.getExcfg());
        return license;
    }

    /**
     * 使用断路器管理该方法的调用， 超过一定时间的延迟将会抛出异常状态
     */

    @HystrixCommand( fallbackMethod = "buildFallbackLicenseList",  // fallbackMethod 使用断路器
//            commandProperties = {
              // 超时策略 默认 1000ms
//            @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "12000")
//    }

            threadPoolKey = "licensesByOrgThreadPool",
            threadPoolProperties = {
                    // 设置线程池的线程数量
                    @HystrixProperty(name = "coreSize", value = "30"),
                    // 设置线程池满了，最大可缓存的请求数量， 超过则拒绝服务
                    @HystrixProperty(name = "maxQueueSize", value = "10")
            },
            commandProperties = {
                    @HystrixProperty(
                            // 设置在执行断路器时，允许一个时间片内，错误的阈值，执行断路器
                            name = "circuitBreaker.requestVolumeThreshold",
                            value = "1"),
                    @HystrixProperty(
                            // 错误比率阀值，如果错误率>=该值，circuit会被打开，并短路所有请求触发fallback。默认50
                            name = "circuitBreaker.errorThresholdPercentage",
                            value = "20"),
                    @HystrixProperty(
                            // 设置执行断路器后，允许另外的调用通过来看服务是否恢复健康前 Hystrix 将休眠的时间
                            name = "circuitBreaker.sleepWindowInMilliseconds",
                            value = "7000"),
                    @HystrixProperty(
                            // 设置一个窗口的时间（一个时间片的时间），默认10秒
                            name = "metrics.rollingStats.timeInMilliseconds",
                            value = "15000"),
                    @HystrixProperty(
                            // 设置时间片内，收集统计桶的数量
                            // 改示例中， 我们收集时间段15秒中收集5个长度为3秒的桶,统计失败次数
                            name = "metrics.rollingStats.numBuckets",
                            value = "5")}
    )
    public List<License> getLicensesByOrg(String organizationId) {
        log.info("开始执行。。。");
        randomlyRunLong();
        return licenseRepository.findByOrganizationId(organizationId);
    }


    private List<License> buildFallbackLicenseList(String organizationId) {
        List<License> fallbackList = new ArrayList<>();
        License license = new License()
                .withId("0000000-00-00000")
                .withOrganizationId(organizationId)
                .withProductName(
                        "Sorry no licensing information currently available");
        fallbackList.add(license);
        return fallbackList;
    }

    public License getLicense(String organizationId, String licenseId, String clientType) {
        License license = licenseRepository.findByOrganizationIdAndLicenseId(organizationId, licenseId);

        Organization org = retrieveOrgInfo(organizationId, clientType);

        return license
                .withOrganizationName(org.getName())
                .withContactName(org.getContactName())
                .withContactEmail(org.getContactEmail())
                .withContactPhone(org.getContactPhone())
                .withComment(config.getExcfg());
    }

    private Organization retrieveOrgInfo(String organizationId, String clientType) {
        Organization organization = null;

        switch (clientType) {
            case "feign":
                System.out.println("I am using the feign client");
                organization = organizationFeignClient.getOrganization(organizationId);
                break;
            case "rest":
                System.out.println("I am using the rest client");
                organization = organizationRestClient.getOrganization(organizationId);
                break;
            case "discovery":
                System.out.println("I am using the discovery client");
                organization = organizationDiscoveryClient.getOrganization(organizationId);
                break;
            default:
                organization = organizationRestClient.getOrganization(organizationId);
        }

        return organization;
    }


    public void updateLicense(License license) {
        licenseRepository.save(license);
    }

    public void deleteLicense(License license) {
        licenseRepository.deleteByLicenseId(license.getLicenseId());
    }

    private void randomlyRunLong() {
        Random rand = new Random();

        int randomNum = rand.nextInt((3 - 1) + 1) + 1;
        if (randomNum == 3){
            log.info("延迟 11 秒");
            sleep();
        }
    }

    private void sleep() {
        try {
            Thread.sleep(11000);
        } catch (InterruptedException e) {
//            e.printStackTrace();
        }
    }
}
