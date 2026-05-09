package org.example.tourism.catalog.hotel;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tourism.catalog.hotel.dto.*;
import org.example.tourism.catalog.shared.CityInfoDto;
import org.example.tourism.catalog.shared.ReviewRequestDto;
import org.example.tourism.catalog.shared.ReviewResponseDto;
import org.example.tourism.common.FileUploadService;
import org.example.tourism.history.SearchHistoryService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

import java.time.LocalDate;

import java.util.HashSet;
import java.util.List;

@RestController
@RequestMapping("/api/hotels")
@RequiredArgsConstructor
@Tag(name = "Hotels", description = "Hotel management and search")
@Slf4j
public class HotelController {
    private final HotelService hotelService;
    private final SearchHistoryService searchHistoryService;
    private final FileUploadService fileUploadService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'HOTEL_MANAGER')")
    @Operation(summary = "Create a new hotel")
    public HotelResponseDto createHotel(
            @Valid @RequestBody HotelRequestDto request,
            Authentication authentication) {

        Jwt jwt = (Jwt) authentication.getPrincipal();
        Long userId = jwt.getClaim("userId");

        return hotelService.createHotel(request, userId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get hotel by ID with details")
    public HotelDetailResponseDto getHotel(@PathVariable Long id) {
        return hotelService.getHotelDetail(id);
    }


    @GetMapping("/featured")
    @Operation(summary = "Get featured hotels")
    public List<HotelSearchResponseDto> getFeaturedHotels() {
        return hotelService.getFeaturedHotels();
    }

    @GetMapping("/search")
    @Operation(summary = "Search hotels by name")
    public List<HotelSearchResponseDto> searchHotelsByName(@RequestParam String name) {
        HotelSearchCriteria criteria = HotelSearchCriteria.builder().name(name).build();
        return hotelService.searchHotels(criteria, Pageable.unpaged()).getContent();
    }

    @GetMapping("/cities")
    @Operation(summary = "Get all available cities")
    public List<CityInfoDto> getAvailableCities() {
        return hotelService.getAvailableCities();
    }

    @GetMapping("/{id}/availability")
    @Operation(summary = "Check hotel availability")
    public HotelAvailabilityDto checkAvailability(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam Integer guests) {
        return hotelService.checkHotelAvailability(id, checkIn, checkOut, guests);
    }
    @GetMapping("/availability/search")
    @Operation(summary = "Search available rooms across all hotels")
    public List<HotelAvailabilityDto> searchAvailableRooms(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam Integer guests,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Integer starRating,
            @RequestParam(required = false) BigDecimal maxPrice) {

        return hotelService.searchAvailableRooms(checkIn, checkOut, guests, city, starRating, maxPrice);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOTEL_MANAGER') and @hotelSecurity.isOwner(#id, authentication)")
    @Operation(summary = "Update hotel")
    public HotelResponseDto updateHotel(
            @PathVariable Long id,
            @Valid @RequestBody HotelRequestDto request,
            Authentication authentication) {
        return hotelService.updateHotel(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete hotel")
    public void deleteHotel(@PathVariable Long id) {
        hotelService.deleteHotel(id);
    }

    @PostMapping("/{id}/upload-images")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOTEL_MANAGER') and @hotelSecurity.isOwner(#id, authentication)")
    @Operation(summary = "Upload hotel images", description = "Upload actual image files for a hotel")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Images uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file format"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Hotel not found")
    })
    public ResponseEntity<List<String>> uploadHotelImages(
            @PathVariable Long id,
            @RequestParam("images") List<MultipartFile> images,
            Authentication authentication) {

        log.info("Uploading {} images for hotel ID: {}", images.size(), id);

        hotelService.getHotelDetail(id);

        List<String> imageUrls = fileUploadService.uploadHotelImages(images);

        if (imageUrls.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        hotelService.addHotelImages(id, imageUrls);

        log.info("Successfully uploaded {} images for hotel ID: {}", imageUrls.size(), id);
        return ResponseEntity.status(HttpStatus.CREATED).body(imageUrls);
    }


    @PostMapping("/{id}/upload-image")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOTEL_MANAGER')")
    @Operation(summary = "Upload a single hotel image", description = "Upload one image file for a hotel")
    public ResponseEntity<String> uploadHotelImage(
            @PathVariable Long id,
            @RequestParam("image") MultipartFile image) {

        List<String> urls = fileUploadService.uploadFiles(List.of(image), "hotels");

        if (urls.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        hotelService.addHotelImages(id, urls);
        return ResponseEntity.status(HttpStatus.CREATED).body(urls.get(0));
    }


    @DeleteMapping("/{hotelId}/images")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOTEL_MANAGER') and @hotelSecurity.isOwner(#hotelId, authentication)")
    @Operation(summary = "Delete hotel image", description = "Remove an image from a hotel (deletes file and removes URL)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Image deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid image URL"),
            @ApiResponse(responseCode = "404", description = "Hotel or image not found")
    })
    public ResponseEntity<Void> deleteHotelImage(
            @PathVariable Long hotelId,
            @RequestParam String imageUrl,
            Authentication authentication) {

        log.info("Deleting image: {} from hotel ID: {}", imageUrl, hotelId);

        boolean fileDeleted = fileUploadService.deleteFile(imageUrl);

        if (!fileDeleted) {
            log.warn("File not found on disk: {}", imageUrl);
        }

        try {
            hotelService.removeHotelImage(hotelId, imageUrl);
        } catch (IllegalArgumentException e) {
            log.error("Failed to remove image from hotel: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }

        log.info("Image deleted successfully: {}", imageUrl);
        return ResponseEntity.noContent().build();
    }


    @PostMapping("/{id}/reviews")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOTEL_MANAGER', 'GUEST')")
    @Operation(summary = "Add a review to a hotel")
    public ResponseEntity<ReviewResponseDto> addHotelReview(
            @PathVariable Long id,
            @Valid @RequestBody ReviewRequestDto request,
            Authentication authentication) {

        Jwt jwt = (Jwt) authentication.getPrincipal();
        Long userId = jwt.getClaim("userId");

        ReviewResponseDto review = hotelService.addHotelReview(id, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(review);
    }

    @GetMapping("/{id}/reviews")
    @Operation(summary = "Get hotel reviews")
    public ResponseEntity<Page<ReviewResponseDto>> getHotelReviews(
            @PathVariable Long id,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            Pageable pageable) {

        Page<ReviewResponseDto> reviews = hotelService.getHotelReviews(id, pageable);
        return ResponseEntity.ok(reviews);
    }

    @PostMapping("/reviews/{reviewId}/helpful")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Mark a review as helpful")
    public ResponseEntity<Void> markReviewHelpful(@PathVariable Long reviewId) {
        hotelService.markReviewHelpful(reviewId);
        return ResponseEntity.ok().build();
    }

    // Update the searchHotels method to save the search
    @GetMapping
    @Operation(summary = "Search hotels with advanced filters")
    public Page<HotelSearchResponseDto> searchHotels(
            // Basic filters
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Integer starRating,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,

            // Date filters
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam(required = false) Integer guests,

            // Policy filters
            @RequestParam(required = false) Boolean childrenAllowed,
            @RequestParam(required = false) Boolean petsAllowed,
            @RequestParam(required = false) Boolean smokingAllowed,
            @RequestParam(required = false) String cancellationPolicy,

            // Amenity filters (can be multiple)
            @RequestParam(required = false) List<String> amenities,

            // Room type filters
            @RequestParam(required = false) String roomTypeName,
            @RequestParam(required = false) String bedType,
            @RequestParam(required = false) Integer minCapacity,

            // Hotel features
            @RequestParam(required = false) Boolean featured,
            @RequestParam(required = false) String dealTag,

            // Sorting
            @RequestParam(required = false) String sortBy,

            @PageableDefault(size = 10) Pageable pageable,
            Authentication authentication) {

        // Save search to history (if authenticated)
        if (authentication != null && authentication.isAuthenticated()) {
            try {
                Jwt jwt = (Jwt) authentication.getPrincipal();
                Long userId = jwt.getClaim("userId");

                searchHistoryService.saveSearch(
                        userId, city, checkIn, checkOut, guests, starRating, maxPrice
                );
            } catch (Exception e) {
                log.warn("Failed to save search history: {}", e.getMessage());
            }
        }

        // Build search criteria
        HotelSearchCriteria criteria = HotelSearchCriteria.builder()
                .name(name)
                .city(city)
                .starRating(starRating)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .checkIn(checkIn)
                .checkOut(checkOut)
                .guests(guests)
                .childrenAllowed(childrenAllowed)
                .petsAllowed(petsAllowed)
                .smokingAllowed(smokingAllowed)
                .cancellationPolicy(cancellationPolicy)
                .amenities(amenities != null ? new HashSet<>(amenities) : null)
                .roomTypeName(roomTypeName)
                .bedType(bedType)
                .minCapacity(minCapacity)
                .featured(featured)
                .dealTag(dealTag)
                .sortBy(sortBy)
                .build();

        return hotelService.searchHotels(criteria, pageable);
    }

}