package hicc.club_fair_2025.repository;

import hicc.club_fair_2025.entity.Station;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
class StationRepositoryTest {

	@Autowired
	private StationRepository stationRepository;

	@DisplayName("findByName()로 역 조회")
	@Test
	void findByName() {
		// given
		Station station = new Station("홍대입구역", 1269211407, 375534442, "홍대입구역");
		stationRepository.save(station);

		// when
		Optional<Station> foundOpt = stationRepository.findByName("홍대입구역");

		// then
		assertThat(foundOpt).isPresent();
		Station found = foundOpt.get();
		assertThat(found.getName()).isEqualTo("홍대입구역");
		assertThat(found.getMapx()).isEqualTo(1269211407);
		assertThat(found.getMapy()).isEqualTo(375534442);
	}

	@DisplayName("findByName() 중복 저장 시 Unique 제약 확인")
	@Test
	void uniqueConstraintOnStationName() {
		// 필요시, Station 엔티티에 Unique 제약이 있다면 테스트 작성
		// (현재 Station 엔티티에 unique 제약이 없다면 생략 가능)
		Station station1 = new Station("상수역", 1270000000, 376000000, "상수역");
		stationRepository.save(station1);

		// 만약 고유 제약이 있다면, 중복 저장 시 예외가 발생해야 함.
		// 아래는 예시이며, 실제 엔티티에 unique 제약을 추가한 경우에만 동작합니다.
		Station station2 = new Station("상수역", 1270000001, 376000001, "상수역");
		assertThrows(DataIntegrityViolationException.class, () -> {
			stationRepository.save(station2);
			stationRepository.flush();
		});
	}
}