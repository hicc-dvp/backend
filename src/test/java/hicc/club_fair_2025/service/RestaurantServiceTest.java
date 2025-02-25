package hicc.club_fair_2025.service;

import hicc.club_fair_2025.entity.Restaurant;
import hicc.club_fair_2025.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceTest {

	@Mock
	private RestaurantRepository restaurantRepository;

	@InjectMocks
	private RestaurantService restaurantService;

	@BeforeEach
	void setup() {
	}

	@DisplayName("findBySearchQuery() - 식당 조회 (정상 케이스)")
	@Test
	void findBySearchQuery() {
		// given
		Restaurant mockRest = new Restaurant("홍대 제육맛집", "한식", "서울 어딘가", "https://map.naver.com/v/123456");
		Mockito.when(restaurantRepository.findBySearchQuery("홍대 한식 제육"))
			.thenReturn(Optional.of(mockRest));

		// when
		Restaurant result = restaurantService.findBySearchQuery("홍대 한식 제육");

		// then
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo("홍대 제육맛집");
	}

	@DisplayName("findBySearchQuery() - 없는 searchQuery일 경우 null 반환")
	@Test
	void findBySearchQuery_NotFound() {
		// given
		Mockito.when(restaurantRepository.findBySearchQuery("없는 쿼리"))
			.thenReturn(Optional.empty());

		// when
		Restaurant result = restaurantService.findBySearchQuery("없는 쿼리");

		// then
		assertThat(result).isNull();
	}

	@DisplayName("saveFromJsonList() - API 응답을 Restaurant로 변환 후 저장")
	@Test
	void saveFromJsonList() {
		// given
		List<Map<String, String>> jsonData = List.of(
			Map.of("title", "<b>홍대 제육</b>", "category", "한식", "roadAddress", "서울 어딘가",
				"mapx", "37.1234", "mapy", "126.5678")
		);

		// when
		restaurantService.saveFromJsonList(jsonData);

		// then
		// saveAll 호출 여부 검증
		Mockito.verify(restaurantRepository, Mockito.times(1))
			.saveAll(ArgumentMatchers.anyList());
	}

	@DisplayName("saveFromJsonList() - mapx/mapy 파싱 오류 시 0.0으로 처리")
	@Test
	void saveFromJsonList_mapParsingError() {
		// given
		List<Map<String, String>> jsonData = List.of(
			Map.of("title", "홍대 제육", "category", "한식", "roadAddress", "서울 어딘가",
				"mapx", "abc", "mapy", "xyz")  // 파싱 오류
		);

		// when
		restaurantService.saveFromJsonList(jsonData);

		// then
		// 파싱 오류 발생 -> 0.0으로 대체
		Mockito.verify(restaurantRepository, Mockito.times(1))
			.saveAll(ArgumentMatchers.anyList());
	}

	@DisplayName("saveFromJsonList() - HTML 태그 제거 로직 확인")
	@Test
	void saveFromJsonList_htmlTagRemoval() {
		// given
		List<Map<String, String>> jsonData = List.of(
			Map.of("title", "<b>홍대</b> <i>제육</i>", "category", "한식", "roadAddress", "서울",
				"mapx", "37.1234", "mapy", "126.5678")
		);

		// when
		restaurantService.saveFromJsonList(jsonData);

		// then
		// 실제로 저장된 Restaurant 엔티티에서 <b>, <i>가 제거되었는지 확인하고 싶다면,
		// restaurantRepository.saveAll(...)의 인자로 들어간 Restaurant를 캡처하거나 verify(...)
		// 여기서는 간단히 호출 여부만 체크
		Mockito.verify(restaurantRepository).saveAll(
			Mockito.argThat(it -> {
				// it은 Iterable<Restaurant> 타입
				if (!(it instanceof List)) {
					return false; // List가 아니면 false
				}
				List<Restaurant> list = (List<Restaurant>) it;
				if (list.isEmpty()) {
					return false;
				}
				// 여기서부터 list.get(0) 사용 가능
				Restaurant r = list.get(0);
				return "홍대 제육".equals(r.getName());
			})
		);
	}
}