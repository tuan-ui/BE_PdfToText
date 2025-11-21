package com.noffice.config;

import com.google.common.net.HttpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfiguration implements WebMvcConfigurer {
	@Value("${URL.COLLABORA.OFFICE}")
	private String urlCollaboraOffice;
	@Value("${API.FE.URL}")
	private String apiFeUrl;
	@Value(value = "${API.URL}")
	private String apiUrl;
	@Value("${API.FE.URL.LOCAL}")
	private String apiFeUrlLocal;
	@Value("${API.URL.LOCAL}")
	private String apiUrlLocal;
	@Value("${API.COLLABORA.URL.DEMO}")
	private String apiCollaboraUrlDemo;
	@Value("${API.FE.URL.DEMO}")
	private String apiFeUrlDemo;

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/api/**") // Adjust the mapping pattern based on your API end points
				.allowedOriginPatterns(
						urlCollaboraOffice,
						apiFeUrl,
						apiUrl,
						apiFeUrlLocal,
						apiUrlLocal,
						apiCollaboraUrlDemo,
						apiFeUrlDemo
				) // Allow requests from any origin
				.allowedMethods("GET", "POST", "PUT", "DELETE") // Allow these HTTP methods
				.allowedHeaders("Content-Type", "Authorization") // Include Authorization header
				.allowCredentials(true)
				.exposedHeaders(HttpHeaders.CONTENT_DISPOSITION);;

	}

}
