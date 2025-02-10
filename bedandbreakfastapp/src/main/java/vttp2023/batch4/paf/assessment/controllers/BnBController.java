package vttp2023.batch4.paf.assessment.controllers;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import vttp2023.batch4.paf.assessment.Utils;
import vttp2023.batch4.paf.assessment.models.Accommodation;
import vttp2023.batch4.paf.assessment.models.Bookings;
import vttp2023.batch4.paf.assessment.services.ListingsService;

@Controller
@RequestMapping(path = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
public class BnBController {

	// You may add additional dependency injections

	@Autowired
	private ListingsService listingsSvc;

	// IMPORTANT: DO NOT MODIFY THIS METHOD UNLESS REQUESTED TO DO SO
	// If this method is changed, any assessment task relying on this method will
	// not be marked
	@GetMapping("/suburbs")
	@ResponseBody
	public ResponseEntity<String> getSuburbs() {
		List<String> suburbs = listingsSvc.getAustralianSuburbs();
		JsonArray result = Json.createArrayBuilder(suburbs).build();
		return ResponseEntity.ok(result.toString());
	}

	// IMPORTANT: DO NOT MODIFY THIS METHOD UNLESS REQUESTED TO DO SO
	// If this method is changed, any assessment task relying on this method will
	// not be marked
	@GetMapping("/search")
	@ResponseBody
	public ResponseEntity<String> search(@RequestParam MultiValueMap<String, String> params) {

		String suburb = params.getFirst("suburb");
		int persons = Integer.parseInt(params.getFirst("persons"));
		int duration = Integer.parseInt(params.getFirst("duration"));
		float priceRange = Float.parseFloat(params.getFirst("price_range"));

		JsonArrayBuilder arrBuilder = Json.createArrayBuilder();
		listingsSvc.findAccommodatations(suburb, persons, duration, priceRange)
				.stream()
				.forEach(acc -> arrBuilder.add(
						Json.createObjectBuilder()
								.add("id", acc.getId())
								.add("name", acc.getName())
								.add("price", acc.getPrice())
								.add("accommodates", acc.getAccomodates())
								.build()));

		return ResponseEntity.ok(arrBuilder.build().toString());
	}

	// IMPORTANT: DO NOT MODIFY THIS METHOD UNLESS REQUESTED TO DO SO
	// If this method is changed, any assessment task relying on this method will
	// not be marked
	@GetMapping("/accommodation/{id}")
	@ResponseBody
	public ResponseEntity<String> getAccommodationById(@PathVariable String id) {

		Optional<Accommodation> opt = listingsSvc.findAccommodatationById(id);
		if (opt.isEmpty())
			return ResponseEntity.notFound().build();

		return ResponseEntity.ok(Utils.toJson(opt.get()).toString());
	}

	// TODO: Task 6
	@PostMapping("/accommodation")
	public ResponseEntity<String> getNewBooking(@RequestBody Map<String, Object> bookingData) {

		// Extract data from request
		String bookingId = (String) bookingData.get("bookingId");
		String listingId = (String) bookingData.get("id");
		String email = (String) bookingData.get("email");
		String name = (String) bookingData.get("name");

		// Manually map "nights" to "duration"
		int duration = (bookingData.get("nights") != null)
				? ((Number) bookingData.get("nights")).intValue()
				: 0;

		// Validate duration before inserting into DB
		if (duration <= 0) {
			return ResponseEntity.badRequest().body("{\"error\": \"Duration (nights) must be greater than 0\"}");
		}

		// Create Bookings object and insert into DB
		Bookings booking = new Bookings();
		booking.setDuration(duration);
		booking.setListingId(listingId);
		booking.setEmail(email);
		booking.setName(name);
		listingsSvc.createBooking(booking);

		return ResponseEntity.ok("{}"); // Return empty JSON response
	}
}
