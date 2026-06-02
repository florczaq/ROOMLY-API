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
import java.util.List;

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

        // Build SpEL context with result and all method parameters
        EvaluationContext context = new StandardEvaluationContext();
        context.setVariable("result", result);
        String[] paramNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();
        for (int i = 0; i < paramNames.length; i++) {
            context.setVariable(paramNames[i], args[i]);
        }

        String title = evaluateExpression(notifiable.title(), context);
        String description = evaluateExpression(notifiable.description(), context);

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

        String multiExpr = notifiable.recipientProfileIds();
        if (!multiExpr.isEmpty()) {
            for (String profileId : evaluateAsStringList(multiExpr, context)) {
                if (!profileId.isEmpty() && !profileRepository.existsByIdAndAccountId(profileId, authentication.getName())) {
                    notificationService.createAndSaveNotification(title, description, profileId);
                }
            }
            return;
        }

        String recipientProfileId = evaluateExpression(notifiable.recipientProfileId(), context);
        if (recipientProfileId == null || recipientProfileId.isEmpty()) {
            throw new IllegalArgumentException("Either recipientProfileId or recipientProfileIds must be set on @Notifiable");
        }
        if (!profileRepository.existsByIdAndAccountId(recipientProfileId, authentication.getName())) {
            notificationService.createAndSaveNotification(title, description, recipientProfileId);
        }
    }
    
    private String evaluateExpression (String expression, EvaluationContext context) {
        if (expression == null || expression.isEmpty()) return expression;
        if (expression.contains("#")) {
            try {
                return parser.parseExpression(expression, new TemplateParserContext()).getValue(context, String.class);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to evaluate SpEL expression: " + expression, e);
            }
        }
        return expression;
    }

    private List<String> evaluateAsStringList (String expression, EvaluationContext context) {
        String inner = expression.trim();
        if (inner.startsWith("#{") && inner.endsWith("}")) {
            inner = inner.substring(2, inner.length() - 1);
        }
        try {
            Object value = parser.parseExpression(inner).getValue(context);
            if (value instanceof List<?> list) {
                return list.stream().map(Object::toString).toList();
            }
            return List.of();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to evaluate SpEL list expression: " + expression, e);
        }
    }
}