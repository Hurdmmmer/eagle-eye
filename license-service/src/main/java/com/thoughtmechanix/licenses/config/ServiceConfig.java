package com.thoughtmechanix.licenses.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class ServiceConfig {
    @Value("${example.property}")
    private String excfg;
}
