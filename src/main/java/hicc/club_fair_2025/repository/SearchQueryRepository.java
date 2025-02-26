package hicc.club_fair_2025.repository;

import hicc.club_fair_2025.entity.SearchQuery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SearchQueryRepository extends JpaRepository<SearchQuery, Long> {
}