package org.example.tourism.unit.payment;

import org.example.tourism.booking.BookingResponseDto;
import org.example.tourism.booking.BookingService;
import org.example.tourism.booking.BookingStatus;
import org.example.tourism.notification.NotificationService;
import org.example.tourism.payment.*;
import org.example.tourism.security.User;
import org.example.tourism.security.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private BookingService bookingService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private PaymentRequestDto paymentRequest;
    private BookingResponseDto bookingResponse;
    private Payment payment;
    private User testUser;

    @BeforeEach
    void setUp() {
        paymentRequest = new PaymentRequestDto();
        paymentRequest.setBookingId(1L);

        bookingResponse = new BookingResponseDto();
        bookingResponse.setId(1L);
        bookingResponse.setUserId(100L);
        bookingResponse.setTotalPrice(new BigDecimal("299.99"));
        bookingResponse.setStatus(BookingStatus.PENDING);
        bookingResponse.setHotelId(1L);
        bookingResponse.setCheckInDate(LocalDateTime.now().plusDays(10).toLocalDate());
        bookingResponse.setCheckOutDate(LocalDateTime.now().plusDays(12).toLocalDate());

        payment = new Payment();
        payment.setId(1L);
        payment.setBookingId(1L);
        payment.setUserId(100L);
        payment.setAmount(new BigDecimal("299.99"));
        payment.setStatus(PaymentStatus.PENDING);
        payment.setTransactionId("TXN-123");
        payment.setPaymentMethod("CREDIT_CARD");
        payment.setRefundedAmount(BigDecimal.ZERO);
        payment.setCreatedAt(LocalDateTime.now());

        testUser = new User();
        testUser.setId(100L);
        testUser.setEmail("test@example.com");
        testUser.setUsername("testuser");
    }

    @Test
    void processPayment_ShouldSucceed_WhenValidRequest() {
        // Given
        when(bookingService.getBooking(1L)).thenReturn(bookingResponse);
        when(paymentRepository.findByBookingId(1L)).thenReturn(Optional.empty());

        // Simulate save returning the payment (the service then modifies it)
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment savedPayment = invocation.getArgument(0);
            savedPayment.setId(1L);
            return savedPayment;
        });

        when(bookingService.confirmBooking(1L)).thenReturn(bookingResponse);
        when(userRepository.findById(100L)).thenReturn(Optional.of(testUser));

        // When
        PaymentResponseDto result = paymentService.processPayment(paymentRequest);

        // Then - Verify the result has COMPLETED status
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getBookingId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(result.getAmount()).isEqualTo(new BigDecimal("299.99"));

        // Verify save was called (at least once)
        verify(paymentRepository, atLeastOnce()).save(any(Payment.class));

        // Verify the booking was confirmed
        verify(bookingService, times(1)).confirmBooking(1L);

        // Verify notification was sent
        verify(notificationService, times(1)).sendBookingConfirmation(anyString(), anyLong(), anyString());
    }

    @Test
    void processPayment_ShouldThrowException_WhenBookingCancelled() {
        // Given
        bookingResponse.setStatus(BookingStatus.CANCELLED);
        when(bookingService.getBooking(1L)).thenReturn(bookingResponse);

        // When/Then
        assertThatThrownBy(() -> paymentService.processPayment(paymentRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot pay for cancelled booking");

        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void processPayment_ShouldThrowException_WhenBookingAlreadyConfirmed() {
        // Given
        bookingResponse.setStatus(BookingStatus.CONFIRMED);
        when(bookingService.getBooking(1L)).thenReturn(bookingResponse);

        // When/Then
        assertThatThrownBy(() -> paymentService.processPayment(paymentRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Booking is already confirmed and paid");
    }

    @Test
    void processPayment_ShouldReturnExistingPayment_WhenPendingPaymentExists() {
        // Given
        when(bookingService.getBooking(1L)).thenReturn(bookingResponse);
        when(paymentRepository.findByBookingId(1L)).thenReturn(Optional.of(payment));

        // When
        PaymentResponseDto result = paymentService.processPayment(paymentRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo(PaymentStatus.PENDING);
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void getPaymentByBooking_ShouldReturnPayment_WhenExists() {
        // Given
        payment.setStatus(PaymentStatus.COMPLETED);
        when(paymentRepository.findByBookingId(1L)).thenReturn(Optional.of(payment));

        // When
        PaymentResponseDto result = paymentService.getPaymentByBooking(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getBookingId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
    }

    @Test
    void getPaymentByBooking_ShouldThrowException_WhenNotFound() {
        // Given
        when(paymentRepository.findByBookingId(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> paymentService.getPaymentByBooking(999L))
                .isInstanceOf(jakarta.persistence.EntityNotFoundException.class);
    }

    @Test
    void getPaymentById_ShouldReturnPayment_WhenExists() {
        // Given
        payment.setStatus(PaymentStatus.COMPLETED);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        // When
        PaymentResponseDto result = paymentService.getPaymentById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getBookingId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
    }

    @Test
    void getPaymentById_ShouldThrowException_WhenNotFound() {
        // Given
        when(paymentRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> paymentService.getPaymentById(999L))
                .isInstanceOf(jakarta.persistence.EntityNotFoundException.class);
    }

    @Test
    void refundPayment_ShouldSucceed_WhenValidRequest() {
        // Given
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setCreatedAt(LocalDateTime.now().minusDays(1));

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(bookingService.getBooking(1L)).thenReturn(bookingResponse);
        when(userRepository.findById(100L)).thenReturn(Optional.of(testUser));
        when(bookingService.cancelBooking(1L)).thenReturn(bookingResponse);

        // When
        PaymentResponseDto result = paymentService.refundPayment(1L);

        // Then
        assertThat(result.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        assertThat(result.getRefundedAmount()).isEqualTo(new BigDecimal("299.99"));
        verify(bookingService, times(1)).cancelBooking(1L);
        verify(notificationService, times(1)).sendNotification(anyString(), anyString(), anyString());
    }

    @Test
    void refundPayment_ShouldThrowException_WhenPaymentNotCompleted() {
        // Given
        payment.setStatus(PaymentStatus.PENDING);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        // When/Then
        assertThatThrownBy(() -> paymentService.refundPayment(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only completed payments can be refunded");
    }

    @Test
    void refundPayment_ShouldThrowException_WhenPaymentAlreadyRefunded() {
        // Given
        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setCreatedAt(LocalDateTime.now().minusDays(1));

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        // When/Then
        assertThatThrownBy(() -> paymentService.refundPayment(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only completed payments can be refunded. Current status: REFUNDED");
    }

    @Test
    void refundPayment_ShouldThrowException_WhenRefundPeriodExpired() {
        // Given
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setCreatedAt(LocalDateTime.now().minusDays(8));
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        // When/Then
        assertThatThrownBy(() -> paymentService.refundPayment(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Refund period has expired");
    }

    @Test
    void getUserPayments_ShouldReturnPayments_WhenUserHasPayments() {
        // Given
        payment.setStatus(PaymentStatus.COMPLETED);
        when(paymentRepository.findPaymentsByUserId(100L)).thenReturn(List.of(payment));

        // When
        List<PaymentResponseDto> results = paymentService.getUserPayments(100L);

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getBookingId()).isEqualTo(1L);
        assertThat(results.get(0).getAmount()).isEqualTo(new BigDecimal("299.99"));
        assertThat(results.get(0).getStatus()).isEqualTo(PaymentStatus.COMPLETED);
    }

    @Test
    void getUserPayments_ShouldReturnEmptyList_WhenUserHasNoPayments() {
        // Given
        when(paymentRepository.findPaymentsByUserId(100L)).thenReturn(List.of());

        // When
        List<PaymentResponseDto> results = paymentService.getUserPayments(100L);

        // Then
        assertThat(results).isEmpty();
    }

    @Test
    void isPaymentOwner_ShouldReturnTrue_WhenUserOwnsPayment() {
        // Given
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(bookingService.getBooking(1L)).thenReturn(bookingResponse);

        // When
        boolean isOwner = paymentService.isPaymentOwner(1L, 100L);

        // Then
        assertThat(isOwner).isTrue();
    }

    @Test
    void isPaymentOwner_ShouldReturnFalse_WhenUserDoesNotOwnPayment() {
        // Given
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(bookingService.getBooking(1L)).thenReturn(bookingResponse);

        // When
        boolean isOwner = paymentService.isPaymentOwner(1L, 999L);

        // Then
        assertThat(isOwner).isFalse();
    }

    @Test
    void isPaymentOwner_ShouldThrowException_WhenPaymentNotFound() {
        // Given
        when(paymentRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> paymentService.isPaymentOwner(999L, 100L))
                .isInstanceOf(jakarta.persistence.EntityNotFoundException.class);
    }

    @Test
    void validateUserOwnsBooking_ShouldReturnTrue_WhenUserOwnsBooking() {
        // Given
        when(bookingService.getBooking(1L)).thenReturn(bookingResponse);

        // When
        boolean ownsBooking = paymentService.validateUserOwnsBooking(1L, 100L);

        // Then
        assertThat(ownsBooking).isTrue();
    }

    @Test
    void validateUserOwnsBooking_ShouldReturnFalse_WhenUserDoesNotOwnBooking() {
        // Given
        when(bookingService.getBooking(1L)).thenReturn(bookingResponse);

        // When
        boolean ownsBooking = paymentService.validateUserOwnsBooking(1L, 999L);

        // Then
        assertThat(ownsBooking).isFalse();
    }

    @Test
    void validateUserOwnsBooking_ShouldThrowException_WhenBookingNotFound() {
        // Given
        when(bookingService.getBooking(999L)).thenThrow(new jakarta.persistence.EntityNotFoundException());

        // When/Then
        assertThatThrownBy(() -> paymentService.validateUserOwnsBooking(999L, 100L))
                .isInstanceOf(jakarta.persistence.EntityNotFoundException.class);
    }
}