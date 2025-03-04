package hicc.club_fair_2025.repository;

import hicc.club_fair_2025.entity.Category;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
class CategoryRepositoryTest {

	@Autowired
	private CategoryRepository categoryRepository;

	@DisplayName("카테고리 저장 후 조회")
	@Test
	void saveAndFind() {
		Category cat = new Category("한식");
		Category saved = categoryRepository.save(cat);
		Category found = categoryRepository.findById(saved.getId()).orElse(null);
		assertThat(found).isNotNull();
		assertThat(found.getName()).isEqualTo("한식");
	}

	@DisplayName("카테고리 이름 unique 제약 - 중복 시 예외 발생")
	@Test
	void uniqueConstraint() {
		Category cat1 = new Category("한식");
		categoryRepository.save(cat1);
		Category cat2 = new Category("한식");
		assertThrows(DataIntegrityViolationException.class, () -> {
			categoryRepository.save(cat2);
			categoryRepository.flush();
		});
	}

	@DisplayName("전체 카테고리 목록 조회")
	@Test
	void findAllCategories() {
		categoryRepository.save(new Category("한식"));
		categoryRepository.save(new Category("일식"));
		List<Category> list = categoryRepository.findAll();
		assertThat(list).hasSize(2);
	}
}