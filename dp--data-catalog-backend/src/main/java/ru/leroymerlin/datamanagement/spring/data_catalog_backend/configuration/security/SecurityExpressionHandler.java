package ru.leroymerlin.datamanagement.spring.data_catalog_backend.configuration.security;

import java.util.function.Supplier;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.aopalliance.intercept.MethodInvocation;

/**
 * @author juliwolf
 */

@Component
public class SecurityExpressionHandler extends DefaultMethodSecurityExpressionHandler {
  private final AuthenticationTrustResolver trustResolver = new AuthenticationTrustResolverImpl();

  @Override
  public EvaluationContext createEvaluationContext (Supplier<Authentication> authentication, MethodInvocation invocation) {
    StandardEvaluationContext context = (StandardEvaluationContext) super.createEvaluationContext(authentication, invocation);
    MethodSecurityExpressionOperations delegate = (MethodSecurityExpressionOperations) context.getRootObject().getValue();
    RoleSecurityExpressionRoot root = new RoleSecurityExpressionRoot(delegate);
    root.setPermissionEvaluator(getPermissionEvaluator());
    root.setTrustResolver(trustResolver);
    context.setRootObject(root);

    return context;
  }
}
