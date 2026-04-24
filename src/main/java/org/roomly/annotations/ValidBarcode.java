package org.roomly.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.roomly.annotations.validators.BarcodeValidator;

import java.lang.annotation.*;

@Documented
@Target({
  ElementType.FIELD,
  ElementType.PARAMETER
})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = BarcodeValidator.class)
@SuppressWarnings("unused")
public @interface ValidBarcode {
    String message () default "Invalid barcode format";
    
    String pattern () default "^[0-9]{8,13}$"; // EAN-8 or EAN-13 format
    
    Class<?>[] groups () default {};
    
    Class<? extends Payload>[] payload () default {};
    
}
