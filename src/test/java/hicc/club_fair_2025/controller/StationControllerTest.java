package hicc.club_fair_2025.controller;

import hicc.club_fair_2025.service.StationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.nio.charset.StandardCharsets;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class StationControllerTest {

	private MockMvc mockMvc;
	private StationService stationService;
	private StationController stationController;

	@BeforeEach
	void setUp() {
		stationService = Mockito.mock(StationService.class);
		stationController = new StationController(stationService);
		mockMvc = MockMvcBuilders.standaloneSetup(stationController)
			.addFilters(new CharacterEncodingFilter("UTF-8", true))
			.defaultResponseCharacterEncoding(StandardCharsets.UTF_8)
			.build();
	}

	@DisplayName("POST /stations/save - 역 데이터 저장 (정상)")
	@Test
	void saveStationData_Success() throws Exception {
		doNothing().when(stationService).saveStations(any(String[].class), anyInt());
		mockMvc.perform(post("/stations/save"))
			.andExpect(status().isOk())
			.andExpect(content().string("저장 완료"));
	}

	@DisplayName("POST /stations/save - 역 데이터 저장 (실패)")
	@Test
	void saveStationData_Failure() throws Exception {
		doThrow(new RuntimeException("API 호출 오류")).when(stationService).saveStations(any(String[].class), anyInt());
		mockMvc.perform(post("/stations/save"))
			.andExpect(status().isOk())
			.andExpect(content().string("저장 실패: API 호출 오류"));
	}
}