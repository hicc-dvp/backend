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
		restaurantService = Mockito.mock(RestaurantService.class);
		searchQueryRepository = Mockito.mock(SearchQueryRepository.class);
		restaurantController = new RestaurantController(restaurantService, searchQueryRepository);
		mockMvc = MockMvcBuilders.standaloneSetup(restaurantController)
			.addFilters(new CharacterEncodingFilter("UTF-8", true))
			.defaultResponseCharacterEncoding(StandardCharsets.UTF_8)
			.build();
	}

	@DisplayName("POST /restaurants/saveOne - 네이버 API 데이터 저장 (정상)")
	@Test
	void saveOneRestaurantPerSearchQuery_Success() throws Exception {
		doNothing().when(restaurantService).saveOneRestaurantPerSearchQuery();
		mockMvc.perform(post("/restaurants/saveOne"))
			.andExpect(status().isOk())
			.andExpect(content().string("저장 완료"));
	}

	@DisplayName("POST /restaurants/saveOne - 네이버 API 데이터 저장 (실패)")
	@Test
	void saveOneRestaurantPerSearchQuery_Failure() throws Exception {
		doThrow(new RuntimeException("API 호출 오류")).when(restaurantService).saveOneRestaurantPerSearchQuery();
		mockMvc.perform(post("/restaurants/saveOne"))
			.andExpect(status().isOk())
			.andExpect(content().string("저장 실패: API 호출 오류"));
	}

	@DisplayName("GET /restaurants/search-queries/{queryId}/restaurant - 검색어 ID로 식당 조회 (정상)")
	@Test
	void getRestaurantByQueryId_Success() throws Exception {
		SearchQuery sq = new SearchQuery("홍대 한식 제육", null);
		sq.setId(100L);
		given(searchQueryRepository.findById(anyLong()))
			.willReturn(Optional.of(sq));
		Restaurant rest = new Restaurant("홍대 제육맛집", "한식", "서울시 어딘가");
		rest.setSearchQuery("홍대 한식 제육");
		List<Restaurant> restList = List.of(rest);
		given(restaurantService.findBySearchQuery("홍대 한식 제육"))
			.willReturn(restList);

		mockMvc.perform(get("/restaurants/search-queries/100/restaurant"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].name").value("홍대 제육맛집"))
			.andExpect(jsonPath("$[0].category").value("한식"));
	}

	@DisplayName("GET /restaurants/search-queries/{queryId}/restaurant - 검색어 ID 없음")
	@Test
	void getRestaurantByQueryId_NotFound() throws Exception {
		given(searchQueryRepository.findById(999L))
			.willReturn(Optional.empty());
		mockMvc.perform(get("/restaurants/search-queries/999/restaurant"))
			.andExpect(status().isBadRequest());
	}
}