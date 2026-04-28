package org.roomly.annotations;

import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Notifiable {
    String title () default "";
    
    String description () default "";
    
    String recipientProfileId () default "";
    
    Class<?>[] groups () default {};
    
    Class<? extends Payload>[] payload () default {};
}
