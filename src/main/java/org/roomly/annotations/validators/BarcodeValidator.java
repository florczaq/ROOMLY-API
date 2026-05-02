package org.roomly.annotations.validators;


import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;
import org.roomly.annotations.ValidBarcode;

@Slf4j
public class BarcodeValidator implements ConstraintValidator<ValidBarcode, String> {
    private String pattern;
    
    @Override
    public void initialize (ValidBarcode constraintAnnotation) {
        this.pattern = constraintAnnotation.pattern();
    }
    
    @Override
    public boolean isValid (String s, ConstraintValidatorContext constraintValidatorContext) {
        log.info("Validating barcode: {}", s);
        if (s == null) {
            return true;
        }
        return s.trim().matches(pattern);
    }
}
