package org.example.tourism.catalog.hotel;

import jakarta.persistence.criteria.*;
import org.example.tourism.catalog.roomtype.RoomType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class HotelSpecificationBuilder {

    public static Specification<Hotel> buildSpecification(HotelSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(criteria.getName())) {
                predicates.add(cb.like(cb.lower(root.get("name")),
                        "%" + criteria.getName().toLowerCase() + "%"));
            }

            if (StringUtils.hasText(criteria.getCity())) {
                predicates.add(cb.like(cb.lower(root.get("city")),
                        "%" + criteria.getCity().toLowerCase() + "%"));
            }

            if (criteria.getStarRating() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("starRating"),
                        criteria.getStarRating()));
            }

            if (criteria.getMinPrice() != null || criteria.getMaxPrice() != null) {
                Join<Hotel, RoomType> roomTypeJoin = root.join("roomTypes", JoinType.LEFT);

                if (criteria.getMinPrice() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(roomTypeJoin.get("basePrice"),
                            criteria.getMinPrice()));
                }
                if (criteria.getMaxPrice() != null) {
                    predicates.add(cb.lessThanOrEqualTo(roomTypeJoin.get("basePrice"),
                            criteria.getMaxPrice()));
                }
            }

            if (criteria.getChildrenAllowed() != null) {
                predicates.add(cb.equal(root.get("childrenAllowed"), criteria.getChildrenAllowed()));
            }
            if (criteria.getPetsAllowed() != null) {
                predicates.add(cb.equal(root.get("petsAllowed"), criteria.getPetsAllowed()));
            }
            if (criteria.getSmokingAllowed() != null) {
                predicates.add(cb.equal(root.get("smokingAllowed"), criteria.getSmokingAllowed()));
            }
            if (StringUtils.hasText(criteria.getCancellationPolicy())) {
                predicates.add(cb.equal(root.get("cancellationPolicy"), criteria.getCancellationPolicy()));
            }

            if (criteria.getAmenities() != null && !criteria.getAmenities().isEmpty()) {
                for (String amenity : criteria.getAmenities()) {
                    predicates.add(cb.isMember(amenity, root.get("amenities")));
                }
            }

            if (StringUtils.hasText(criteria.getRoomTypeName()) ||
                    StringUtils.hasText(criteria.getBedType()) ||
                    criteria.getMinCapacity() != null) {

                Join<Hotel, RoomType> roomTypeJoin = root.join("roomTypes", JoinType.LEFT);

                if (StringUtils.hasText(criteria.getRoomTypeName())) {
                    predicates.add(cb.like(cb.lower(roomTypeJoin.get("name")),
                            "%" + criteria.getRoomTypeName().toLowerCase() + "%"));
                }
                if (StringUtils.hasText(criteria.getBedType())) {
                    predicates.add(cb.like(cb.lower(roomTypeJoin.get("bedType")),
                            "%" + criteria.getBedType().toLowerCase() + "%"));
                }
                if (criteria.getMinCapacity() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(roomTypeJoin.get("capacity"),
                            criteria.getMinCapacity()));
                }
            }

            if (criteria.getFeatured() != null) {
                predicates.add(cb.equal(root.get("featured"), criteria.getFeatured()));
            }

            if (StringUtils.hasText(criteria.getDealTag())) {
                predicates.add(cb.equal(root.get("dealTag"), criteria.getDealTag()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}