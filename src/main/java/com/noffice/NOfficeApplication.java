package com.noffice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableScheduling
public class NOfficeApplication {

	public static void main(String[] args) {
		SpringApplication.run(NOfficeApplication.class, args);
		//openSwaggerUI();
	}

	private static void openSwaggerUI() {
        String url = "http://localhost:8081/swagger-ui.html";
        try {
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
            } else if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                Runtime.getRuntime().exec("open " + url);
            } else {
                Runtime.getRuntime().exec("xdg-open " + url);
            }
        } catch (Exception e) {
            System.err.println("Không thể mở trình duyệt: " + e.getMessage());
        }
    }
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
