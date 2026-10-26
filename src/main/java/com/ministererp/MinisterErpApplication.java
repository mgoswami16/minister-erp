package com.ministererp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableScheduling
public class MinisterErpApplication {

    public static void main(String[] args) {
        SpringApplication.run(MinisterErpApplication.class, args);
        printBanner();
    }

    private static void printBanner() {
        System.out.println("\n");
        
        System.out.println("║       MINISTER OFFICE CONTROL ROOM ERP SYSTEM            ║");
        System.out.println("║       Hon. Minister Shri Asim Arun                       ║");
        System.out.println("║       Uttar Pradesh Government                           ║");
        
        System.out.println("║       Version  : 1.0.0                                   ║");
        System.out.println("║       API URL  : http://localhost:8080/api/v1             ║");
        
        System.out.println("\n");
    }
}