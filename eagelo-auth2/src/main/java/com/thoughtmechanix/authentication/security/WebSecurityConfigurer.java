package com.thoughtmechanix.authentication.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * 拓展 {@link WebSecurityConfigurerAdapter} 定义用户ID 密码 角色
 */
@Configuration
public class WebSecurityConfigurer extends WebSecurityConfigurerAdapter {

    /**
     * ②Spring Security 使用 AuthenticationManagerBean 来处理身份验证。
     * @return
     * @throws Exception
     */
    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception{
        return super.authenticationManagerBean();
    }

    /**
     * Spring Security 使 用 UserDetailsService 来 处 理 Spring
     * Security 返回的用户信息
     * @return
     * @throws Exception
     */
    @Override
    @Bean
    public UserDetailsService userDetailsServiceBean() throws Exception {
        return super.userDetailsServiceBean();
    }

    /***
     *  定义一个用户
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
                .withUser("john")
                .password("123")
                .roles("USER")
                .and()
                .withUser("william")
                .password("123")
                .roles("ADMIN", "USER");
    }
}
