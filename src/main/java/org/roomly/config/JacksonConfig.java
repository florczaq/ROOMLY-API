package org.roomly.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;

@Controller
public class JacksonConfig {
    @Bean
    public ObjectMapper objectMapper () {
        return new ObjectMapper();
    }
}
