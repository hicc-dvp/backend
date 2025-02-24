package hicc.club_fair_2025.controller;

import hicc.club_fair_2025.entity.Category;
import hicc.club_fair_2025.entity.SearchQuery;
import hicc.club_fair_2025.service.CategoryService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CategoryControllerTest {

	private MockMvc mockMvc;
	private CategoryService categoryService; // 직접 Mock
	private CategoryController categoryController;

	@BeforeEach
	void setup() {
		// 1) 수동으로 Mock 생성
		categoryService = Mockito.mock(CategoryService.class);

		// 2) Controller 생성자 주입
		categoryController = new CategoryController(categoryService);

		// 3) standaloneSetup
		mockMvc = MockMvcBuilders.standaloneSetup(categoryController)
			.build();
	}

	@Test
	@DisplayName("GET /categories - 전체 카테고리 목록 조회")
	void getAllCategories() throws Exception {
		// given
		var cat1 = new Category("한식");
		cat1.setId(1L);
		var cat2 = new Category("일식");
		cat2.setId(2L);

		given(categoryService.findAllCategories())
			.willReturn(List.of(cat1, cat2));

		// when & then
		mockMvc.perform(get("/categories"))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].id").value(1L))
			.andExpect(jsonPath("$[0].name").value("한식"))
			.andExpect(jsonPath("$[1].id").value(2L))
			.andExpect(jsonPath("$[1].name").value("일식"));
	}

	@Test
	@DisplayName("GET /categories/{id}/search-queries - 특정 카테고리 검색어 목록 (정상)")
	void getSearchQueriesByCategoryId() throws Exception {
		// given
		SearchQuery sq1 = new SearchQuery("홍대 한식 제육", null);
		sq1.setId(10L);
		SearchQuery sq2 = new SearchQuery("홍대 한식 국밥", null);
		sq2.setId(11L);

		// Stub: service.findSearchQueriesByCategoryId(1L) -> [sq1, sq2]
		given(categoryService.findSearchQueriesByCategoryId(1L))
			.willReturn(List.of(sq1, sq2));

		// when & then
		mockMvc.perform(get("/categories/1/search-queries"))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].id").value(10L))
			.andExpect(jsonPath("$[0].query").value("홍대 한식 제육"))
			.andExpect(jsonPath("$[1].id").value(11L))
			.andExpect(jsonPath("$[1].query").value("홍대 한식 국밥"));
	}

	@Test
	@DisplayName("GET /categories/{id}/search-queries - 없는 카테고리 (빈 배열 반환)")
	void getSearchQueriesByCategoryId_NotFound() throws Exception {
		// given
		given(categoryService.findSearchQueriesByCategoryId(999L))
			.willReturn(List.of());

		// when & then
		mockMvc.perform(get("/categories/999/search-queries"))
			.andExpect(status().isOk())
			.andExpect(content().string("[]"));
	}
}