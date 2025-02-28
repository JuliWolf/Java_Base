package ru.leroymerlin.datamanagement.spring.data_catalog_backend.aspects;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import logger.LoggerWrapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.aspects.models.PathVariables;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.Request;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.log.LogRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.Log;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.RequestType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.AuthUserDetails;

/**
 * @author juliwolf
 */

@Aspect
@Configuration
public class LoggerAspectConfiguration {
  @Autowired
  private LogRepository logRepository;

  @Pointcut("""
    execution(* ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1..*(..)) &&
    (
      @within(org.springframework.web.bind.annotation.RestController) ||
      @within(org.springframework.web.bind.annotation.RequestMapping)
    ) &&
    (
      @annotation(org.springframework.web.bind.annotation.PostMapping) ||
      @annotation(org.springframework.web.bind.annotation.PatchMapping) ||
      @annotation(org.springframework.web.bind.annotation.DeleteMapping)
    ) &&
    !(
      within(ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.auth.AuthController)
    )
  """)
  public void controllerMethods(){}

  @Pointcut("""
    execution(* ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.AssetsController.getAssetHeader(..))
  """)
  public void getAssetsChildrenMethod(){}

  @Around("controllerMethods() || getAssetsChildrenMethod()")
  public Object saveInLog (ProceedingJoinPoint joinPoint) throws Throwable {
    HashMap<String, Object> argsMap = parseArgs(joinPoint);

    User user = getUser(argsMap).orElse(null);
    String controller = getControllerName(joinPoint);
    String pathVariables = getPathVariables(argsMap);
    String requestString = getRequestString(argsMap);
    RequestType requestType = getRequestType(joinPoint);

    ResponseEntity responseObject = (ResponseEntity) joinPoint.proceed();

    String responseJson = parseObjectToString(responseObject.getBody());

    try {
      logRepository.save(new Log(
        requestType,
        user,
        controller,
        pathVariables,
        requestString,
        responseJson,
        responseObject.getStatusCode().value()
      ));
    } catch (Exception exception) {
      LoggerWrapper.error("Error while saving log: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        LoggerAspectConfiguration.class.getName()
      );
    }

    return responseObject;
  }

  private String getControllerName (ProceedingJoinPoint joinPoint) {
    return joinPoint.getTarget().getClass().getSimpleName();
  }

  private HashMap<String, Object> parseArgs (ProceedingJoinPoint joinPoint) {
    Object[] args = joinPoint.getArgs();
    HashMap<String, Object> argsMap = new HashMap<>();
    PathVariables pathVariables = new PathVariables(new HashMap<>());

    argsMap.put(PathVariables.class.getSimpleName(), pathVariables);

    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    Method method = signature.getMethod();
    Annotation[][] parameterAnnotations = method.getParameterAnnotations();

    assert args.length == parameterAnnotations.length;

    for (int i = 0; i < args.length; i++) {
      if (args[i] instanceof Request) {
        argsMap.put(Request.class.getSimpleName(), args[i]);

        continue;
      }

      if (args[i] instanceof Authentication) {
        argsMap.put(Authentication.class.getSimpleName(), args[i]);

        continue;
      }

      Annotation[] parameterAnnotation = parameterAnnotations[i];

      parsePathVariables(parameterAnnotation, pathVariables.getVariables(), args[i]);
    }

    return argsMap;
  }

  private void parsePathVariables (Annotation[] annotations, HashMap<String, String> variables, Object arg) {
    if (annotations.length == 0) return;

    Optional<Annotation> optionalVariable = Arrays.stream(annotations).filter(annotation -> annotation instanceof PathVariable).findFirst();

    if (optionalVariable.isEmpty()) return;

    PathVariable pathVariable = (PathVariable) optionalVariable.get();

    variables.put(pathVariable.value(), (String) arg);
  }

  private String getRequestString(HashMap<String, Object> argsMap) {
    Request request = (Request) argsMap.get(Request.class.getSimpleName());

    return parseObjectToString(request);
  }

  private Optional<User> getUser (HashMap<String, Object> argsMap) {
    Authentication userData = (Authentication) argsMap.get(Authentication.class.getSimpleName());

    if (userData == null) {
      return Optional.empty();
    }

    AuthUserDetails userDetails = (AuthUserDetails) userData.getPrincipal();

    return Optional.of(userDetails.getUser());
  }

  private RequestType getRequestType (ProceedingJoinPoint joinPoint) {
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    Method method = signature.getMethod();

    if (method.isAnnotationPresent(GetMapping.class)) {
      return RequestType.GET;
    }

    if (method.isAnnotationPresent(PostMapping.class)) {
      return RequestType.POST;
    }

    if (method.isAnnotationPresent(PatchMapping.class)) {
      return RequestType.PATCH;
    }

    if (method.isAnnotationPresent(DeleteMapping.class)) {
      return RequestType.DELETE;
    }

    return null;
  }

  private String parseObjectToString (Object object) {
    try {
      ObjectMapper objectMapper = new ObjectMapper();

      return objectMapper.writeValueAsString(object);
    } catch (JsonProcessingException jsonProcessingException) {
      LoggerWrapper.error("Can not convert object to json: " + jsonProcessingException.getMessage(),
        jsonProcessingException.getStackTrace(),
        null,
        LoggerAspectConfiguration.class.getName()
      );

      return null;
    }
  }

  private String getPathVariables (HashMap<String, Object> argsMap) {
    PathVariables pathVariables = (PathVariables) argsMap.get(PathVariables.class.getSimpleName());

    if (pathVariables.getVariables().isEmpty()) return null;

    return parseObjectToString(pathVariables.getVariables());
  }
}
