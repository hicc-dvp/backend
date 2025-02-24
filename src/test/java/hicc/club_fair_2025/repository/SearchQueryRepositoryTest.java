package hicc.club_fair_2025.repository;

import hicc.club_fair_2025.entity.SearchQuery;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
class SearchQueryRepositoryTest {

	@Autowired
	private SearchQueryRepository searchQueryRepository;

	@DisplayName("검색어 저장 후 조회")
	@Test
	void saveAndFind() {
		// given
		SearchQuery sq = new SearchQuery("홍대 한식 제육", null);
		SearchQuery saved = searchQueryRepository.save(sq);

		// when
		SearchQuery found = searchQueryRepository.findById(saved.getId()).orElse(null);

		// then
		assertThat(found).isNotNull();
		assertThat(found.getQuery()).isEqualTo("홍대 한식 제육");
	}

	@DisplayName("검색어 query unique 제약 - 중복 시 예외")
	@Test
	void uniqueConstraint() {
		// given
		SearchQuery sq1 = new SearchQuery("홍대 한식 제육", null);
		searchQueryRepository.save(sq1);

		SearchQuery sq2 = new SearchQuery("홍대 한식 제육", null); // 중복

		// when & then
		assertThrows(DataIntegrityViolationException.class, () -> {
			searchQueryRepository.save(sq2);
			searchQueryRepository.flush();
		});
	}

	@DisplayName("존재하지 않는 검색어 ID 조회")
	@Test
	void findById_NotFound() {
		// given
		// when
		Optional<SearchQuery> found = searchQueryRepository.findById(999L);

		// then
		assertThat(found).isEmpty();
	}
}