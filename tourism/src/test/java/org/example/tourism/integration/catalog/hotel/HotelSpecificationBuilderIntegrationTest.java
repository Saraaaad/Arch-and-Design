package org.example.tourism.integration.catalog.hotel;

import org.example.tourism.catalog.hotel.*;
import org.example.tourism.catalog.roomtype.RoomType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class HotelSpecificationBuilderIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private HotelRepository hotelRepository;

    @BeforeEach
    void setUp() {
        // Clear any existing data
        hotelRepository.deleteAll();
        entityManager.flush();

        // Create Hotel 1 - Grand Plaza in New York (5 stars)
        Hotel hotel1 = new Hotel();
        hotel1.setName("Grand Plaza");
        hotel1.setCity("New York");
        hotel1.setCountry("USA");
        hotel1.setStarRating(5);
        hotel1.setChildrenAllowed(true);
        hotel1.setPetsAllowed(false);
        hotel1.setSmokingAllowed(false);
        hotel1.setCancellationPolicy("FREE_CANCELLATION");
        hotel1.setAmenities(Set.of("WiFi", "Pool", "Spa"));
        hotel1.setFeatured(true);

        RoomType roomType1 = new RoomType();
        roomType1.setName("Deluxe Suite");
        roomType1.setBedType("King");
        roomType1.setCapacity(2);
        roomType1.setBasePrice(new BigDecimal("300.00"));
        roomType1.setHotel(hotel1);
        hotel1.getRoomTypes().add(roomType1);

        entityManager.persist(hotel1);

        // Create Hotel 2 - Budget Inn in New York (3 stars)
        Hotel hotel2 = new Hotel();
        hotel2.setName("Budget Inn");
        hotel2.setCity("New York");
        hotel2.setCountry("USA");
        hotel2.setStarRating(3);
        hotel2.setChildrenAllowed(true);
        hotel2.setPetsAllowed(true);
        hotel2.setSmokingAllowed(true);
        hotel2.setCancellationPolicy("NON_REFUNDABLE");
        hotel2.setAmenities(Set.of("WiFi", "Parking"));
        hotel2.setFeatured(false);

        RoomType roomType2 = new RoomType();
        roomType2.setName("Standard Room");
        roomType2.setBedType("Queen");
        roomType2.setCapacity(2);
        roomType2.setBasePrice(new BigDecimal("100.00"));
        roomType2.setHotel(hotel2);
        hotel2.getRoomTypes().add(roomType2);

        entityManager.persist(hotel2);

        // Create Hotel 3 - Beach Resort in Miami (4 stars)
        Hotel hotel3 = new Hotel();
        hotel3.setName("Beach Resort");
        hotel3.setCity("Miami");
        hotel3.setCountry("USA");
        hotel3.setStarRating(4);
        hotel3.setChildrenAllowed(true);
        hotel3.setPetsAllowed(true);
        hotel3.setSmokingAllowed(false);
        hotel3.setCancellationPolicy("FREE_CANCELLATION");
        hotel3.setAmenities(Set.of("WiFi", "Pool", "Beach Access", "Spa"));
        hotel3.setFeatured(true);
        hotel3.setDealTag("SUMMER_SALE");

        RoomType roomType3 = new RoomType();
        roomType3.setName("Ocean View Suite");
        roomType3.setBedType("King");
        roomType3.setCapacity(4);
        roomType3.setBasePrice(new BigDecimal("500.00"));
        roomType3.setHotel(hotel3);
        hotel3.getRoomTypes().add(roomType3);

        entityManager.persist(hotel3);

        entityManager.flush();
    }

    @Test
    void testSearchByCity() {
        HotelSearchCriteria criteria = HotelSearchCriteria.builder()
                .city("New York")
                .build();

        Specification<Hotel> spec = HotelSpecificationBuilder.buildSpecification(criteria);
        var results = hotelRepository.findAll(spec);

        assertThat(results).hasSize(2);
        assertThat(results).extracting("name").containsExactlyInAnyOrder("Grand Plaza", "Budget Inn");
    }

    @Test
    void testSearchByName() {
        HotelSearchCriteria criteria = HotelSearchCriteria.builder()
                .name("Grand")
                .build();

        Specification<Hotel> spec = HotelSpecificationBuilder.buildSpecification(criteria);
        var results = hotelRepository.findAll(spec);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Grand Plaza");
    }

    @Test
    void testSearchByStarRating() {
        HotelSearchCriteria criteria = HotelSearchCriteria.builder()
                .starRating(4)
                .build();

        Specification<Hotel> spec = HotelSpecificationBuilder.buildSpecification(criteria);
        var results = hotelRepository.findAll(spec);

        // starRating >= 4 should return both 5-star and 4-star hotels
        assertThat(results).hasSize(2);
        assertThat(results).extracting("name").containsExactlyInAnyOrder("Grand Plaza", "Beach Resort");
        assertThat(results).extracting("starRating").containsExactlyInAnyOrder(5, 4);
    }

    @Test
    void testSearchByStarRating_ExactMatch_UsingGreaterThanOrEqual() {
        // This test documents that starRating uses >= comparison
        HotelSearchCriteria criteria = HotelSearchCriteria.builder()
                .starRating(5)
                .build();

        Specification<Hotel> spec = HotelSpecificationBuilder.buildSpecification(criteria);
        var results = hotelRepository.findAll(spec);

        // starRating >= 5 should return only 5-star hotels
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Grand Plaza");
        assertThat(results.get(0).getStarRating()).isEqualTo(5);
    }

    @Test
    void testSearchByPriceRange() {
        HotelSearchCriteria criteria = HotelSearchCriteria.builder()
                .minPrice(new BigDecimal("150"))
                .maxPrice(new BigDecimal("400"))
                .build();

        Specification<Hotel> spec = HotelSpecificationBuilder.buildSpecification(criteria);
        var results = hotelRepository.findAll(spec);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Grand Plaza");
    }

    @Test
    void testSearchByMinPriceOnly() {
        HotelSearchCriteria criteria = HotelSearchCriteria.builder()
                .minPrice(new BigDecimal("400"))
                .build();

        Specification<Hotel> spec = HotelSpecificationBuilder.buildSpecification(criteria);
        var results = hotelRepository.findAll(spec);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Beach Resort");
    }

    @Test
    void testSearchByMaxPriceOnly() {
        HotelSearchCriteria criteria = HotelSearchCriteria.builder()
                .maxPrice(new BigDecimal("200"))
                .build();

        Specification<Hotel> spec = HotelSpecificationBuilder.buildSpecification(criteria);
        var results = hotelRepository.findAll(spec);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Budget Inn");
    }

    @Test
    void testSearchBySingleAmenity() {
        HotelSearchCriteria criteria = HotelSearchCriteria.builder()
                .amenities(Set.of("Spa"))
                .build();

        Specification<Hotel> spec = HotelSpecificationBuilder.buildSpecification(criteria);
        var results = hotelRepository.findAll(spec);

        assertThat(results).hasSize(2);
        assertThat(results).extracting("name").containsExactlyInAnyOrder("Grand Plaza", "Beach Resort");
    }

    @Test
    void testSearchByMultipleAmenities_AllMustBePresent() {
        HotelSearchCriteria criteria = HotelSearchCriteria.builder()
                .amenities(Set.of("WiFi", "Pool", "Spa"))
                .build();

        Specification<Hotel> spec = HotelSpecificationBuilder.buildSpecification(criteria);
        var results = hotelRepository.findAll(spec);

        assertThat(results).hasSize(2);
        assertThat(results).extracting("name").containsExactlyInAnyOrder("Grand Plaza", "Beach Resort");
    }

    @Test
    void testSearchByAmenities_ExactMatchOnly() {
        HotelSearchCriteria criteria = HotelSearchCriteria.builder()
                .amenities(Set.of("WiFi", "Pool", "Beach Access", "Spa"))
                .build();

        Specification<Hotel> spec = HotelSpecificationBuilder.buildSpecification(criteria);
        var results = hotelRepository.findAll(spec);

        // Only Beach Resort has all these amenities
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Beach Resort");
    }

    @Test
    void testSearchByCancellationPolicy() {
        HotelSearchCriteria criteria = HotelSearchCriteria.builder()
                .cancellationPolicy("FREE_CANCELLATION")
                .build();

        Specification<Hotel> spec = HotelSpecificationBuilder.buildSpecification(criteria);
        var results = hotelRepository.findAll(spec);

        assertThat(results).hasSize(2);
        assertThat(results).extracting("name").containsExactlyInAnyOrder("Grand Plaza", "Beach Resort");
    }

    @Test
    void testSearchByCancellationPolicy_NonRefundable() {
        HotelSearchCriteria criteria = HotelSearchCriteria.builder()
                .cancellationPolicy("NON_REFUNDABLE")
                .build();

        Specification<Hotel> spec = HotelSpecificationBuilder.buildSpecification(criteria);
        var results = hotelRepository.findAll(spec);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Budget Inn");
    }

    @Test
    void testSearchByChildrenAllowed() {
        HotelSearchCriteria criteria = HotelSearchCriteria.builder()
                .childrenAllowed(true)
                .build();

        Specification<Hotel> spec = HotelSpecificationBuilder.buildSpecification(criteria);
        var results = hotelRepository.findAll(spec);

        assertThat(results).hasSize(3);
    }

    @Test
    void testSearchByChildrenNotAllowed() {
        HotelSearchCriteria criteria = HotelSearchCriteria.builder()
                .childrenAllowed(false)
                .build();

        Specification<Hotel> spec = HotelSpecificationBuilder.buildSpecification(criteria);
        var results = hotelRepository.findAll(spec);

        assertThat(results).isEmpty();
    }

    @Test
    void testSearchByPetsAllowed() {
        HotelSearchCriteria criteria = HotelSearchCriteria.builder()
                .petsAllowed(true)
                .build();

        Specification<Hotel> spec = HotelSpecificationBuilder.buildSpecification(criteria);
        var results = hotelRepository.findAll(spec);

        assertThat(results).hasSize(2);
        assertThat(results).extracting("name").containsExactlyInAnyOrder("Budget Inn", "Beach Resort");
    }

    @Test
    void testSearchBySmokingAllowed() {
        HotelSearchCriteria criteria = HotelSearchCriteria.builder()
                .smokingAllowed(false)
                .build();

        Specification<Hotel> spec = HotelSpecificationBuilder.buildSpecification(criteria);
        var results = hotelRepository.findAll(spec);

        assertThat(results).hasSize(2);
        assertThat(results).extracting("name").containsExactlyInAnyOrder("Grand Plaza", "Beach Resort");
    }

    @Test
    void testSearchByRoomTypeName() {
        HotelSearchCriteria criteria = HotelSearchCriteria.builder()
                .roomTypeName("Suite")
                .build();

        Specification<Hotel> spec = HotelSpecificationBuilder.buildSpecification(criteria);
        var results = hotelRepository.findAll(spec);

        assertThat(results).hasSize(2);
        assertThat(results).extracting("name").containsExactlyInAnyOrder("Grand Plaza", "Beach Resort");
    }

    @Test
    void testSearchByRoomTypeName_Standard() {
        HotelSearchCriteria criteria = HotelSearchCriteria.builder()
                .roomTypeName("Standard")
                .build();

        Specification<Hotel> spec = HotelSpecificationBuilder.buildSpecification(criteria);
        var results = hotelRepository.findAll(spec);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Budget Inn");
    }

    @Test
    void testSearchByBedType() {
        HotelSearchCriteria criteria = HotelSearchCriteria.builder()
                .bedType("King")
                .build();

        Specification<Hotel> spec = HotelSpecificationBuilder.buildSpecification(criteria);
        var results = hotelRepository.findAll(spec);

        assertThat(results).hasSize(2);
        assertThat(results).extracting("name").containsExactlyInAnyOrder("Grand Plaza", "Beach Resort");
    }

    @Test
    void testSearchByBedType_Queen() {
        HotelSearchCriteria criteria = HotelSearchCriteria.builder()
                .bedType("Queen")
                .build();

        Specification<Hotel> spec = HotelSpecificationBuilder.buildSpecification(criteria);
        var results = hotelRepository.findAll(spec);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Budget Inn");
    }

    @Test
    void testSearchByMinCapacity() {
        HotelSearchCriteria criteria = HotelSearchCriteria.builder()
                .minCapacity(3)
                .build();

        Specification<Hotel> spec = HotelSpecificationBuilder.buildSpecification(criteria);
        var results = hotelRepository.findAll(spec);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Beach Resort");
    }

    @Test
    void testSearchByFeatured() {
        HotelSearchCriteria criteria = HotelSearchCriteria.builder()
                .featured(true)
                .build();

        Specification<Hotel> spec = HotelSpecificationBuilder.buildSpecification(criteria);
        var results = hotelRepository.findAll(spec);

        assertThat(results).hasSize(2);
        assertThat(results).extracting("name").containsExactlyInAnyOrder("Grand Plaza", "Beach Resort");
    }

    @Test
    void testSearchByDealTag() {
        HotelSearchCriteria criteria = HotelSearchCriteria.builder()
                .dealTag("SUMMER_SALE")
                .build();

        Specification<Hotel> spec = HotelSpecificationBuilder.buildSpecification(criteria);
        var results = hotelRepository.findAll(spec);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Beach Resort");
    }

    @Test
    void testCombinedSearch_MultipleFilters() {
        HotelSearchCriteria criteria = HotelSearchCriteria.builder()
                .city("New York")
                .starRating(4)
                .amenities(Set.of("Spa"))
                .build();

        Specification<Hotel> spec = HotelSpecificationBuilder.buildSpecification(criteria);
        var results = hotelRepository.findAll(spec);

        // Only Grand Plaza (5 stars) matches - Beach Resort is in Miami
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Grand Plaza");
    }

    @Test
    void testCombinedSearch_WithPriceRange() {
        HotelSearchCriteria criteria = HotelSearchCriteria.builder()
                .city("Miami")
                .minPrice(new BigDecimal("400"))
                .maxPrice(new BigDecimal("600"))
                .build();

        Specification<Hotel> spec = HotelSpecificationBuilder.buildSpecification(criteria);
        var results = hotelRepository.findAll(spec);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Beach Resort");
    }

    @Test
    void testCombinedSearch_WithRoomTypeFilters() {
        HotelSearchCriteria criteria = HotelSearchCriteria.builder()
                .bedType("King")
                .minCapacity(3)
                .build();

        Specification<Hotel> spec = HotelSpecificationBuilder.buildSpecification(criteria);
        var results = hotelRepository.findAll(spec);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Beach Resort");
    }

    @Test
    void testSearch_NoFilters_ReturnsAllHotels() {
        HotelSearchCriteria criteria = HotelSearchCriteria.builder().build();

        Specification<Hotel> spec = HotelSpecificationBuilder.buildSpecification(criteria);
        var results = hotelRepository.findAll(spec);

        assertThat(results).hasSize(3);
    }

    @Test
    void testSearch_WithNonExistentCity_ReturnsEmpty() {
        HotelSearchCriteria criteria = HotelSearchCriteria.builder()
                .city("NonExistentCity")
                .build();

        Specification<Hotel> spec = HotelSpecificationBuilder.buildSpecification(criteria);
        var results = hotelRepository.findAll(spec);

        assertThat(results).isEmpty();
    }

    @Test
    void testSearch_WithPartialNameMatch() {
        HotelSearchCriteria criteria = HotelSearchCriteria.builder()
                .name("Plaza")
                .build();

        Specification<Hotel> spec = HotelSpecificationBuilder.buildSpecification(criteria);
        var results = hotelRepository.findAll(spec);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Grand Plaza");
    }
}