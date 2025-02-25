package hicc.club_fair_2025.controller;

import hicc.club_fair_2025.dto.CategoryDto;
import hicc.club_fair_2025.entity.Category;
import hicc.club_fair_2025.entity.SearchQuery;
import hicc.club_fair_2025.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 카테고리 및 검색어 관련 API 컨트롤러
 */
@Tag(name = "Category", description = "카테고리 및 검색어 API")
@RestController
@RequestMapping("/categories")
public class CategoryController {

	private final CategoryService categoryService;

	public CategoryController(CategoryService categoryService) {
		this.categoryService = categoryService;
	}

	/**
	 * 전체 카테고리 목록 조회
	 */
	@Operation(summary = "전체 카테고리 조회", description = "DB에 저장된 모든 카테고리를 반환합니다. (id, name만)")
	@GetMapping
	public List<CategoryDto> getAllCategories() {
		// 1) 엔티티 목록 조회
		List<Category> categories = categoryService.findAllCategories();

		// 2) Category → CategoryDto 변환
		return categories.stream()
			.map(cat -> new CategoryDto(cat.getId(), cat.getName()))
			.toList();
	}

	/**
	 * 특정 카테고리에 속한 검색어 목록 조회
	 * @param categoryId 카테고리 식별자(PK)
	 */
	@Operation(summary = "특정 카테고리의 검색어 목록 조회", description = "카테고리 ID로 해당 카테고리에 속한 검색어들을 반환합니다.")
	@GetMapping("/{categoryId}/search-queries")
	public List<SearchQuery> getSearchQueriesByCategoryId(@PathVariable Long categoryId) {
		return categoryService.findSearchQueriesByCategoryId(categoryId);
	}
}