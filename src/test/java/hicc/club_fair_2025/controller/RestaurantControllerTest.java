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
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.BDDMockito.*;
import static org.mockito.ArgumentMatchers.anyList;
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

		// MockMvc 빌더
		mockMvc = MockMvcBuilders.standaloneSetup(restaurantController)
			// 인코딩 필터 추가
			.addFilters(new CharacterEncodingFilter("UTF-8", true))
			.defaultResponseCharacterEncoding(StandardCharsets.UTF_8)
			.build();
	}

	@DisplayName("POST /restaurants/save - 네이버 API 데이터 저장 (정상)")
	@Test
	void saveNaverData_Success() throws Exception {
		// given
		given(restaurantService.fetchMultipleQueriesFromNaver())
			.willReturn(List.of(Map.of("title", "홍대 한식 제육")));

		willDoNothing().given(restaurantService).saveFromJsonList(anyList());

		// when & then
		mockMvc.perform(post("/restaurants/save"))
			.andExpect(status().isOk())
			.andExpect(content().string("저장 완료")); // 한글 정상 매칭
	}

	@DisplayName("POST /restaurants/save - 네이버 API 데이터 저장 (실패)")
	@Test
	void saveNaverData_Failure() throws Exception {
		// given
		given(restaurantService.fetchMultipleQueriesFromNaver())
			.willThrow(new RuntimeException("API 호출 오류"));

		// when & then
		mockMvc.perform(post("/restaurants/save"))
			.andExpect(status().isOk())
			.andExpect(content().string("저장 실패: API 호출 오류")); // 한글 정상 매칭
	}

	@DisplayName("GET /restaurants/search-queries/{queryId}/restaurant - 검색어 ID로 식당 조회 (정상)")
	@Test
	void getRestaurantByQueryId() throws Exception {
		// given
		var sq = new SearchQuery("홍대 한식 제육", null);
		sq.setId(100L);

		given(searchQueryRepository.findById(anyLong()))
			.willReturn(Optional.of(sq));

		var rest = new Restaurant("홍대 제육맛집", "한식", "서울시 어딘가", 0.0, 0.0);
		given(restaurantService.findBySearchQuery("홍대 한식 제육"))
			.willReturn(rest);

		// when & then
		mockMvc.perform(get("/restaurants/search-queries/100/restaurant"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value("홍대 제육맛집"))
			.andExpect(jsonPath("$.category").value("한식"));
	}

	@DisplayName("GET /restaurants/search-queries/{queryId}/restaurant - 검색어 ID 없음")
	@Test
	void getRestaurantByQueryId_NotFound() throws Exception {
		// given
		given(searchQueryRepository.findById(999L))
			.willReturn(Optional.empty());

		// when & then
		mockMvc.perform(get("/restaurants/search-queries/999/restaurant"))
			.andExpect(status().isBadRequest()); // 또는 is4xxClientError
	}
}