package com.noffice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class SwaggerConfigTest {

    private Environment environment;
    private SwaggerConfig swaggerConfig;

    @BeforeEach
    void setup() {
        environment = Mockito.mock(Environment.class);
        swaggerConfig = new SwaggerConfig(environment);
    }

    @Test
    void testCustomOpenAPI_WithMultipleServers() {
        // Mock giá trị trong properties
        when(environment.getProperty("swagger.servers", ""))
                .thenReturn("https://api1.com;Dev Server,https://api2.com;Prod Server");

        OpenAPI openAPI = swaggerConfig.customOpenAPI();

        assertNotNull(openAPI);

        // Kiểm tra thông tin INFO
        assertEquals("NOffice API", openAPI.getInfo().getTitle());
        assertEquals("05-June-2025 13:45", openAPI.getInfo().getVersion());

        // Kiểm tra servers parse đúng
        List<Server> servers = openAPI.getServers();
        assertEquals(2, servers.size());

        assertEquals("https://api1.com", servers.get(0).getUrl());
        assertEquals("Dev Server", servers.get(0).getDescription());

        assertEquals("https://api2.com", servers.get(1).getUrl());
        assertEquals("Prod Server", servers.get(1).getDescription());

        // Kiểm tra SecurityScheme
        assertNotNull(openAPI.getComponents().getSecuritySchemes().get("bearerAuth"));

        // Kiểm tra SecurityRequirement
        assertFalse(openAPI.getSecurity().isEmpty());
        assertTrue(openAPI.getSecurity().get(0).containsKey("bearerAuth"));
    }

    @Test
    void testCustomOpenAPI_NoServers() {
        // Không có property swagger.servers
        when(environment.getProperty("swagger.servers", ""))
                .thenReturn("");

        OpenAPI openAPI = swaggerConfig.customOpenAPI();

        assertNotNull(openAPI);

        // Không có server nào
        assertTrue(openAPI.getServers().isEmpty());

        // Kiểm tra SecurityScheme tồn tại
        assertNotNull(openAPI.getComponents().getSecuritySchemes().get("bearerAuth"));
    }

    @Test
    void testCustomOpenAPI_InvalidServerFormat_Ignored() {
        // 1 server đúng format, 1 server sai format
        when(environment.getProperty("swagger.servers", ""))
                .thenReturn("https://api.com;Valid Server,invalid_format_server");

        OpenAPI openAPI = swaggerConfig.customOpenAPI();

        List<Server> servers = openAPI.getServers();

        // Chỉ lấy server hợp lệ
        assertEquals(1, servers.size());
        assertEquals("https://api.com", servers.get(0).getUrl());
        assertEquals("Valid Server", servers.get(0).getDescription());
    }
}
