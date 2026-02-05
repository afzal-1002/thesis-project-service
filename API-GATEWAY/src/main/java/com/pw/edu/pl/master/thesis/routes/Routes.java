package com.pw.edu.pl.master.thesis.routes;

import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.HandlerFunction;
import org.springframework.web.servlet.function.RequestPredicate;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

@Configuration
public class Routes {

//    @Bean
//    public RouterFunction<ServerResponse> productServiceRoute()
//    {
//        return GatewayRouterFunctions.route("product_service")
//                .route(RequestPredicate.path("/api/product"), HandlerFunction.http("http://localhost:800"))
//                .build();
//    }

}
