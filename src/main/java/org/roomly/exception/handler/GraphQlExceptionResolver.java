package org.roomly.exception.handler;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import jakarta.persistence.EntityNotFoundException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.roomly.exception.ConflictException;
import org.roomly.exception.ResourceNotFoundException;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GraphQlExceptionResolver extends DataFetcherExceptionResolverAdapter {

    @Override
    protected GraphQLError resolveToSingleError(@NonNull Throwable ex, @NonNull DataFetchingEnvironment env) {
        ErrorType errorType = mapToErrorType(ex);
        if (errorType == null) {
            log.error("Unhandled GraphQL error", ex);
            return null;
        }
        log.warn("GraphQL error [{}]: {}", errorType, ex.getMessage());
        return GraphqlErrorBuilder.newError(env)
            .errorType(errorType)
            .message(ex.getMessage())
            .build();
    }

    private ErrorType mapToErrorType(Throwable ex) {
        if (ex instanceof ResourceNotFoundException || ex instanceof EntityNotFoundException) {
            return ErrorType.NOT_FOUND;
        }
        if (ex instanceof ConflictException) {
            return ErrorType.BAD_REQUEST;
        }
        if (ex instanceof IllegalArgumentException) {
            return ErrorType.BAD_REQUEST;
        }
        if (ex instanceof IllegalStateException) {
            return ErrorType.BAD_REQUEST;
        }
        if (ex instanceof SecurityException) {
            return ErrorType.FORBIDDEN;
        }
        return null;
    }
}