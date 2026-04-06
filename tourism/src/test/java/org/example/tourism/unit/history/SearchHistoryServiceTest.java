package org.example.tourism.unit.history;

import org.example.tourism.history.SearchHistory;
import org.example.tourism.history.SearchHistoryRepository;
import org.example.tourism.history.SearchHistoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchHistoryServiceTest {

    @Mock
    private SearchHistoryRepository searchHistoryRepository;

    @InjectMocks
    private SearchHistoryService searchHistoryService;

    private Long userId;

    @BeforeEach
    void setUp() {
        userId = 100L;
    }

    @Test
    void saveSearch_ShouldSaveSearch_WhenValidParameters() {
        // Given
        LocalDate checkIn = LocalDate.now().plusDays(10);
        LocalDate checkOut = LocalDate.now().plusDays(12);

        when(searchHistoryRepository.findByUserIdOrderBySearchedAtDesc(userId))
                .thenReturn(new ArrayList<>());

        // When
        searchHistoryService.saveSearch(userId, "New York", checkIn, checkOut, 2, 4, new BigDecimal("500.00"));

        // Then
        ArgumentCaptor<SearchHistory> captor = ArgumentCaptor.forClass(SearchHistory.class);
        verify(searchHistoryRepository, times(1)).save(captor.capture());

        SearchHistory saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(userId);
        assertThat(saved.getCity()).isEqualTo("New York");
        assertThat(saved.getGuests()).isEqualTo(2);
        assertThat(saved.getStarRating()).isEqualTo(4);
        assertThat(saved.getMaxPrice()).isEqualTo(new BigDecimal("500.00"));
    }

    @Test
    void saveSearch_ShouldNotSave_WhenAllParametersNull() {
        // When
        searchHistoryService.saveSearch(userId, null, null, null, null, null, null);

        // Then
        verify(searchHistoryRepository, never()).save(any(SearchHistory.class));
    }

    @Test
    void saveSearch_ShouldDeleteOldest_WhenMoreThan20Searches() {
        // Given
        LocalDate checkIn = LocalDate.now().plusDays(10);
        LocalDate checkOut = LocalDate.now().plusDays(12);

        // Create 21 searches (more than 20)
        List<SearchHistory> twentyOneSearches = createSearchHistoryList(21);

        when(searchHistoryRepository.findByUserIdOrderBySearchedAtDesc(userId))
                .thenReturn(twentyOneSearches);

        // When
        searchHistoryService.saveSearch(userId, "Paris", checkIn, checkOut, 2, 5, new BigDecimal("300.00"));

        // Then
        verify(searchHistoryRepository, times(1)).save(any(SearchHistory.class));
        // Verify delete was called with the oldest search (index 20, which is the last one)
        // Use any(SearchHistory.class) because we can't predict the exact instance
        verify(searchHistoryRepository, times(1)).delete(any(SearchHistory.class));
    }

    @Test
    void saveSearch_ShouldNotDelete_WhenLessThanOrEqual20Searches() {
        // Given
        LocalDate checkIn = LocalDate.now().plusDays(10);
        LocalDate checkOut = LocalDate.now().plusDays(12);

        // Create 20 searches (exactly 20)
        List<SearchHistory> twentySearches = createSearchHistoryList(20);

        when(searchHistoryRepository.findByUserIdOrderBySearchedAtDesc(userId))
                .thenReturn(twentySearches);

        // When
        searchHistoryService.saveSearch(userId, "Paris", checkIn, checkOut, 2, 5, new BigDecimal("300.00"));

        // Then
        verify(searchHistoryRepository, times(1)).save(any(SearchHistory.class));
        // Delete should NOT be called because size is not > 20
        verify(searchHistoryRepository, never()).delete(any(SearchHistory.class));
    }

    @Test
    void getUserSearches_ShouldReturnSearches_WhenUserHasSearches() {
        // Given
        List<SearchHistory> expectedSearches = createSearchHistoryList(5);
        when(searchHistoryRepository.findByUserIdOrderBySearchedAtDesc(userId))
                .thenReturn(expectedSearches);

        // When
        List<SearchHistory> results = searchHistoryService.getUserSearches(userId);

        // Then
        assertThat(results).hasSize(5);
        verify(searchHistoryRepository, times(1)).findByUserIdOrderBySearchedAtDesc(userId);
    }

    @Test
    void getUserSearches_ShouldReturnEmptyList_WhenUserHasNoSearches() {
        // Given
        when(searchHistoryRepository.findByUserIdOrderBySearchedAtDesc(userId))
                .thenReturn(new ArrayList<>());

        // When
        List<SearchHistory> results = searchHistoryService.getUserSearches(userId);

        // Then
        assertThat(results).isEmpty();
        verify(searchHistoryRepository, times(1)).findByUserIdOrderBySearchedAtDesc(userId);
    }

    @Test
    void getRecentSearches_ShouldReturnLimitedSearches() {
        // Given
        int limit = 5;
        List<SearchHistory> expectedSearches = createSearchHistoryList(limit);
        when(searchHistoryRepository.findTopByUserId(userId, limit))
                .thenReturn(expectedSearches);

        // When
        List<SearchHistory> results = searchHistoryService.getRecentSearches(userId, limit);

        // Then
        assertThat(results).hasSize(limit);
        verify(searchHistoryRepository, times(1)).findTopByUserId(userId, limit);
    }

    @Test
    void clearSearchHistory_ShouldDeleteAllSearches_ForUser() {
        // When
        searchHistoryService.clearSearchHistory(userId);

        // Then
        verify(searchHistoryRepository, times(1)).deleteByUserId(userId);
    }

    @Test
    void saveSearch_ShouldHandleNullCity() {
        // Given
        LocalDate checkIn = LocalDate.now().plusDays(10);
        LocalDate checkOut = LocalDate.now().plusDays(12);

        when(searchHistoryRepository.findByUserIdOrderBySearchedAtDesc(userId))
                .thenReturn(new ArrayList<>());

        // When
        searchHistoryService.saveSearch(userId, null, checkIn, checkOut, 2, null, null);

        // Then
        ArgumentCaptor<SearchHistory> captor = ArgumentCaptor.forClass(SearchHistory.class);
        verify(searchHistoryRepository, times(1)).save(captor.capture());

        SearchHistory saved = captor.getValue();
        assertThat(saved.getCity()).isNull();
        assertThat(saved.getGuests()).isEqualTo(2);
        assertThat(saved.getStarRating()).isNull();
    }

    @Test
    void saveSearch_ShouldHandleNullDates() {
        // Given
        when(searchHistoryRepository.findByUserIdOrderBySearchedAtDesc(userId))
                .thenReturn(new ArrayList<>());

        // When
        searchHistoryService.saveSearch(userId, "Paris", null, null, null, 5, new BigDecimal("300.00"));

        // Then
        ArgumentCaptor<SearchHistory> captor = ArgumentCaptor.forClass(SearchHistory.class);
        verify(searchHistoryRepository, times(1)).save(captor.capture());

        SearchHistory saved = captor.getValue();
        assertThat(saved.getCity()).isEqualTo("Paris");
        assertThat(saved.getCheckIn()).isNull();
        assertThat(saved.getCheckOut()).isNull();
        assertThat(saved.getStarRating()).isEqualTo(5);
    }

    private List<SearchHistory> createSearchHistoryList(int count) {
        List<SearchHistory> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            SearchHistory sh = new SearchHistory();
            sh.setId((long) i);
            sh.setUserId(userId);
            sh.setCity("City " + i);
            list.add(sh);
        }
        return list;
    }
}