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
	private final RestaurantRepository restaurantRepository;

	public UserController(UserRepository userRepository, RestaurantRepository restaurantRepository) {
		this.userRepository = userRepository;
		this.restaurantRepository = restaurantRepository;
	}

	@Operation(summary = "사용자 등록", description = "새로운 사용자를 등록합니다.")
	@PostMapping
	public User createUser(@RequestBody UserDto UserDto) {
		Restaurant restaurant = restaurantRepository.findById(UserDto.getRestaurantId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 searchQuerytId 없음"));
		User user = new User(UserDto.getInstagramId(), UserDto.getIntroduction(), restaurant);
		return userRepository.save(user);
	}

	@Operation(summary = "레스토랑ID로 사용자 조회", description = "선택한 서치쿼리에 맞는 사용자 정보를 조회합니다.")
	@GetMapping("/{restaurantId}")
	public List<UserDto> getUsersByRestaurantId(@PathVariable Long restaurantId) {

		Restaurant restaurant = restaurantRepository.findById(restaurantId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 RestaurantId 없음"));

		String searchQuery = restaurant.getSearchQuery();

		List<User> users =  userRepository.findByRestaurant_SearchQuery(searchQuery);
		if (users.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "조건에 해당하는 사용자를 찾을 수 없습니다.");
		}
		return users.stream().map(UserDto::new).toList();
	}



}