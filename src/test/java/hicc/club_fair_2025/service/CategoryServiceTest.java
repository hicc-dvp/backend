package hicc.club_fair_2025.service;

import hicc.club_fair_2025.entity.Category;
import hicc.club_fair_2025.entity.SearchQuery;
import hicc.club_fair_2025.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

	@Mock
	private CategoryRepository categoryRepository;

	@InjectMocks
	private CategoryService categoryService;

	private Category category1;
	private Category category2;

	@BeforeEach
	void setup() {
		category1 = new Category("한식");
		category1.setId(1L);

		category2 = new Category("일식");
		category2.setId(2L);
	}

	@DisplayName("findAllCategories() - 전체 카테고리 조회")
	@Test
	void findAllCategories() {
		// given
		BDDMockito.given(categoryRepository.findAll())
			.willReturn(List.of(category1, category2));

		// when
		List<Category> result = categoryService.findAllCategories();

		// then
		assertThat(result).hasSize(2);
		assertThat(result.get(0).getName()).isEqualTo("한식");
		assertThat(result.get(1).getName()).isEqualTo("일식");
	}

	@DisplayName("findSearchQueriesByCategoryId() - 특정 카테고리 검색어 목록")
	@Test
	void findSearchQueriesByCategoryId() {
		// given
		SearchQuery sq1 = new SearchQuery("홍대 한식 제육", category1);
		sq1.setId(10L);
		SearchQuery sq2 = new SearchQuery("홍대 한식 국밥", category1);
		sq2.setId(11L);

		category1.getSearchQueries().addAll(List.of(sq1, sq2));

		BDDMockito.given(categoryRepository.findById(1L))
			.willReturn(Optional.of(category1));

		// when
		List<SearchQuery> queries = categoryService.findSearchQueriesByCategoryId(1L);

		// then
		assertThat(queries).hasSize(2);
		assertThat(queries.get(0).getQuery()).isEqualTo("홍대 한식 제육");
		assertThat(queries.get(1).getQuery()).isEqualTo("홍대 한식 국밥");
	}

	@DisplayName("findSearchQueriesByCategoryId() - 없는 카테고리 ID일 경우 빈 목록 반환")
	@Test
	void findSearchQueriesByCategoryId_NotFound() {
		// given
		BDDMockito.given(categoryRepository.findById(999L))
			.willReturn(Optional.empty());

		// when
		List<SearchQuery> queries = categoryService.findSearchQueriesByCategoryId(999L);

		// then
		assertThat(queries).isEmpty();
	}
}