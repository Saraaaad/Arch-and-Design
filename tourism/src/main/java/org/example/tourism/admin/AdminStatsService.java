package org.example.tourism.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tourism.booking.Booking;
import org.example.tourism.booking.BookingRepository;
import org.example.tourism.booking.BookingStatus;
import org.example.tourism.catalog.hotel.Hotel;
import org.example.tourism.catalog.hotel.HotelRepository;
import org.example.tourism.payment.Payment;
import org.example.tourism.payment.PaymentRepository;
import org.example.tourism.payment.PaymentStatus;
import org.example.tourism.security.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminStatsService {

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final HotelRepository hotelRepository;
    private final PaymentRepository paymentRepository;

    @Transactional(readOnly = true)
    public DashboardStatsDto getDashboardStats() {
        log.info("Fetching admin dashboard stats");

        TotalStatsDto totalStats = getTotalStats();
        Map<String, MonthlyStatsDto> monthlyStats = getMonthlyStats();
        List<TopHotelDto> topHotels = getTopHotels();
        OccupancyStatsDto occupancyStats = getOccupancyStats();

        return DashboardStatsDto.builder()
                .totalStats(totalStats)
                .monthlyStats(monthlyStats)
                .topHotels(topHotels)
                .occupancyStats(occupancyStats)
                .build();
    }

    private TotalStatsDto getTotalStats() {
        Long totalUsers = userRepository.count();
        Long totalHotels = hotelRepository.count();

        // Use database queries instead of loading all data
        Long totalBookings = bookingRepository.countByStatus(BookingStatus.CONFIRMED);
        Long totalRevenue = paymentRepository.sumAmountByStatus(PaymentStatus.COMPLETED);
        Long pendingBookings = bookingRepository.countByStatus(BookingStatus.PENDING);
        Long confirmedBookings = bookingRepository.countByStatus(BookingStatus.CONFIRMED);
        Long cancelledBookings = bookingRepository.countByStatus(BookingStatus.CANCELLED);

        return TotalStatsDto.builder()
                .totalUsers(totalUsers)
                .totalHotels(totalHotels)
                .totalBookings(totalBookings)
                .totalRevenue(totalRevenue)
                .pendingBookings(pendingBookings)
                .confirmedBookings(confirmedBookings)
                .cancelledBookings(cancelledBookings)
                .build();
    }
    private Map<String, MonthlyStatsDto> getMonthlyStats() {
        Map<String, MonthlyStatsDto> stats = new LinkedHashMap<>();
        LocalDateTime now = LocalDateTime.now();

        for (int i = 5; i >= 0; i--) {
            YearMonth yearMonth = YearMonth.from(now.minusMonths(i));
            String monthKey = yearMonth.toString();

            LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
            LocalDateTime end = yearMonth.atEndOfMonth().atTime(23, 59, 59);

            List<Booking> bookingsInMonth = bookingRepository.findAll().stream()
                    .filter(b -> b.getCreatedAt() != null)
                    .filter(b -> !b.getCreatedAt().isBefore(start) && !b.getCreatedAt().isAfter(end))
                    .filter(b -> b.getStatus() == BookingStatus.CONFIRMED)
                    .collect(Collectors.toList());

            BigDecimal revenue = bookingsInMonth.stream()
                    .map(Booking::getTotalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            stats.put(yearMonth.getMonth().toString() + " " + yearMonth.getYear(),
                    MonthlyStatsDto.builder()
                            .month(yearMonth.getMonth().toString() + " " + yearMonth.getYear())
                            .bookingsCount((long) bookingsInMonth.size())
                            .revenue(revenue)
                            .build());
        }

        return stats;
    }

    private List<TopHotelDto> getTopHotels() {
        List<Booking> confirmedBookings = bookingRepository.findAll().stream()
                .filter(b -> b.getStatus() == BookingStatus.CONFIRMED)
                .collect(Collectors.toList());

        Map<Long, Long> bookingCounts = confirmedBookings.stream()
                .collect(Collectors.groupingBy(Booking::getHotelId, Collectors.counting()));

        Map<Long, BigDecimal> revenueByHotel = confirmedBookings.stream()
                .collect(Collectors.groupingBy(
                        Booking::getHotelId,
                        Collectors.mapping(Booking::getTotalPrice,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));

        return bookingCounts.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(5)
                .map(entry -> {
                    Long hotelId = entry.getKey();
                    Hotel hotel = hotelRepository.findById(hotelId).orElse(null);
                    return TopHotelDto.builder()
                            .hotelId(hotelId)
                            .hotelName(hotel != null ? hotel.getName() : "Unknown Hotel")
                            .bookingCount(entry.getValue())
                            .totalRevenue(revenueByHotel.getOrDefault(hotelId, BigDecimal.ZERO))
                            .build();
                })
                .collect(Collectors.toList());
    }

    private OccupancyStatsDto getOccupancyStats() {
        Map<Long, Double> hotelOccupancyRates = new HashMap<>();

        List<Hotel> hotels = hotelRepository.findAll();
        LocalDate today = LocalDate.now();
        LocalDate nextMonth = today.plusMonths(1);

        for (Hotel hotel : hotels) {
            int totalRooms = hotel.getTotalRooms() != null && hotel.getTotalRooms() > 0
                    ? hotel.getTotalRooms() : 100;

            List<Booking> upcomingBookings = bookingRepository.findAll().stream()
                    .filter(b -> b.getHotelId().equals(hotel.getId()))
                    .filter(b -> b.getStatus() == BookingStatus.CONFIRMED)
                    .filter(b -> b.getCheckInDate().isAfter(today) && b.getCheckInDate().isBefore(nextMonth))
                    .collect(Collectors.toList());

            double occupancyRate = totalRooms > 0 ?
                    (upcomingBookings.size() * 100.0) / (totalRooms * 30) : 0;

            hotelOccupancyRates.put(hotel.getId(), Math.min(occupancyRate, 100.0));
        }

        double overallOccupancy = hotelOccupancyRates.values().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        return OccupancyStatsDto.builder()
                .overallOccupancyRate(Math.round(overallOccupancy * 10) / 10.0)
                .hotelOccupancyRates(hotelOccupancyRates)
                .build();
    }
}