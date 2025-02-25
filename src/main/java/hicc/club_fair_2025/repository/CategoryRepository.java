package hicc.club_fair_2025.repository;

import hicc.club_fair_2025.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Category 엔티티를 관리하기 위한 레포지토리
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
}