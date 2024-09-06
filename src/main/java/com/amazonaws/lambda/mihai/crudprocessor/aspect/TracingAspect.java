package com.amazonaws.lambda.mihai.crudprocessor.aspect;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import com.fasterxml.jackson.databind.ObjectMapper;

@Aspect
public class TracingAspect {
	
	private static Logger logger = LogManager.getLogger(TracingAspect.class);
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	 
	  @Around("execution(* *(..)) && @annotation(com.amazonaws.lambda.mihai.crudprocessor.aspect.Trace)")
	  public Object traceMethod (ProceedingJoinPoint jp) throws Throwable {
		 return logMethods(jp);
	 
	  }
	  
	  @Around("execution(public * *(..)) "
	  		+ "&& @within(com.amazonaws.lambda.mihai.crudprocessor.aspect.TraceAll)"
	  		+ "&& !execution(* get*(..)) "
	  		+ "&& !execution(* set*(..))"
	  		+ "&& !execution(* build*(..))")
	  public Object traceClass (ProceedingJoinPoint jp) throws Throwable {
		 return logMethods(jp);
	  }

	  private Object logMethods(ProceedingJoinPoint jp) throws Throwable {
        String methodName = jp.getSignature().getName();
        
        logger.debug("\nSTART method: " + methodName + " with params: " + getMethodParameters(jp));
        
        long startTime = new Date().getTime();
        Object result = jp.proceed(jp.getArgs());
        long endTime = new Date().getTime();
        
        logger.debug("\nEND method: " + methodName + " with execution time: " + (endTime - startTime) + " ms");
        logger.debug("AOP method: " + methodName + ", returned: \n" + OBJECT_MAPPER.writeValueAsString(result) + "\n");

        return result;
	  }

    private String getMethodParameters(ProceedingJoinPoint jp) throws Exception {
        String[] argNames = ((MethodSignature) jp.getSignature()).getParameterNames();
        Object[] values = jp.getArgs();
        Map<String, Object> params = new HashMap<String, Object>();
        if (argNames.length != 0) {
            for (int i = 0; i < argNames.length; i++) {
                params.put(argNames[i], values[i]);
            }
        }
        
        return OBJECT_MAPPER.writeValueAsString(params);

    }

}
