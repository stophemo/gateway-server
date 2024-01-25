package org.example;

import com.tfswx.component.resttemplateplus.TfRestTemplatePlusScan;
//import org.example.service.RemoteRouteService;
import org.example.service.RemoteRouteService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@TfRestTemplatePlusScan(services = RemoteRouteService.class)
public class GatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
