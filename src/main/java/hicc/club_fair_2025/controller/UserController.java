package hicc.club_fair_2025.controller;

import hicc.club_fair_2025.entity.User;
import hicc.club_fair_2025.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

/**
 * UserController - 사용자 관련 API 엔드포인트를 제공합니다.
 */
@Tag(name = "User", description = "사용자 관련 API")
@RestController
@RequestMapping("/users")
public class UserController {

	private final UserRepository userRepository;

	public UserController(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Operation(summary = "사용자 등록", description = "새로운 사용자를 등록합니다.")
	@PostMapping
	public User createUser(@RequestBody User user) {
		return userRepository.save(user);
	}

	@Operation(
		summary = "특정 검색어와 역에 따른 사용자 조회",
		description = "주어진 searchQuery와 station에 해당하는 모든 사용자 정보를 반환합니다."
	)
	@GetMapping
	public List<User> getUsersBySearchQueryAndStation(@RequestParam String searchQuery, @RequestParam String station) {
		List<User> users = userRepository.findBySearchQueryAndStation(searchQuery, station);
		if (users.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "조건에 해당하는 사용자를 찾을 수 없습니다.");
		}
		return users;
	}
}