package com.alibaba.arthas.tunnel.server.app;

import com.alibaba.arthas.tunnel.server.app.configuration.ArthasProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * @author hengyunabc 2021-08-11
 */
@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    ArthasProperties arthasProperties;

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.authorizeRequests()
                .antMatchers("/api/node/**").permitAll()
                .requestMatchers(EndpointRequest.toAnyEndpoint()).permitAll().and().csrf().disable();
        // allow iframe
        if (arthasProperties.isEnableIframeSupport()) {
            httpSecurity.headers().frameOptions().disable();
        }
    }
}