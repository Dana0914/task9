package com.task09;


import com.task09.layer.OpenMeteoApi;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.type.TypeReference;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.RetentionSetting;
import com.syndicate.deployment.annotations.lambda.LambdaLayer;
import com.syndicate.deployment.model.ArtifactExtension;
import com.syndicate.deployment.model.DeploymentRuntime;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.syndicate.deployment.model.lambda.url.AuthType;
import com.syndicate.deployment.model.lambda.url.InvokeMode;
import com.fasterxml.jackson.core.type.TypeReference;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.Map;



@LambdaHandler(
    lambdaName = "api_handler",
	roleName = "api_handler-role",
	layers = {"api_handler-layer"},
	aliasName = "${lambdas_alias_name}",
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@LambdaLayer(
		layerName = "api_handler-layer",
		libraries = {"lib/open-meteo-layer.jar"},
		runtime = DeploymentRuntime.JAVA11,
		artifactExtension = ArtifactExtension.ZIP
)
@LambdaUrlConfig(
		authType = AuthType.NONE,
		invokeMode = InvokeMode.BUFFERED
)
public class ApiHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {
	private final ObjectMapper objectMapper;
	private final Map<String, Object> responseMap;
	private OpenMeteoApi api = new OpenMeteoApi();

	public ApiHandler() {
		responseMap = new LinkedHashMap<>();
		objectMapper = new ObjectMapper();
	}

	@Override
	public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent requestEvent, Context context) {
		APIGatewayV2HTTPEvent.RequestContext requestContext = new APIGatewayV2HTTPEvent.RequestContext();
		APIGatewayV2HTTPEvent.RequestContext.Http http = new APIGatewayV2HTTPEvent.RequestContext.Http();

		http.setMethod("GET");
		http.setPath("/weather");

		requestContext.setHttp(http);
		requestEvent.setRequestContext(requestContext);

		String method = requestEvent.getRequestContext().getHttp().getMethod();
		String path = requestEvent.getRequestContext().getHttp().getPath();

		if ("/weather".equals(path) && "GET".equalsIgnoreCase(method)) {
			// Fetch weather data
			String weatherData = api.fetchWeatherData();

			if (weatherData != null) {

				// Return successful response
				return APIGatewayV2HTTPResponse.builder()
						.withStatusCode(200)
						.withBody(weatherData)
						.withHeaders(Map.of("Content-Type", "application/json"))
						.build();
			}
			// Return bad request response
			return APIGatewayV2HTTPResponse.builder()
					.withStatusCode(400)
					.withBody("{\"message\":\"Bad request syntax or unsupported method. Request path: " + path + ". HTTP method: " + method + "\"}")
					.withHeaders(Map.of("Content-Type", "application/json"))
					.build();

		}
		// Return bad request response
		return APIGatewayV2HTTPResponse.builder()
				.withStatusCode(400)
				.withBody("{\"message\":\"Bad request syntax or unsupported method. Request path: " + path + ". HTTP method: " + method + "\"}")
				.withHeaders(Map.of("Content-Type", "application/json"))
				.build();

	}

}
