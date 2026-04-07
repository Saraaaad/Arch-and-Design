[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-22041afd0340ce965d47ae6ef1cefeee28c7c493a6346c4f15d667ab976d596c.svg)](https://classroom.github.com/a/CU6l4amx)
# Tourism Hotel Booking System
### Step 1 - Monolith Implementation



## Project Overview

A comprehensive hotel booking backend system developed as part of the SWER313 course project. The system enables guests to search hotels, check room availability, create bookings, process payments, and receive notifications. Hotel managers and administrators can manage hotel listings, room types, pricing rules, and booking policies.

## 👥 Team Members

- **Sara Dumaeri**
- **Selina Al Allam**

## 📁 Project Structure
````
|   README.md
|       
\---tourism
    |   pom.xml  
    +---src
    |   +---main
    |   |   +---java
    |   |   |   \---org
    |   |   |       \---example
    |   |   |           \---tourism
    |   |   |               |   TourismApplication.java
    |   |   |               |   
    |   |   |               +---admin
    |   |   |               |       AdminStatsController.java
    |   |   |               |       AdminStatsService.java
    |   |   |               |       DashboardStatsDto.java
    |   |   |               |       MonthlyStatsDto.java
    |   |   |               |       OccupancyStatsDto.java
    |   |   |               |       TopHotelDto.java
    |   |   |               |       TotalStatsDto.java
    |   |   |               |       
    |   |   |               +---availability
    |   |   |               |       AvailabilityController.java
    |   |   |               |       AvailabilityResponseDto.java
    |   |   |               |       AvailabilityService.java
    |   |   |               |       AvailabilityServiceImpl.java
    |   |   |               |       PricingService.java
    |   |   |               |       PricingServiceImpl.java
    |   |   |               |       
    |   |   |               +---booking
    |   |   |               |       Booking.java
    |   |   |               |       BookingController.java
    |   |   |               |       BookingRepository.java
    |   |   |               |       BookingRequestDto.java
    |   |   |               |       BookingResponseDto.java
    |   |   |               |       BookingService.java
    |   |   |               |       BookingServiceImpl.java
    |   |   |               |       BookingStatus.java
    |   |   |               |       CreateBookingRequest.java
    |   |   |               |       
    |   |   |               +---catalog
    |   |   |               |   +---hotel
    |   |   |               |   |   |   Hotel.java
    |   |   |               |   |   |   HotelController.java
    |   |   |               |   |   |   HotelRepository.java
    |   |   |               |   |   |   HotelReview.java
    |   |   |               |   |   |   HotelReviewRepository.java
    |   |   |               |   |   |   HotelSearchCriteria.java
    |   |   |               |   |   |   HotelService.java
    |   |   |               |   |   |   HotelServiceImpl.java
    |   |   |               |   |   |   HotelSpecificationBuilder.java
    |   |   |               |   |   |   
    |   |   |               |   |   \---dto
    |   |   |               |   |           HotelAvailabilityDto.java
    |   |   |               |   |           HotelDetailResponseDto.java
    |   |   |               |   |           HotelRequestDto.java
    |   |   |               |   |           HotelResponseDto.java
    |   |   |               |   |           HotelSearchResponseDto.java
    |   |   |               |   |           
    |   |   |               |   +---roomtype
    |   |   |               |   |   |   RoomType.java
    |   |   |               |   |   |   RoomTypeController.java
    |   |   |               |   |   |   RoomTypeRepository.java
    |   |   |               |   |   |   RoomTypeService.java
    |   |   |               |   |   |   RoomTypeServiceImpl.java
    |   |   |               |   |   |   
    |   |   |               |   |   \---dto
    |   |   |               |   |           RoomTypeAvailabilityDto.java
    |   |   |               |   |           RoomTypeRequestDto.java
    |   |   |               |   |           RoomTypeResponseDto.java
    |   |   |               |   |           RoomTypeSummaryDto.java
    |   |   |               |   |           
    |   |   |               |   \---shared
    |   |   |               |           CityInfoDto.java
    |   |   |               |           PriceBreakdownDto.java
    |   |   |               |           ReviewRequestDto.java
    |   |   |               |           ReviewResponseDto.java
    |   |   |               |           
    |   |   |               +---common
    |   |   |               |       ApiError.java
    |   |   |               |       BookingAlreadyCancelledException.java
    |   |   |               |       BookingNotAvailableException.java
    |   |   |               |       CancellationNotAllowedException.java
    |   |   |               |       CannotWishlistOwnHotelException.java
    |   |   |               |       DateTooFarException.java
    |   |   |               |       DuplicateResourceException.java
    |   |   |               |       EmptyFileException.java
    |   |   |               |       FileUploadService.java
    |   |   |               |       GlobalExceptionHandler.java
    |   |   |               |       HotelHasActiveBookingsException.java
    |   |   |               |       InvalidTokenException.java
    |   |   |               |       PaymentAlreadyRefundedException.java
    |   |   |               |       PaymentFailedException.java
    |   |   |               |       ReviewNotAllowedException.java
    |   |   |               |       Role.java
    |   |   |               |       
    |   |   |               +---config
    |   |   |               |       LoadDatabase.java
    |   |   |               |       OpenApiConfig.java
    |   |   |               |       SecurityConfig.java
    |   |   |               |       WebConfig.java
    |   |   |               |       
    |   |   |               +---history
    |   |   |               |       SearchHistory.java
    |   |   |               |       SearchHistoryController.java
    |   |   |               |       SearchHistoryRepository.java
    |   |   |               |       SearchHistoryService.java
    |   |   |               |       
    |   |   |               +---notification
    |   |   |               |       NotificationService.java
    |   |   |               |       
    |   |   |               +---payment
    |   |   |               |       Payment.java
    |   |   |               |       PaymentController.java
    |   |   |               |       PaymentRepository.java
    |   |   |               |       PaymentRequestDto.java
    |   |   |               |       PaymentResponseDto.java
    |   |   |               |       PaymentService.java
    |   |   |               |       PaymentServiceImpl.java
    |   |   |               |       PaymentStatus.java
    |   |   |               |       
    |   |   |               +---security
    |   |   |               |       AuthController.java
    |   |   |               |       AuthorizationService.java
    |   |   |               |       AuthResponse.java
    |   |   |               |       AuthService.java
    |   |   |               |       ChangePasswordRequest.java
    |   |   |               |       CustomUserDetailsService.java
    |   |   |               |       JwtTokenService.java
    |   |   |               |       LoginRequest.java
    |   |   |               |       ProfileController.java
    |   |   |               |       ProfileUpdateRequest.java
    |   |   |               |       RefreshToken.java
    |   |   |               |       RefreshTokenRepository.java
    |   |   |               |       RefreshTokenRequest.java
    |   |   |               |       RegisterRequest.java
    |   |   |               |       UpdateRolesRequest.java
    |   |   |               |       User.java
    |   |   |               |       UserDto.java
    |   |   |               |       UserManagementController.java
    |   |   |               |       UserRepository.java
    |   |   |               |       
    |   |   |               \---wishlist
    |   |   |                       Wishlist.java
    |   |   |                       WishlistCheckResponse.java
    |   |   |                       WishlistController.java
    |   |   |                       WishlistRepository.java
    |   |   |                       WishlistResponseDto.java
    |   |   |                       WishlistService.java
    |   |   |                       
    |   |   \---resources
    |   |           application.properties
    |   |           
    |   \---test
    |       +---java
    |       |   \---org
    |       |       \---example
    |       |           \---tourism
    |       |               +---integration
    |       |               |   +---booking
    |       |               |   |       BookingServiceImplIntegrationTest.java
    |       |               |   |       
    |       |               |   +---catalog
    |       |               |   |   +---hotel
    |       |               |   |   |       HotelServiceIntegrationTest.java
    |       |               |   |   |       HotelSpecificationBuilderIntegrationTest.java
    |       |               |   |   |       
    |       |               |   |   \---roomtype
    |       |               |   |           RoomTypeServiceIntegrationTest.java
    |       |               |   |           
    |       |               |   \---payment
    |       |               |           PaymentServiceIntegrationTest.java
    |       |               |           
    |       |               \---unit
    |       |                   +---admin
    |       |                   |       AdminStatsServiceTest.java
    |       |                   |       
    |       |                   +---availability
    |       |                   |       AvailabilityServiceImplTest.java
    |       |                   |       PricingServiceImplTest.java
    |       |                   |       
    |       |                   +---booking
    |       |                   |       BookingServiceImplTest.java
    |       |                   |       
    |       |                   +---catalog
    |       |                   |   +---hotel
    |       |                   |   |       HotelServiceImplTest.java
    |       |                   |   |       
    |       |                   |   \---roomtype
    |       |                   |           RoomTypeServiceImplTest.java
    |       |                   |           
    |       |                   +---history
    |       |                   |       SearchHistoryServiceTest.java
    |       |                   |       
    |       |                   +---payment
    |       |                   |       PaymentServiceImplTest.java
    |       |                   |       
    |       |                   +---security
    |       |                   |       AuthServiceTest.java
    |       |                   |       
    |       |                   \---wishlist
    |       |                           WishlistServiceTest.java
    |       |                           
    |       \---resources
    |               application-test.properties
    |
````

## Core Features :


####  API documentation at: http://localhost:8080/swagger-ui.html

#### 1. Hotel Catalog
- Hotel CRUD operations (Admin/Hotel Manager only)
- Room types CRUD with capacity, pricing, and amenities
- Browse hotels with advanced filters and pagination
- Hotel details with room type listings
- Featured hotels display
- City-based search with hotel counts

#### 2. Availability & Pricing
- Date range availability checking
- Guest capacity validation
- Weekend pricing multiplier (20% increase for Saturday/Sunday)
- Real-time double-booking prevention
- Available rooms search across all hotels

#### 3. Booking System
- Create booking (PENDING status)
- Confirm booking (Admin only)
- Cancel booking with validation
- Guest booking history
- Hotel manager booking overview
- Automatic total price calculation

#### 4. Payment Processing
- Mock payment processing
- Payment status tracking (PENDING → COMPLETED → REFUNDED)
- Transaction ID generation
- Refund within 7-day window
- Payment history per user

#### 5. Notifications
- Booking confirmation emails (simulated)
- Cancellation notifications (simulated)
- Console-based email simulation for testing

#### 6. Additional Features
- User authentication with JWT
- Role-based access control (ADMIN, HOTEL_MANAGER, GUEST)
- Refresh token mechanism with rotation
- Wishlist management
- Hotel reviews with ratings
- Search history tracking
- Admin dashboard with statistics
- File upload for hotel images
- Global exception handling


### Default Users

| Username | Password | Role |
|----------|----------|------|
| admin | admin123 | ADMIN |
| manager | manager123 | HOTEL_MANAGER |
| guest | guest123 | GUEST |

## API Endpoints

### Authentication

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /auth/register | Register new user |
| POST | /auth/login | Login and get tokens |
| POST | /auth/refresh | Refresh access token |
| POST | /auth/revoke | Revoke refresh token |

### Hotels

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/hotels | Get all hotels (with filters) |
| GET | /api/hotels/{id} | Get hotel by ID |
| POST | /api/hotels | Create hotel (Admin/Manager) |
| PUT | /api/hotels/{id} | Update hotel |
| DELETE | /api/hotels/{id} | Delete hotel |
| GET | /api/hotels/featured | Get featured hotels |
| GET | /api/hotels/cities | Get available cities |
| GET | /api/hotels/availability/search | Search available rooms |

### Room Types

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/room-types | Get all room types |
| GET | /api/room-types/{id} | Get room type by ID |
| POST | /api/room-types | Create room type |
| PUT | /api/room-types/{id} | Update room type |
| DELETE | /api/room-types/{id} | Delete room type |

### Availability

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/availability/check | Check room availability |

### Bookings

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/bookings | Create booking |
| GET | /api/bookings/{id} | Get booking by ID |
| POST | /api/bookings/{id}/confirm | Confirm booking |
| POST | /api/bookings/{id}/cancel | Cancel booking |
| GET | /api/bookings/user/me | Get my bookings |
| GET | /api/bookings/hotel/{hotelId} | Get hotel bookings |

### Payments

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/payments | Process payment |
| GET | /api/payments/{id} | Get payment by ID |
| GET | /api/payments/booking/{bookingId} | Get payment by booking |
| POST | /api/payments/{id}/refund | Refund payment |
| GET | /api/payments/user/me | Get my payments |

### Wishlist

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/wishlist | Add to wishlist |
| DELETE | /api/wishlist/{hotelId} | Remove from wishlist |
| GET | /api/wishlist/me | Get my wishlist |
| GET | /api/wishlist/me/check/{hotelId} | Check if in wishlist |
| GET | /api/wishlist/count/{hotelId} | Get wishlist count |

### Reviews

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/hotels/{id}/reviews | Add hotel review |
| GET | /api/hotels/{id}/reviews | Get hotel reviews |
| POST | /api/hotels/reviews/{id}/helpful | Mark review as helpful |

### Profile

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/profile/me | Get my profile |
| PUT | /api/profile/me | Update profile |
| PUT | /api/profile/me/password | Change password |

### Search History

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/history/searches | Get search history |
| GET | /api/history/searches/recent | Get recent searches |
| DELETE | /api/history/searches | Clear search history |

### Admin Dashboard

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/admin/stats/overview | Get dashboard stats |
| GET | /api/admin/stats/total | Get total stats |
| GET | /api/admin/stats/monthly | Get monthly stats |
| GET | /api/admin/stats/top-hotels | Get top hotels |

## Database Schema

### Core Tables

| Table | Description |
|-------|-------------|
| users | User accounts and authentication |
| user_roles | User role assignments |
| hotels | Hotel information and policies |
| room_types | Room categories and pricing |
| bookings | Booking records |
| payments | Payment transactions |
| wishlists | User wishlist items |
| hotel_reviews | Guest reviews and ratings |
| search_history | User search logs |
| refresh_tokens | JWT refresh tokens |

### Sample Data

- **50 Hotels** across major cities (New York, Los Angeles, Miami, Las Vegas, London, Paris, Rome, Tokyo, Singapore, Bangkok, Dubai, Sydney, Toronto)
- **Different Room Types** with realistic pricing ($15 - $25,000)
- **3 Default Users** with different roles

### Technologies Used
- Java 21
- Spring Boot
- Spring Security with JWT
- Spring Data JPA with H2 Database
- Swagger for API documentation
- JUnit and Mockito for testing