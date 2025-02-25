package hicc.club_fair_2025.service;

import hicc.club_fair_2025.entity.Category;
import hicc.club_fair_2025.entity.SearchQuery;
import hicc.club_fair_2025.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 카테고리(Category)와 관련된 비즈니스 로직을 담당하는 서비스
 */
@Service
public class CategoryService {

	private final CategoryRepository categoryRepository;

	public CategoryService(CategoryRepository categoryRepository) {
		this.categoryRepository = categoryRepository;
	}

	/**
	 * DB에 저장된 모든 카테고리 목록을 반환
	 */
	public List<Category> findAllCategories() {
		return categoryRepository.findAll();
	}

	/**
	 * 특정 카테고리 ID에 해당하는 모든 서치 쿼리(검색어) 목록을 반환
	 *
	 * @param categoryId 카테고리 PK
	 * @return 검색어 목록 (없으면 빈 리스트)
	 */
	public List<SearchQuery> findSearchQueriesByCategoryId(Long categoryId) {
		Optional<Category> categoryOpt = categoryRepository.findById(categoryId);
		if (categoryOpt.isPresent()) {
			// Category 엔티티에 연결된 searchQueries 리스트 반환
			Category category = categoryOpt.get();
			return category.getSearchQueries();
		}
		// 해당 카테고리가 없으면 빈 리스트 반환
		return List.of();
	}
}