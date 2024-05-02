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


@LambdaHandler(lambdaName = "hello_world",
	roleName = "hello_world-role",
	isPublishVersion = false,
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@LambdaUrlConfig(
		authType = AuthType.NONE,
		invokeMode = InvokeMode.BUFFERED
)
public class HelloWorld implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

	private static final int SC_OK = 200;
	private static final int SC_BAD_REQUEST = 400;
	private static final String PATH = "hello";
	private static String RESPONSE = "{\"statusCode\": %d, \"message\": \"%s\"}";

	private static String MESSAGE_OK = "Hello from Lambda";
	private static String MESSAGE_BAD_REQUEST = "Bad request syntax or unsupported method. Request path: %s. HTTP method: %s";



	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
		String method = request.getHttpMethod();
		String path = request.getPath();

		int code = SC_OK;
		String message = MESSAGE_OK;

		if(!PATH.equals(path)) {
			code = SC_BAD_REQUEST;
			message = String.format(MESSAGE_BAD_REQUEST, path, method);
		}


		return new APIGatewayProxyResponseEvent()
				.withStatusCode(code)
				.withBody(String.format(RESPONSE, code, message));
	}
}
