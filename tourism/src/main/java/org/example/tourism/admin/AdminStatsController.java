package org.example.tourism.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/stats")
@RequiredArgsConstructor
@Tag(name = "Admin Dashboard", description = "Admin dashboard statistics endpoints")
@PreAuthorize("hasRole('ADMIN')")
public class AdminStatsController {

    private final AdminStatsService adminStatsService;

    @GetMapping("/overview")
    @Operation(summary = "Get dashboard overview stats", description = "Get total stats for admin dashboard")
    public DashboardStatsDto getDashboardStats() {
        return adminStatsService.getDashboardStats();
    }

    @GetMapping("/total")
    @Operation(summary = "Get total stats", description = "Get total counts for users, hotels, bookings, and revenue")
    public TotalStatsDto getTotalStats() {
        return adminStatsService.getDashboardStats().getTotalStats();
    }

    @GetMapping("/monthly")
    @Operation(summary = "Get monthly stats", description = "Get monthly booking and revenue statistics for the last 6 months")
    public java.util.Map<String, MonthlyStatsDto> getMonthlyStats() {
        return adminStatsService.getDashboardStats().getMonthlyStats();
    }

    @GetMapping("/top-hotels")
    @Operation(summary = "Get top hotels", description = "Get top 5 hotels by booking count and revenue")
    public java.util.List<TopHotelDto> getTopHotels() {
        return adminStatsService.getDashboardStats().getTopHotels();
    }
}