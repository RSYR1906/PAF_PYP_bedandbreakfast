package vttp2023.batch4.paf.assessment.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vttp2023.batch4.paf.assessment.models.Accommodation;
import vttp2023.batch4.paf.assessment.models.AccommodationSummary;
import vttp2023.batch4.paf.assessment.models.Bookings;
import vttp2023.batch4.paf.assessment.models.User;
import vttp2023.batch4.paf.assessment.repositories.BookingsRepository;
import vttp2023.batch4.paf.assessment.repositories.ListingsRepository;

@Service
public class ListingsService {

	// You may add additional dependency injections

	@Autowired
	private ListingsRepository listingsRepo;

	@Autowired
	private BookingsRepository bookingsRepo;

	@Autowired
	private ForexService forexSvc;

	// IMPORTANT: DO NOT MODIFY THIS METHOD.
	// If this method is changed, any assessment task relying on this method will
	// not be marked
	public List<String> getAustralianSuburbs() {
		return listingsRepo.getSuburbs("australia");
	}

	// IMPORTANT: DO NOT MODIFY THIS METHOD UNLESS REQUESTED TO DO SO
	// If this method is changed, any assessment task relying on this method will
	// not be marked
	public List<AccommodationSummary> findAccommodatations(String suburb, int persons, int duration, float priceRange) {
		return listingsRepo.findListings(suburb, persons, duration, priceRange);
	}

	// IMPORTANT: DO NOT MODIFY THIS METHOD UNLESS REQUESTED TO DO SO
	// If this method is changed, any assessment task relying on this method will
	// not be marked
	public Optional<Accommodation> findAccommodatationById(String id) {
		Optional<Accommodation> opt = listingsRepo.findAccommodatationById(id);

		if (opt.isEmpty())
			return opt;

		Accommodation acc = opt.get();
		float sgd = forexSvc.convert("aud", "sgd", acc.getPrice());
		acc.setPrice(sgd);

		return opt;
	}

	// TODO: Task 6
	// IMPORTANT: DO NOT MODIFY THE SIGNATURE OF THIS METHOD.
	// You may only add annotations and throw exceptions to this method
	@Transactional
	public void createBooking(Bookings booking) {
		try {
			// Check if user exists in the database
			Optional<User> existingUser = bookingsRepo.userExists(booking.getEmail());

			// If user does not exist, create a new user
			if (existingUser.isEmpty()) {
				bookingsRepo.newUser(new User(booking.getEmail(), booking.getName()));
			}

			// Create the new booking
			bookingsRepo.newBookings(booking);

		} catch (Exception e) {
			throw new RuntimeException("Booking failed: " + e.getMessage(), e);
		}
	}

}
