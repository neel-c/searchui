/**
* Copyright 2018 Attivio Inc., All rights reserved.
*/
package com.attivio.suitback.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import com.github.ulisesbocchio.spring.boot.security.saml.bean.SAMLConfigurerBean;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {
  // Add the property logging.level.com.attivio.suitback.config.SecurityConfig
  // to the application.properties to get debug logging (e.g., with value of DEBUG).
  static final Logger LOG = LoggerFactory.getLogger(SecurityConfig.class);

  @Value("${security.saml.entityId:}")
  String entityId;
    
  @Value("${saml.sso.context-provider.lb.enabled:false}")
  boolean lbEnabled;
  
  @Autowired
  SAMLConfigurerBean samlConfigurer;
  
  @Override
  public void configure(WebSecurity web) throws Exception {
    // The REST API, the special sockjs-node URLs, and any static files are NOT to be authenticated
    web
      .ignoring()
        .antMatchers(
            "/error",
            "/loggedout",
            "/users",
            "/configuration",
            "/sockjs-node/**",
            "/log",
            "/**/*.css",
            "/**/*.ttf",
            "/**/*.eot",
            "/**/*.wof",
            "/**/*.woff2"
            ,"/**/*.svg",
            "/**/*.png",
            "/**/*.gif",
            "/**/*.ico"
           );
  }
  
  @Override
  protected void configure(HttpSecurity http) throws Exception {
    // Only configure SAML authentication if we're asked to
    if (entityId != null && entityId.length() > 0) {
      LOG.trace("Configuring the servlet with SAML authentication for entity name" + entityId);
      http
        .httpBasic()
          .disable()
        .csrf()
          .disable()
        .anonymous()
          .disable()
        .headers()
          .frameOptions()
            .sameOrigin()
        .and()
          // Any SAML-related endpoints are NOT to be authenticated
          .authorizeRequests()
            .requestMatchers(samlConfigurer.endpointsMatcher())
            .permitAll()
        .and()
          // Finally, everything else IS to be authenticated
          .authorizeRequests()
            .anyRequest()
            .authenticated();
    } else {
      // For non-SAML installations, don't do any authentication in the back end
      LOG.trace("Configuring the servlet with no authentication enalbed");
      http
        .httpBasic()
          .disable()
        .csrf()
          .disable()
        .authorizeRequests()
          .anyRequest()
          .permitAll();
    }
  }
}
