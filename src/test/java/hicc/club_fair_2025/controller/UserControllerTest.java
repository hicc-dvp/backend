package hicc.club_fair_2025.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hicc.club_fair_2025.entity.Category;
import hicc.club_fair_2025.entity.Restaurant;
import hicc.club_fair_2025.entity.SearchQuery;
import hicc.club_fair_2025.entity.User;
import hicc.club_fair_2025.repository.RestaurantRepository;
import hicc.club_fair_2025.repository.SearchQueryRepository;
import hicc.club_fair_2025.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserControllerTest {

	private MockMvc mockMvc;
	private UserRepository userRepository;
	private UserController userController;
	private SearchQueryRepository searchQueryRepository;
	private ObjectMapper objectMapper;

	@BeforeEach
	void setUp() {
		userRepository = Mockito.mock(UserRepository.class);
		searchQueryRepository = Mockito.mock(SearchQueryRepository.class); // 추가
		userController = new UserController(userRepository, searchQueryRepository);
		objectMapper = new ObjectMapper();
		mockMvc = MockMvcBuilders.standaloneSetup(userController)
			.addFilters(new CharacterEncodingFilter("UTF-8", true))
			.defaultResponseCharacterEncoding(StandardCharsets.UTF_8)
			.build();
	}

	@DisplayName("사용자 등록 API 테스트")
	@Test
	void createUser_Success() throws Exception {
		Long searchQueryId = 1L;
		SearchQuery searchQuery = new SearchQuery("제육", new Category("한식"));
		searchQuery.setId(searchQueryId);
		User user = new User("insta123", "안녕하세요", searchQuery);
		user.setId(1L);
		when(userRepository.save(any(User.class))).thenReturn(user);
		mockMvc.perform(post("/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(user)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(1L))
			.andExpect(jsonPath("$.instagramId").value("insta123"))
			.andExpect(jsonPath("$.searchQuery.id").value(1L));
	}

	@DisplayName("특정 검색어 및 역에 따른 사용자 조회 API 테스트")
	@Test
	void getUsersByRestaurant_Success() throws Exception {
		Long searchQueryId = 1L;
		SearchQuery searchQuery = new SearchQuery("제육", new Category("한식"));
		searchQuery.setId(searchQueryId);
		User user = new User("insta123", "안녕하세요", searchQuery);
		user.setId(1L);
		when(userRepository.findBySearchQuery_Id(searchQueryId)).thenReturn(List.of(user));
		mockMvc.perform(get("/users/1"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].id").value(1L))
			.andExpect(jsonPath("$[0].instagramId").value("insta123"))
				.andExpect(jsonPath("$[0].searchQuery.id").value(1L));
	}
}