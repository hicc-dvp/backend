package hicc.club_fair_2025.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import hicc.club_fair_2025.entity.Station;

@Repository
public interface StationRepository extends JpaRepository<Station, Long> {
	Optional<Station> findByName(String name);
}
