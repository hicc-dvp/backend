package hicc.club_fair_2025.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import hicc.club_fair_2025.entity.User;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
	List<User> findBySearchQueryAndStation(String searchQuery, String station);
}