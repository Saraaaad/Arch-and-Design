package org.example.tourism.history;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchHistoryService {

    private final SearchHistoryRepository searchHistoryRepository;

    @Transactional
    public void saveSearch(Long userId, String city, LocalDate checkIn,
                           LocalDate checkOut, Integer guests, Integer starRating,
                           BigDecimal maxPrice) {
        if (city == null && checkIn == null && checkOut == null &&
                guests == null && starRating == null && maxPrice == null) {
            return;
        }

        log.info("Saving search for user {}: city={}, dates={} to {}, guests={}, starRating={}, maxPrice={}",
                userId, city, checkIn, checkOut, guests, starRating, maxPrice);

        SearchHistory search = new SearchHistory();
        search.setUserId(userId);
        search.setCity(city);
        search.setCheckIn(checkIn);
        search.setCheckOut(checkOut);
        search.setGuests(guests);
        search.setStarRating(starRating);
        search.setMaxPrice(maxPrice);

        searchHistoryRepository.save(search);

        List<SearchHistory> userSearches = searchHistoryRepository.findByUserIdOrderBySearchedAtDesc(userId);
        if (userSearches.size() > 20) {
            searchHistoryRepository.delete(userSearches.get(userSearches.size() - 1));
        }
    }

    @Transactional(readOnly = true)
    public List<SearchHistory> getUserSearches(Long userId) {
        log.info("Fetching search history for user {}", userId);
        return searchHistoryRepository.findByUserIdOrderBySearchedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public List<SearchHistory> getRecentSearches(Long userId, int limit) {
        log.info("Fetching last {} searches for user {}", limit, userId);
        return searchHistoryRepository.findTopByUserId(userId, limit);
    }

    @Transactional
    public void clearSearchHistory(Long userId) {
        log.info("Clearing search history for user {}", userId);
        searchHistoryRepository.deleteByUserId(userId);
    }
}