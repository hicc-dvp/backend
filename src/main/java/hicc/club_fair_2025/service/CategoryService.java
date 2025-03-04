package hicc.club_fair_2025.service;

import hicc.club_fair_2025.entity.Category;
import hicc.club_fair_2025.entity.SearchQuery;
import hicc.club_fair_2025.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

	private final CategoryRepository categoryRepository;

	public CategoryService(CategoryRepository categoryRepository) {
		this.categoryRepository = categoryRepository;
	}

	public List<Category> findAllCategories() {
		return categoryRepository.findAll();
	}

	public List<SearchQuery> findSearchQueriesByCategoryId(Long categoryId) {
		Optional<Category> categoryOpt = categoryRepository.findById(categoryId);
		return categoryOpt.map(Category::getSearchQueries)
			.orElse(List.of());
	}
}