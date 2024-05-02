package com.task02;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.model.RetentionSetting;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.syndicate.deployment.model.lambda.url.AuthType;
import com.syndicate.deployment.model.lambda.url.InvokeMode;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

@LambdaHandler(lambdaName = "hello_world",
	roleName = "hello_world-role",
	isPublishVersion = false,
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@LambdaUrlConfig(
		authType = AuthType.NONE,
		invokeMode = InvokeMode.BUFFERED
)
public class HelloWorld implements RequestHandler<Map<String, Object>, String> {

	private static final int SC_OK = 200;
	private static final int SC_BAD_REQUEST = 400;
	private static final String REQUEST_CONTEXT = "requestContext";
	private static final String HTTP = "http";
	private static final String METHOD = "method";
	private static final String PATH = "path";

	private static final String HELLO_PATH = "/hello";
	private static final String GET_METHOD = "GET";



	private static String RESPONSE = "{'statusCode': %d, 'message': '%s'}";

	private static String MESSAGE_OK = "Hello from Lambda";
	private static String MESSAGE_BAD_REQUEST = "Bad request syntax or unsupported method. Request path: %s. HTTP method: %s";



	public String handleRequest(Map<String, Object> request, Context context) {

		Map<String, Object> requestContext = (Map<String, Object>) request.get(REQUEST_CONTEXT);
		Map<String, Object> http = (Map<String, Object>) requestContext.get(HTTP);
		String method = (String)http.get(METHOD);
		String path = (String)http.get(PATH);

		int code = SC_OK;
		String message = MESSAGE_OK;

		if(!HELLO_PATH.equals(path) || !GET_METHOD.equals(method)) {
			code = SC_BAD_REQUEST;
			message = String.format(MESSAGE_BAD_REQUEST, path, method);
		}

		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("statusCode", code);
		resultMap.put("message", message);

		return new Gson().toJson(resultMap);

	}
}
