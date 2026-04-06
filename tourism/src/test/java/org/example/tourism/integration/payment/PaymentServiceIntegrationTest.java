package org.example.tourism.integration.payment;

import org.example.tourism.availability.*;
import org.example.tourism.booking.*;
import org.example.tourism.payment.*;
import org.example.tourism.common.Role;
import org.example.tourism.notification.NotificationService;
import org.example.tourism.security.User;
import org.example.tourism.security.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class PaymentServiceIntegrationTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @MockitoBean
    private AvailabilityService availabilityService;

    @MockitoBean
    private NotificationService notificationService;

    private User testUser;
    private BookingResponseDto testBooking;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new User();
        testUser.setUsername("paymentuser");
        testUser.setPasswordHash("encoded");
        testUser.setEmail("payment@example.com");
        testUser.setFullName("Payment User");
        testUser.setEnabled(true);
        testUser.setRoles(Set.of(Role.GUEST));
        testUser = userRepository.save(testUser);

        // Create a booking
        AvailabilityResponseDto availabilityResponse = new AvailabilityResponseDto();
        availabilityResponse.setAvailable(true);
        availabilityResponse.setTotalPrice(new BigDecimal("299.99"));

        when(availabilityService.checkAvailability(anyLong(), anyLong(), any(), any(), anyInt()))
                .thenReturn(availabilityResponse);

        BookingRequestDto bookingRequest = new BookingRequestDto();
        bookingRequest.setHotelId(1L);
        bookingRequest.setRoomTypeId(1L);
        bookingRequest.setUserId(testUser.getId());
        bookingRequest.setCheckInDate(LocalDate.now().plusDays(10));
        bookingRequest.setCheckOutDate(LocalDate.now().plusDays(12));
        bookingRequest.setGuests(2);

        testBooking = bookingService.createBooking(bookingRequest);
    }

    @Test
    void processPayment_ShouldSucceed_WhenValidBooking() {
        // Given
        PaymentRequestDto paymentRequest = new PaymentRequestDto();
        paymentRequest.setBookingId(testBooking.getId());

        // When
        PaymentResponseDto result = paymentService.processPayment(paymentRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getBookingId()).isEqualTo(testBooking.getId());
        assertThat(result.getAmount()).isEqualTo(new BigDecimal("299.99"));
        assertThat(result.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(result.getTransactionId()).isNotNull();
    }

    @Test
    void processPayment_ShouldThrowException_WhenBookingAlreadyPaid() {
        // Given
        PaymentRequestDto paymentRequest = new PaymentRequestDto();
        paymentRequest.setBookingId(testBooking.getId());

        // First payment - should succeed
        paymentService.processPayment(paymentRequest);

        // When/Then - Second payment should fail with correct message
        assertThatThrownBy(() -> paymentService.processPayment(paymentRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Booking is already confirmed and paid");
    }

    @Test
    void processPayment_ShouldThrowException_WhenBookingCancelled() throws Exception {
        // First cancel the booking using reflection or create a cancelled booking
        // Since cancelBooking might require authentication, we'll create a new cancelled booking

        AvailabilityResponseDto availabilityResponse = new AvailabilityResponseDto();
        availabilityResponse.setAvailable(true);
        availabilityResponse.setTotalPrice(new BigDecimal("199.99"));

        when(availabilityService.checkAvailability(anyLong(), anyLong(), any(), any(), anyInt()))
                .thenReturn(availabilityResponse);

        BookingRequestDto cancelledBookingRequest = new BookingRequestDto();
        cancelledBookingRequest.setHotelId(2L);
        cancelledBookingRequest.setRoomTypeId(2L);
        cancelledBookingRequest.setUserId(testUser.getId());
        cancelledBookingRequest.setCheckInDate(LocalDate.now().plusDays(20));
        cancelledBookingRequest.setCheckOutDate(LocalDate.now().plusDays(22));
        cancelledBookingRequest.setGuests(1);

        BookingResponseDto cancelledBooking = bookingService.createBooking(cancelledBookingRequest);

        // Cancel the booking
        bookingService.cancelBooking(cancelledBooking.getId());

        PaymentRequestDto paymentRequest = new PaymentRequestDto();
        paymentRequest.setBookingId(cancelledBooking.getId());

        // When/Then
        assertThatThrownBy(() -> paymentService.processPayment(paymentRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot pay for cancelled booking");
    }

    @Test
    void getPaymentByBooking_ShouldReturnPayment_WhenExists() {
        // Given
        PaymentRequestDto paymentRequest = new PaymentRequestDto();
        paymentRequest.setBookingId(testBooking.getId());
        PaymentResponseDto created = paymentService.processPayment(paymentRequest);

        // When
        PaymentResponseDto found = paymentService.getPaymentByBooking(testBooking.getId());

        // Then
        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(created.getId());
        assertThat(found.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
    }

    @Test
    void getPaymentByBooking_ShouldThrowException_WhenNotFound() {
        // When/Then
        assertThatThrownBy(() -> paymentService.getPaymentByBooking(9999L))
                .isInstanceOf(jakarta.persistence.EntityNotFoundException.class);
    }

    @Test
    void getPaymentById_ShouldReturnPayment_WhenExists() {
        // Given
        PaymentRequestDto paymentRequest = new PaymentRequestDto();
        paymentRequest.setBookingId(testBooking.getId());
        PaymentResponseDto created = paymentService.processPayment(paymentRequest);

        // When
        PaymentResponseDto found = paymentService.getPaymentById(created.getId());

        // Then
        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(created.getId());
        assertThat(found.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
    }

    @Test
    void getPaymentById_ShouldThrowException_WhenNotFound() {
        // When/Then
        assertThatThrownBy(() -> paymentService.getPaymentById(9999L))
                .isInstanceOf(jakarta.persistence.EntityNotFoundException.class);
    }

    @Test
    void refundPayment_ShouldSucceed_WhenPaymentExists() {
        // Given
        PaymentRequestDto paymentRequest = new PaymentRequestDto();
        paymentRequest.setBookingId(testBooking.getId());
        PaymentResponseDto payment = paymentService.processPayment(paymentRequest);

        // When
        PaymentResponseDto refunded = paymentService.refundPayment(payment.getId());

        // Then
        assertThat(refunded.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        assertThat(refunded.getRefundedAmount()).isEqualTo(new BigDecimal("299.99"));
    }

    @Test
    void refundPayment_ShouldThrowException_WhenPaymentNotFound() {
        // When/Then
        assertThatThrownBy(() -> paymentService.refundPayment(9999L))
                .isInstanceOf(jakarta.persistence.EntityNotFoundException.class);
    }

    @Test
    void getUserPayments_ShouldReturnAllUserPayments() {
        // Given
        PaymentRequestDto paymentRequest = new PaymentRequestDto();
        paymentRequest.setBookingId(testBooking.getId());
        paymentService.processPayment(paymentRequest);

        // Create another booking and payment
        AvailabilityResponseDto availabilityResponse = new AvailabilityResponseDto();
        availabilityResponse.setAvailable(true);
        availabilityResponse.setTotalPrice(new BigDecimal("199.99"));

        when(availabilityService.checkAvailability(anyLong(), anyLong(), any(), any(), anyInt()))
                .thenReturn(availabilityResponse);

        BookingRequestDto secondBookingRequest = new BookingRequestDto();
        secondBookingRequest.setHotelId(2L);
        secondBookingRequest.setRoomTypeId(2L);
        secondBookingRequest.setUserId(testUser.getId());
        secondBookingRequest.setCheckInDate(LocalDate.now().plusDays(15));
        secondBookingRequest.setCheckOutDate(LocalDate.now().plusDays(17));
        secondBookingRequest.setGuests(1);

        BookingResponseDto secondBooking = bookingService.createBooking(secondBookingRequest);

        PaymentRequestDto secondPaymentRequest = new PaymentRequestDto();
        secondPaymentRequest.setBookingId(secondBooking.getId());
        paymentService.processPayment(secondPaymentRequest);

        // When
        var payments = paymentService.getUserPayments(testUser.getId());

        // Then
        assertThat(payments).hasSize(2);
        assertThat(payments).allMatch(p -> p.getStatus() == PaymentStatus.COMPLETED);
    }

    @Test
    void isPaymentOwner_ShouldReturnTrue_WhenUserOwnsPayment() {
        // Given
        PaymentRequestDto paymentRequest = new PaymentRequestDto();
        paymentRequest.setBookingId(testBooking.getId());
        PaymentResponseDto payment = paymentService.processPayment(paymentRequest);

        // When
        boolean isOwner = paymentService.isPaymentOwner(payment.getId(), testUser.getId());

        // Then
        assertThat(isOwner).isTrue();
    }

    @Test
    void isPaymentOwner_ShouldReturnFalse_WhenUserDoesNotOwnPayment() {
        // Given
        PaymentRequestDto paymentRequest = new PaymentRequestDto();
        paymentRequest.setBookingId(testBooking.getId());
        PaymentResponseDto payment = paymentService.processPayment(paymentRequest);

        // When
        boolean isOwner = paymentService.isPaymentOwner(payment.getId(), 9999L);

        // Then
        assertThat(isOwner).isFalse();
    }

    @Test
    void validateUserOwnsBooking_ShouldReturnTrue_WhenUserOwnsBooking() {
        // When
        boolean ownsBooking = paymentService.validateUserOwnsBooking(testBooking.getId(), testUser.getId());

        // Then
        assertThat(ownsBooking).isTrue();
    }

    @Test
    void validateUserOwnsBooking_ShouldReturnFalse_WhenUserDoesNotOwnBooking() {
        // When
        boolean ownsBooking = paymentService.validateUserOwnsBooking(testBooking.getId(), 9999L);

        // Then
        assertThat(ownsBooking).isFalse();
    }
}