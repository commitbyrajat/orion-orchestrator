package com.example.wealth.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  OpenAPI wealthManagementOpenApi() {
    return new OpenAPI()
        .info(new Info()
            .title("Wealth Management Mutual Fund API")
            .version("0.0.1")
            .description("""
                Demonstration wealth management API backed by an in-memory HSQLDB database.
                It exposes mutual fund discovery, investor portfolio summaries, and investment simulation endpoints.
                """)
            .contact(new Contact()
                .name("Orloj DIY Examples")
                .url("https://github.com")
                .email("support@example.com"))
            .license(new License()
                .name("Apache-2.0")
                .url("https://www.apache.org/licenses/LICENSE-2.0")));
  }
}
