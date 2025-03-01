package hicc.club_fair_2025.repository;

import hicc.club_fair_2025.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    List<Restaurant> findBySearchQueryAndStation(String searchQuery, String station);
}