package hicc.club_fair_2025.repository;

import hicc.club_fair_2025.entity.Restaurant;

import java.util.List;

public interface DiningRepository {
    Restaurant save(Restaurant dining);

    void saveAll(List<Restaurant> diningList);

    List<String> findAllTitles();
    // 기능 필요한 거 추가


}
