package org.example.tourism.history;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {
    List<SearchHistory> findByUserIdOrderBySearchedAtDesc(Long userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM SearchHistory s WHERE s.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    @Query(value = "SELECT * FROM search_history WHERE user_id = :userId ORDER BY searched_at DESC LIMIT :limit",
            nativeQuery = true)
    List<SearchHistory> findTopByUserId(@Param("userId") Long userId, @Param("limit") int limit);
}