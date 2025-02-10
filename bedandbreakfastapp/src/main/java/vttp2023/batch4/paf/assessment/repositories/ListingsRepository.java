package vttp2023.batch4.paf.assessment.repositories;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import vttp2023.batch4.paf.assessment.Utils;
import vttp2023.batch4.paf.assessment.models.Accommodation;
import vttp2023.batch4.paf.assessment.models.AccommodationSummary;

@Repository
public class ListingsRepository {

	// You may add additional dependency injections

	@Autowired
	private MongoTemplate template;

	// db.listings.aggregate([
	// {
	// $match: {
	// "address.country" : {$regex : "Australia", $options : "i"} ,
	// "address.suburb": { $exists: true, $ne: null, $ne: "" }

	// }
	// },
	// {
	// $group: {
	// _id: "$address.suburb" // Group by suburb
	// }
	// },
	// $project: {
	// _id : 1
	// }
	// ]);
	public List<String> getSuburbs(String country) {

		MatchOperation matchSuburbs = Aggregation.match(
				Criteria.where("address.country").regex(Pattern.quote(country), "i") // Escape regex safely
						.and("address.suburb").exists(true).ne(null).ne(""));

		GroupOperation groupSuburbs = Aggregation.group("address.suburb"); // Group by suburb

		ProjectionOperation projectSuburb = Aggregation.project("_id"); // Only keep _id (suburb names)

		Aggregation aggregation = Aggregation.newAggregation(matchSuburbs, groupSuburbs, projectSuburb);

		// Convert result to List<String> by extracting _id
		return template.aggregate(aggregation, "listings", Document.class)
				.getMappedResults()
				.stream()
				.map(doc -> doc.getString("_id")) // Extract suburb name from _id
				.toList(); // Converts to List<String>
	}

	// db.listings.aggregate({
	// $match : {"address.suburb": { $regex: "Surry", $options: "i" },
	// accommodates: { $gte: 2 },
	// price: { $lte: 100 },
	// min_nights: { $gte: 2 }}
	// },
	// {
	// $sort : { price : -1}
	// },
	// {
	// $project : {
	// _id: 1,
	// name: 1,
	// accommodates: 1,
	// price: 1
	// }});
	public List<AccommodationSummary> findListings(String suburb, int persons, int duration, float priceRange) {

		MatchOperation matchListings = Aggregation.match(
				Criteria.where("address.suburb").regex(suburb, "i")
						.and("accommodates").gte(persons)
						.and("price").lte(priceRange) // Assuming price is a number
						.and("min_nights").gte(duration) // Use min_nights
		);

		SortOperation sortByHighestPrice = Aggregation.sort(Sort.by(Sort.Direction.DESC, "price"));

		ProjectionOperation projectListings = Aggregation.project()
				.andExpression("_id").as("id") // Convert ObjectId to String
				.and("name").as("name")
				.and("accommodates").as("accommodates")
				.and("price").as("price");

		Aggregation aggregation = Aggregation.newAggregation(matchListings, sortByHighestPrice, projectListings);

		// Process price safely
		return template.aggregate(aggregation, "listings", Document.class)
				.getMappedResults()
				.stream()
				.map(doc -> {
					AccommodationSummary summary = new AccommodationSummary();
					summary.setId(doc.get("_id").toString()); // Convert ObjectId to String
					summary.setName(doc.getString("name"));
					summary.setAccomodates(doc.getInteger("accommodates"));
					summary.setPrice(doc.get("price", Number.class).floatValue()); // Convert price safely
					return summary;
				})
				.toList();
	}

	// IMPORTANT: DO NOT MODIFY THIS METHOD UNLESS REQUESTED TO DO SO
	// If this method is changed, any assessment task relying on this method will
	// not be marked
	public Optional<Accommodation> findAccommodatationById(String id) {
		Criteria criteria = Criteria.where("_id").is(id);
		Query query = Query.query(criteria);

		List<Document> result = template.find(query, Document.class, "listings");
		if (result.size() <= 0)
			return Optional.empty();

		return Optional.of(Utils.toAccommodation(result.getFirst()));
	}

}
