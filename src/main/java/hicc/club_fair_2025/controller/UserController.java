package hicc.club_fair_2025.controller;

import hicc.club_fair_2025.dto.UserDto;
import hicc.club_fair_2025.entity.Restaurant;
import hicc.club_fair_2025.entity.SearchQuery;
import hicc.club_fair_2025.entity.User;
import hicc.club_fair_2025.repository.RestaurantRepository;
import hicc.club_fair_2025.repository.SearchQueryRepository;
import hicc.club_fair_2025.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Tag(name = "User", description = "사용자 관련 API")
@RestController
@RequestMapping("/users")
public class UserController {

	private final UserRepository userRepository;
	private final SearchQueryRepository searchQueryRepository;

	public UserController(UserRepository userRepository, SearchQueryRepository searchQueryRepository) {
		this.userRepository = userRepository;
		this.searchQueryRepository = searchQueryRepository;
	}

	@Operation(summary = "사용자 등록", description = "새로운 사용자를 등록합니다.")
	@PostMapping
	public User createUser(@RequestBody UserDto UserDto) {
		SearchQuery searchQuery = searchQueryRepository.findById(UserDto.getSearchQueryId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 searchQuerytId 없음"));
		User user = new User(UserDto.getInstagramId(), UserDto.getIntroduction(), searchQuery);
		return userRepository.save(user);
	}

	@Operation(summary = "서치쿼리 ID로 사용자 조회", description = "선택한 서치쿼리에 맞는 사용자 정보를 조회합니다.")
	@GetMapping("/{searchQueryId}")
	public List<UserDto> getUsersBySearchQuery(@PathVariable Long searchQueryId) {
		List<User> users = userRepository.findBySearchQuery_Id(searchQueryId);
		if (users.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "조건에 해당하는 사용자를 찾을 수 없습니다.");
		}
		return users.stream().map(UserDto::new).toList();
	}



}