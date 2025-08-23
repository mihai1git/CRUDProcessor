package com.amazonaws.lambda.mihai.crudprocessor.service;

import java.util.Calendar;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazonaws.lambda.mihai.crudprocessor.aspect.Trace;
import com.amazonaws.lambda.mihai.crudprocessor.aspect.TraceAll;
import com.amazonaws.lambda.mihai.crudprocessor.model.RateAuthorization;



@TraceAll
public class SecurityService {
	
	private static Logger logger = LogManager.getLogger(SecurityService.class);
	private Map<String, String> environmentVariables;
	
	private DynamoService dynamoSrv;
	
	
    /**
     * access is counted only for requests that uses the services, denied access is not counted
     * @return true if number of access to lambda business exceeds the rate in interval
     */
    public RateAuthorization isRateBasedAuthorized() {
    	
    	Boolean auth = Boolean.FALSE;
    	Boolean activeAuthorizer = ("true".equals(environmentVariables.get("activeAuthorizer")))?Boolean.TRUE:Boolean.FALSE;
    	Integer rateLimit = Integer.valueOf(environmentVariables.getOrDefault("rateLimit", "10"));//default number of calls
    	Integer evaluationWindow = Integer.valueOf(environmentVariables.getOrDefault("evaluationWindow", "1"));//default number of days: 1, starting with midnight
    	String requestAggregation = "COUNT_ALL";
    	String apigatewayid = environmentVariables.get("apigatewayid");
    	if (apigatewayid == null) throw new RuntimeException("apigatewayid was not set from API GW invocation event, requestContext.apiId !");
    	Integer unauthorizedHours = null;
    	
    	if (activeAuthorizer && validRate(rateLimit, requestAggregation, apigatewayid)) {
    		auth = Boolean.TRUE;
    		dynamoSrv.putRecord(apigatewayid, auth);
    	} else {
    		unauthorizedHours = 24 - Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
    	}
    	
    	RateAuthorization rateAuth = new RateAuthorization();
    	rateAuth.setAuthorization(auth);
    	rateAuth.setUnauthorizedHours(unauthorizedHours);
    	
    	return rateAuth;
    }
	
	/**
	 * evaluationWindow - today starting with 00:00:00 UTC, not in web client timezone, with message
	 * @param dynamoSrv
	 * @param rateLimit
	 * @param requestAggregation
	 * @param apigatewayid
	 * @return
	 */
	private Boolean validRate(Integer rateLimit, String requestAggregation, String apigatewayid) {
		
    	Boolean validRate = Boolean.FALSE;
        
        Integer recNum = dynamoSrv.countNewestItems(apigatewayid);
        
        if (rateLimit > recNum) validRate = Boolean.TRUE;
                	
        return validRate;
	}

	public Map<String, String> getEnvironmentVariables() {
		return environmentVariables;
	}

	public void setEnvironmentVariables(Map<String, String> environmentVariables) {
		this.environmentVariables = environmentVariables;
	}

	public DynamoService getDynamoSrv() {
		return dynamoSrv;
	}

	public void setDynamoSrv(DynamoService dynamoSrv) {
		this.dynamoSrv = dynamoSrv;
	}
	
	
}
