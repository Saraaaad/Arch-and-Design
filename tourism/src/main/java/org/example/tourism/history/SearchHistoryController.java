package org.example.tourism.history;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
@Tag(name = "Search History", description = "User search history management")
public class SearchHistoryController {

    private final SearchHistoryService searchHistoryService;

    @GetMapping("/searches")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get my search history", description = "Get all search history for the authenticated user")
    public ResponseEntity<List<SearchHistory>> getMySearches(Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        return ResponseEntity.ok(searchHistoryService.getUserSearches(userId));
    }

    @GetMapping("/searches/recent")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get recent searches", description = "Get recent search history (last 10 searches)")
    public ResponseEntity<List<SearchHistory>> getRecentSearches(
            @RequestParam(defaultValue = "10") int limit,
            Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        return ResponseEntity.ok(searchHistoryService.getRecentSearches(userId, limit));
    }

    @DeleteMapping("/searches")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Clear search history", description = "Clear all search history for the authenticated user")
    public ResponseEntity<Void> clearSearchHistory(Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        searchHistoryService.clearSearchHistory(userId);
        return ResponseEntity.noContent().build();
    }

    private Long getUserIdFromAuth(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        return jwt.getClaim("userId");
    }
}