/*
package com.tfswx.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.util.pattern.PathPatternParser;

*/
/**
 * 网关跨域配置
 *//*

@Data
@Configuration
@ConfigurationProperties(prefix = "com.tfswx.gateway")
public class CorsConfig {
	*/
/**
	 * http options：access-control-max-age
	 *//*

	private long maxAge = 0;

	@Bean
	public CorsWebFilter corsFilter() {
		CorsConfiguration config = new CorsConfiguration();
		config.addAllowedMethod("*");
		config.addAllowedOrigin("*");
		config.addAllowedHeader("*");
		config.setAllowCredentials(true);
		if (maxAge > 0) {
			config.setMaxAge(maxAge);
		}

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource(new PathPatternParser());
		source.registerCorsConfiguration("/**", config);

		return new CorsWebFilter(source);
	}
}*/
