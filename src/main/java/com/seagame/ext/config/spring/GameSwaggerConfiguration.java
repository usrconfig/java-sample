package com.seagame.ext.config.spring;

import com.creants.creants_2x.core.util.AppConfig;
import com.creants.eventhandling.config.SwaggerConfiguration;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GameSwaggerConfiguration extends SwaggerConfiguration {
    @Bean
    public OpenAPI customOpenAPI() {
        String property = AppConfig.getProps().getProperty("game.evi");
        String key = "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiT0ZGQ0hBSU5fU1lTVEVNIiwiaXNzIjoiYXV0aDAiLCJlbnYiOiJkZXYiLCJhcHBfaWQiOiJNZXRAbnRzX2RldiJ9.RUwnUYWjjovPU-Nt4Jfn_Jv7voOlPJG42PpJmyivcFQ";
        if (property != null && property.equals("stg")) {
            key = "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiT0ZGQ0hBSU5fU1lTVEVNIiwiaXNzIjoiYXV0aDAiLCJlbnYiOiJzdGciLCJhcHBfaWQiOiJNZXRAbnRzX3N0ZyJ9.HTLx0Gszhp8Aaxu8FPGi5yYKe8ZfZ6lkjdhS-Ymb34M";
        }
        return this.createCustomOpenAPI(key);
    }

}

