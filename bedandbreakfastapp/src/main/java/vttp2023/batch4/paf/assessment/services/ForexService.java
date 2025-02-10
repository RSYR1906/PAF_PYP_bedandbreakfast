package vttp2023.batch4.paf.assessment.services;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ForexService {

	// TODO: Task 5
	private static final String API_URL = "https://api.frankfurter.app/latest";
	private final ObjectMapper objectMapper = new ObjectMapper(); // JSON Parser

	public float convert(String from, String to, float amount) {
		// If same currency, return the same amount
		if (from.equalsIgnoreCase(to)) {
			return amount;
		}

		// Build the request URL
		String url = UriComponentsBuilder.fromHttpUrl(API_URL)
				.queryParam("from", from.toUpperCase())
				.queryParam("to", to.toUpperCase())
				.toUriString();

		// Call the API
		RestTemplate restTemplate = new RestTemplate();
		@SuppressWarnings("unchecked")
		Map<String, Object> response = restTemplate.getForObject(url, Map.class);

		if (response != null && response.containsKey("rates")) {
			// Convert 'rates' safely using ObjectMapper
			@SuppressWarnings("unchecked")
			Map<String, Double> rates = objectMapper.convertValue(response.get("rates"), Map.class);
			if (rates.containsKey(to.toUpperCase())) {
				return amount * rates.get(to.toUpperCase()).floatValue();
			}
		}

		// If conversion fails
		throw new RuntimeException("Failed to retrieve exchange rate");
	}
}
