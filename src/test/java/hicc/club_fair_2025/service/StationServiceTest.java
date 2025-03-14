package hicc.club_fair_2025.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import hicc.club_fair_2025.entity.Station;
import hicc.club_fair_2025.repository.StationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class StationServiceTest {

	@Mock
	private StationRepository stationRepository;

	@InjectMocks
	private StationService stationService;

	private ObjectMapper objectMapper = new ObjectMapper();

	@BeforeEach
	void setUp() {
	}

	@DisplayName("saveStations() - 정상 케이스")
	@Test
	void saveStations_Success() throws Exception {
		String[] queries = {"홍대입구역", "상수역"};
		Map<String, Object> fakeResponse = new HashMap<>();
		fakeResponse.put("total", 1);
		List<Map<String, String>> items = new ArrayList<>();
		items.add(Map.of(
			"title", "<b>홍대입구역</b>",
			"mapx", "1269211407",
			"mapy", "375534442"
		));
		fakeResponse.put("items", items);
		String fakeJson = objectMapper.writeValueAsString(fakeResponse);
		ResponseEntity<String> fakeResponseEntity = new ResponseEntity<>(fakeJson, HttpStatus.OK);

		try (MockedConstruction<RestTemplate> mocked = Mockito.mockConstruction(RestTemplate.class,
			(mock, context) -> {
				when(mock.exchange(any(RequestEntity.class), eq(String.class)))
					.thenReturn(fakeResponseEntity);
			})) {
			stationService.saveStations(queries, 1);
		}

		then(stationRepository).should(times(1)).saveAll(anyList());
	}
}