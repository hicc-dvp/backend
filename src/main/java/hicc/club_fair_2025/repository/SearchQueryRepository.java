package hicc.club_fair_2025.repository;

import hicc.club_fair_2025.entity.SearchQuery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * SearchQuery 엔티티를 관리하기 위한 레포지토리
 */
@Repository
public interface SearchQueryRepository extends JpaRepository<SearchQuery, Long> {
}