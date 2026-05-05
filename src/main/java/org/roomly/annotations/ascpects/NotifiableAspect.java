package org.roomly.annotations.ascpects;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.roomly.annotations.Notifiable;
import org.roomly.notifications.service.NotificationService;
import org.roomly.repositories.ProfileRepository;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
@RequiredArgsConstructor
public class NotifiableAspect {
    private final NotificationService notificationService;
    private final ProfileRepository profileRepository;
    private final ExpressionParser parser = new SpelExpressionParser();
    
    /**
     * This aspect intercepts methods annotated with @Notifiable and creates a notification based on the annotation's parameters.
     * It checks if the authenticated user is not the recipient of the notification to prevent self-notifications.
     */
    @AfterReturning(pointcut = "@annotation(org.roomly.annotations.Notifiable)", returning = "result")
    public void afterNotifiableMethod (JoinPoint joinPoint, Object result) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        
        Notifiable notifiable = method.getAnnotation(Notifiable.class);
        
        // Create SpEL evaluation context with the result object
        EvaluationContext context = new StandardEvaluationContext();
        context.setVariable("result", result);

        // Evaluate SpEL expressions
        String title = evaluateExpression(notifiable.title(), context);
        String description = evaluateExpression(notifiable.description(), context);
        String recipientProfileId = evaluateExpression(notifiable.recipientProfileId(), context);
        
        if (recipientProfileId == null || recipientProfileId.isEmpty()) {
            throw new IllegalArgumentException("Recipient profile ID cannot be null or empty");
        }
        
        if (title == null || title.isEmpty()) {
            throw new IllegalArgumentException("Notification title cannot be null or empty");
        }
        
        if (description == null) {
            throw new IllegalArgumentException("Notification description cannot be null");
        }
        
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("User is not authenticated");
        }
        
        //TODO this validation dont work and sends notification to the user itself, need to check if the recipientProfileId belongs to the authenticated user and skip notification if it does
        
        // Notify only if the recipientProfileId does not belong to the authenticated user
        if (profileRepository.existsByIdAndAccountId(recipientProfileId, authentication.getName())) {
            return;
        }
        
        notificationService.createAndSaveNotification(title, description, recipientProfileId);
    }
    
    /**
     * Evaluates a SpEL expression if it contains SpEL syntax, otherwise returns the literal string.
     */
    private String evaluateExpression (String expression, EvaluationContext context) {
        if (expression == null || expression.isEmpty()) {
            return expression;
        }
        
        // Check if the expression contains SpEL syntax
        if (expression.contains("#")) {
            try {
                return parser
                  .parseExpression(expression, new TemplateParserContext())
                  .getValue(context, String.class);
            } catch (Exception e) {
                // If evaluation fails, log and return the original expression
                throw new IllegalStateException("Failed to evaluate SpEL expression: " + expression, e);
            }
        }
        
        return expression;
    }
}