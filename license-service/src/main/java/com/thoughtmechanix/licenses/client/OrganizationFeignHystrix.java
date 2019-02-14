package com.thoughtmechanix.licenses.client;

import com.thoughtmechanix.licenses.model.Organization;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrganizationFeignHystrix implements OrganizationFeignClient {
    @Override
    public Organization getOrganization(String organizationId) {
        log.info("Feign 断路器执行");
        return null;
    }
}
