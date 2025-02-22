package hicc.club_fair_2025.repository;

import hicc.club_fair_2025.domain.Dining;

import java.util.List;

public interface DiningRepository {
    Dining save(Dining dining);

    void saveAll(List<Dining> diningList);

    List<String> findAllTitles();
    // 기능 필요한 거 추가


}
