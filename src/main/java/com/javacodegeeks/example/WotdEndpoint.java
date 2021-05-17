package com.javacodegeeks.example;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

@Component
@Endpoint(id = "wotd", enableByDefault = true)
public class WotdEndpoint {
	
	@ReadOperation
	public String wordOfTheDay() {
		return "{\"wotd\": \"Brilliant\"}";
	}

}
