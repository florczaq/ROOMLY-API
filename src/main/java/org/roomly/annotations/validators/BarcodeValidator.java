package org.roomly.annotations.validators;


import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.roomly.annotations.ValidBarcode;

public class BarcodeValidator implements ConstraintValidator<ValidBarcode, String> {
    private String pattern;
    
    @Override
    public void initialize (ValidBarcode constraintAnnotation) {
        this.pattern = constraintAnnotation.pattern();
    }
    
    @Override
    public boolean isValid (String s, ConstraintValidatorContext constraintValidatorContext) {
        if (s == null || s.isEmpty()) {
            return false;
        }
        return s.matches(pattern);
    }
}
