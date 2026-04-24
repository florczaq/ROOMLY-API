package org.roomly;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RoomlyApplication {
    
    public static void main (String[] args) {
        SpringApplication.run(RoomlyApplication.class, args);
    }
    
}

/*
    TODO:
        - Email service for sending activation emails, password reset emails
        - Implement password reset functionality
        - Implement email verification functionality
        - Implement user profile management functionality
        - Implement household management functionality
        - Implement shopping list management functionality
        - Implement inventory management functionality
 */