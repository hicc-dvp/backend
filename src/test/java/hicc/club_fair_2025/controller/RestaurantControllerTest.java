package hicc.club_fair_2025.controller;

import hicc.club_fair_2025.entity.Restaurant;
import hicc.club_fair_2025.entity.SearchQuery;
import hicc.club_fair_2025.repository.SearchQueryRepository;
import hicc.club_fair_2025.service.RestaurantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.mockito.BDDMockito.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class RestaurantControllerTest {

	private MockMvc mockMvc;
	private RestaurantService restaurantService;
	private SearchQueryRepository searchQueryRepository;
	private RestaurantController restaurantController;

	@BeforeEach
	void setUp() {
		// Mock 객체 생성
		restaurantService = Mockito.mock(RestaurantService.class);
		searchQueryRepository = Mockito.mock(SearchQueryRepository.class);

		// Controller 생성
		restaurantController = new RestaurantController(restaurantService, searchQueryRepository);

		// MockMvc 빌더: UTF-8 인코딩 필터 추가
		mockMvc = MockMvcBuilders.standaloneSetup(restaurantController)
			.addFilters(new CharacterEncodingFilter("UTF-8", true))
			.defaultResponseCharacterEncoding(StandardCharsets.UTF_8)
			.build();
	}

	@DisplayName("POST /restaurants/saveOne - 네이버 API 데이터 저장 (정상)")
	@Test
	void saveOneRestaurantPerSearchQuery_Success() throws Exception {
		// given: 서비스 메서드 호출 시 예외 발생 없이 정상 수행
		// 여기서는 아무런 리턴값이 없으므로 doNothing 사용
		doNothing().when(restaurantService).saveOneRestaurantPerSearchQuery();

		// when & then: POST 요청 후 "저장 완료" 문자열 반환 확인
		mockMvc.perform(post("/restaurants/saveOne"))
			.andExpect(status().isOk())
			.andExpect(content().string("저장 완료"));
	}

	@DisplayName("POST /restaurants/saveOne - 네이버 API 데이터 저장 (실패)")
	@Test
	void saveOneRestaurantPerSearchQuery_Failure() throws Exception {
		// given: 서비스 메서드 호출 시 예외 발생
		doThrow(new RuntimeException("API 호출 오류")).when(restaurantService).saveOneRestaurantPerSearchQuery();

		// when & then: POST 요청 후 오류 메시지 반환 확인
		mockMvc.perform(post("/restaurants/saveOne"))
			.andExpect(status().isOk())
			.andExpect(content().string("저장 실패: API 호출 오류"));
	}

	@DisplayName("GET /restaurants/search-queries/{queryId}/restaurant - 검색어 ID로 식당 조회 (정상)")
	@Test
	void getRestaurantByQueryId_Success() throws Exception {
		// given: SearchQuery와 매핑된 Restaurant 정보 설정
		SearchQuery sq = new SearchQuery("홍대 한식 제육", null);
		sq.setId(100L);

		given(searchQueryRepository.findById(anyLong()))
			.willReturn(Optional.of(sq));

		Restaurant rest = new Restaurant("홍대 제육맛집", "한식", "서울시 어딘가");
		// Restaurant 엔티티의 searchQuery 필드는 "홍대 한식 제육"로 저장되어 있어야 함
		rest.setSearchQuery("홍대 한식 제육");
		given(restaurantService.findBySearchQuery("홍대 한식 제육"))
			.willReturn(rest);

		// when & then: GET 요청 후 JSON 응답 필드 값 검증
		mockMvc.perform(get("/restaurants/search-queries/100/restaurant"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value("홍대 제육맛집"))
			.andExpect(jsonPath("$.category").value("한식"));
	}

	@DisplayName("GET /restaurants/search-queries/{queryId}/restaurant - 검색어 ID 없음")
	@Test
	void getRestaurantByQueryId_NotFound() throws Exception {
		// given: 존재하지 않는 SearchQuery ID로 조회 시 Optional.empty 반환
		given(searchQueryRepository.findById(999L))
			.willReturn(Optional.empty());

		// when & then: GET 요청 시 400 Bad Request 상태 코드 반환 확인
		mockMvc.perform(get("/restaurants/search-queries/999/restaurant"))
			.andExpect(status().isBadRequest());
	}
}