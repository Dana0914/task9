package com.task09.layer;


import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.HashMap;

public class OpenMeteoApi {
    private final String URL = "https://api.open-meteo.com/v1/forecast?latitude=52.52&longitude=13.41" +
            "&current=temperature_2m,wind_speed_10m&hourly=temperature_2m," +
            "relative_humidity_2m,wind_speed_10m";

    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, Object> responseMap = new LinkedHashMap<>();
    private final Request request = new Request.Builder()
            .url(URL)
            .get()
            .build();

    public String fetchWeatherData() {
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                return "Response is not successful";
            }

            // Parse response JSON
            Map<String, Object> jsonMap = objectMapper.readValue(response.body().string(), new TypeReference<>() {});

            // Extract required fields
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("latitude", jsonMap.get("latitude"));
            result.put("longitude", jsonMap.get("longitude"));
            result.put("generationtime_ms", jsonMap.get("generationtime_ms"));
            result.put("utc_offset_seconds", jsonMap.get("utc_offset_seconds"));
            result.put("timezone", jsonMap.get("timezone"));
            result.put("timezone_abbreviation", jsonMap.get("timezone_abbreviation"));
            result.put("elevation", jsonMap.get("elevation"));

            // Extract hourly & current data
            result.put("hourly_units", jsonMap.get("hourly_units"));
            result.put("hourly", jsonMap.get("hourly"));
            result.put("current_units", jsonMap.get("current_units"));
            result.put("current", jsonMap.get("current"));

            return objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"Failed to fetch weather data\"}";
        }
    }


}