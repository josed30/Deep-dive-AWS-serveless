package com.task05;


import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.RetentionSetting;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;


import com.google.gson.Gson;


import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Calendar;
import java.text.SimpleDateFormat;


@LambdaHandler(lambdaName = "api_handler",
	roleName = "api_handler-role",
	isPublishVersion = false,
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@EnvironmentVariables(value = {
		@EnvironmentVariable(key = "region", value = "${region}"),
		@EnvironmentVariable(key = "target_table", value = "${target_table}")})
public class ApiHandler implements RequestHandler<Map<String, Object>, Map<String, Object>> {

	private Gson parser = new Gson();
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.ssssss");

	public Map<String, Object> handleRequest(Map<String, Object> request, Context context) {


		String id = UUID.randomUUID().toString();
		String createdAt = sdf.format(Calendar.getInstance().getTime());

		Map<String, Object> response = persist(id, request);

		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("statusCode", 201);
		resultMap.put("event", response);
		return resultMap;

	}


	private Map<String, Object> persist(String id, Map<String, Object> body){
		DynamoDbClient client = null;
		String principalId = String.valueOf(body.get("principalId"));
		String createdAt = sdf.format(Calendar.getInstance().getTime());
		Map<String, String> content = (Map<String, String>)body.get("content");
		Map<String, Object> response = new HashMap<>();

		response.put("id", id);
		response.put("principalId", Integer.valueOf(principalId));
		response.put("createdAt", createdAt);
		response.put("body", content);


		try {
			Region region = Region.EU_CENTRAL_1;
			client = DynamoDbClient.builder()
					.region(region)
					.build();

			HashMap<String, AttributeValue> itemValues = new HashMap<>();
			itemValues.put("id", AttributeValue.builder().s(id).build());
			itemValues.put("principalId", AttributeValue.builder().n(principalId).build());
			itemValues.put("createdAt", AttributeValue.builder().s(createdAt).build());
			itemValues.put("body", AttributeValue.builder().m(convert(content)).build());

			PutItemRequest request = PutItemRequest.builder()
					.tableName(System.getenv("target_table"))
					.item(itemValues)
					.build();

			client.putItem(request);

            return response;

		}finally{
			if(client != null){
				client.close();
			}

		}


	}

	private Map<String, AttributeValue> convert(Map<String, String> content){

		Map<String, AttributeValue> map = new HashMap<>();

		for(String key: content.keySet()){
			String value = content.get(key);
			map.put(key, AttributeValue.builder().s(value).build());
		}

		return map;


	}







}
