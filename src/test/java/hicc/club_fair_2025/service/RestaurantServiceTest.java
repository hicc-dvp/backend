package hicc.club_fair_2025.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hicc.club_fair_2025.entity.Restaurant;
import hicc.club_fair_2025.entity.SearchQuery;
import hicc.club_fair_2025.entity.Station;
import hicc.club_fair_2025.repository.RestaurantRepository;
import hicc.club_fair_2025.repository.SearchQueryRepository;
import hicc.club_fair_2025.repository.StationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceTest {

	@Mock
	private RestaurantRepository restaurantRepository;
	@Mock
	private SearchQueryRepository searchQueryRepository;
	@Mock
	private StationRepository stationRepository;

	@InjectMocks
	private RestaurantService restaurantService;

	private ObjectMapper objectMapper = new ObjectMapper();

	@Spy
	private RestTemplate restTemplate = new RestTemplate();

	private SearchQuery sampleSearchQuery;

	@BeforeEach
	void setUp() {
		sampleSearchQuery = new SearchQuery("국밥", null);
		sampleSearchQuery.setId(1L);
	}

	@DisplayName("findBySearchQuery() - 식당 조회 (정상 케이스)")
	@Test
	void findBySearchQuery_Success() {
		Restaurant mockRestaurant = new Restaurant("홍대 제육맛집", "한식", "서울 어딘가");
		mockRestaurant.setSearchQuery("홍대 한식 제육");
		List<Restaurant> list = List.of(mockRestaurant);
		given(restaurantRepository.findBySearchQuery("홍대 한식 제육")).willReturn(list);

		List<Restaurant> result = restaurantService.findBySearchQuery("홍대 한식 제육");

		assertThat(result).isNotEmpty();
		assertThat(result.get(0).getName()).isEqualTo("홍대 제육맛집");
	}

	@DisplayName("findBySearchQuery() - 없는 검색어일 경우 빈 리스트 반환")
	@Test
	void findBySearchQuery_NotFound() {
		given(restaurantRepository.findBySearchQuery("없는 쿼리")).willReturn(Collections.emptyList());

		List<Restaurant> result = restaurantService.findBySearchQuery("없는 쿼리");

		assertThat(result).isEmpty();
	}

	@DisplayName("saveOneRestaurantPerSearchQuery() - 정상 케이스 (거리 비교 후 '상수역' 저장)")
	@Test
	void saveOneRestaurantPerSearchQuery_Success() throws Exception {
		List<SearchQuery> sqList = List.of(sampleSearchQuery);
		given(searchQueryRepository.findAll()).willReturn(sqList);

		Map<String, Object> fakeResponse = new HashMap<>();
		fakeResponse.put("total", 1);
		List<Map<String, String>> items = new ArrayList<>();
		items.add(Map.of(
			"title", "<b>홍대 제육맛집</b>",
			"category", "한식",
			"roadAddress", "서울 어딘가",
			"mapx", "1269144811",
			"mapy", "375526833"
		));
		fakeResponse.put("items", items);
		String fakeJson = objectMapper.writeValueAsString(fakeResponse);
		ResponseEntity<String> fakeResponseEntity = new ResponseEntity<>(fakeJson, HttpStatus.OK);

		Station station1 = new Station("홍대입구역", 1269141400, 375526800, "홍대입구역");
		Station station2 = new Station("상수역", 1269141500, 375526900, "상수역");
		given(stationRepository.findByName("홍대입구역")).willReturn(Optional.of(station1));
		given(stationRepository.findByName("상수역")).willReturn(Optional.of(station2));

		try (MockedConstruction<RestTemplate> mocked = Mockito.mockConstruction(RestTemplate.class,
			(mock, context) -> {
				when(mock.exchange(any(RequestEntity.class), eq(String.class)))
					.thenReturn(fakeResponseEntity);
			})) {
			restaurantService.saveOneRestaurantPerSearchQuery();
		}

		then(restaurantRepository).should(atLeastOnce()).saveAll(anyList());
	}
}