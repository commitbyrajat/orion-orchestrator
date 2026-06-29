package com.example.msme.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  OpenAPI msmeOpenApi() {
    return new OpenAPI()
        .info(new Info()
            .title("MSME Overdraft Eligibility API")
            .version("0.0.1")
            .description("Simulation of MSME onboarding, GST discovery, account analysis, and overdraft eligibility scoring.")
            .license(new License().name("Apache-2.0").url("https://www.apache.org/licenses/LICENSE-2.0")));
  }
}
