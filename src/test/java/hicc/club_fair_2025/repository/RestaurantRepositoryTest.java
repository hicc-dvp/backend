package hicc.club_fair_2025.repository;

import hicc.club_fair_2025.entity.Restaurant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
class RestaurantRepositoryTest {

	@Autowired
	private RestaurantRepository restaurantRepository;

	@DisplayName("searchQuery로 식당 조회 - findBySearchQuery()")
	@Test
	void findBySearchQuery() {
		// given
		Restaurant rest = new Restaurant("홍대 제육맛집", "한식", "서울 어딘가");
		rest.setSearchQuery("홍대 한식 제육");
		rest.setMapx(1269144811);
		rest.setMapy(375526833);
		rest.setStation("홍대입구역");
		restaurantRepository.save(rest);

		// when
		List<Restaurant> found = restaurantRepository.findBySearchQuery("홍대 한식 제육");

		// then
		assertThat(found).isNotEmpty();
		assertThat(found.get(0).getName()).isEqualTo("홍대 제육맛집");
	}

	@DisplayName("searchQuery unique 제약 - 중복 시 예외 발생")
	@Test
	void uniqueConstraint() {
		// given
		Restaurant rest1 = new Restaurant("홍대 제육맛집1", "한식", "서울 어딘가");
		rest1.setSearchQuery("홍대 한식 제육");
		rest1.setMapx(1269144811);
		rest1.setMapy(375526833);
		rest1.setStation("홍대입구역");
		restaurantRepository.save(rest1);

		Restaurant rest2 = new Restaurant("홍대 제육맛집2", "한식", "서울 어딘가2");
		rest2.setSearchQuery("홍대 한식 제육"); // 중복되는 검색어
		rest2.setMapx(1269144812);
		rest2.setMapy(375526834);
		rest2.setStation("홍대입구역");

		// when & then
		assertThrows(DataIntegrityViolationException.class, () -> {
			restaurantRepository.save(rest2);
			restaurantRepository.flush();
		});
	}

	@DisplayName("식당 저장 후 조회")
	@Test
	void saveAndFind() {
		// given
		Restaurant rest = new Restaurant("홍대 제육맛집", "한식", "서울 어딘가");
		rest.setSearchQuery("홍대 한식 제육");
		rest.setMapx(1269144811);
		rest.setMapy(375526833);
		rest.setStation("홍대입구역");
		Restaurant saved = restaurantRepository.save(rest);

		// when
		Optional<Restaurant> foundOpt = restaurantRepository.findById(saved.getId());
		Restaurant found = foundOpt.orElse(null);

		// then
		assertThat(found).isNotNull();
		assertThat(found.getName()).isEqualTo("홍대 제육맛집");
		assertThat(found.getSearchQuery()).isEqualTo("홍대 한식 제육");
		assertThat(found.getStation()).isEqualTo("홍대입구역");
	}
}