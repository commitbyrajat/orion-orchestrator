package com.example.msme.config;

import java.awt.GraphicsEnvironment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class HsqlConsoleConfig {

  @Bean
  ApplicationRunner hsqlConsoleRunner(
      @Value("${app.hsqldb.console.enabled:false}") boolean enabled,
      @Value("${spring.datasource.url}") String jdbcUrl,
      @Value("${spring.datasource.username}") String username) {
    return args -> {
      if (!enabled) {
        log.info("HSQL console is disabled. Set app.hsqldb.console.enabled=true for local GUI console usage.");
        return;
      }
      if (GraphicsEnvironment.isHeadless()) {
        log.warn("HSQL console requested but the JVM is headless; use Swagger or curl instead.");
        return;
      }
      Class<?> manager = Class.forName("org.hsqldb.util.DatabaseManagerSwing");
      manager.getMethod("main", String[].class)
          .invoke(null, (Object) new String[] {"--url", jdbcUrl, "--user", username});
    };
  }
}
