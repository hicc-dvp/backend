package hicc.club_fair_2025.repository;

import hicc.club_fair_2025.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Restaurant 엔티티를 관리하기 위한 Spring Data JPA 레포지토리
 */
@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    /**
     * 주어진 searchQuery 값으로 Restaurant를 조회합니다.
     */
    Optional<Restaurant> findBySearchQuery(String searchQuery);
}