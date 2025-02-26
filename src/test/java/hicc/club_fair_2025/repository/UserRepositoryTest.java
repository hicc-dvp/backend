package hicc.club_fair_2025.repository;

import hicc.club_fair_2025.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
class UserRepositoryTest {

	@Autowired
	private UserRepository userRepository;

	@DisplayName("사용자 저장 및 조회 테스트")
	@Test
	void testSaveAndFind() {
		User user = new User("insta123", "상수역", "제육");
		userRepository.save(user);
		Optional<User> found = userRepository.findById(user.getId());
		assertThat(found).isPresent();
		assertThat(found.get().getInstagramId()).isEqualTo("insta123");
		assertThat(found.get().getStation()).isEqualTo("상수역");
		assertThat(found.get().getSearchQuery()).isEqualTo("제육");
	}

	@DisplayName("중복 InstagramId 저장 시 예외 발생 테스트")
	@Test
	void testDuplicateInstagramId() {
		User user1 = new User("insta123", "상수역", "제육");
		User user2 = new User("insta123", "홍대입구역", "백반");
		userRepository.save(user1);
		assertThrows(Exception.class, () -> userRepository.saveAndFlush(user2));
	}
}