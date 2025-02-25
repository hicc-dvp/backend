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
		// StationService 모킹
		stationService = Mockito.mock(StationService.class);
		// Controller 생성
		stationController = new StationController(stationService);
		// MockMvc 설정 (UTF-8 인코딩 필터 추가)
		mockMvc = MockMvcBuilders.standaloneSetup(stationController)
			.addFilters(new CharacterEncodingFilter("UTF-8", true))
			.defaultResponseCharacterEncoding(StandardCharsets.UTF_8)
			.build();
	}

	@DisplayName("POST /stations/save - 역 데이터 저장 (정상)")
	@Test
	void saveStationData_Success() throws Exception {
		// given: stationService.saveStations()가 예외 없이 정상 수행됨
		doNothing().when(stationService).saveStations(any(String[].class), anyInt());

		// when & then: POST 요청 시 "저장 완료" 메시지 반환
		mockMvc.perform(post("/stations/save"))
			.andExpect(status().isOk())
			.andExpect(content().string("저장 완료"));
	}

	@DisplayName("POST /stations/save - 역 데이터 저장 (실패)")
	@Test
	void saveStationData_Failure() throws Exception {
		// given: stationService.saveStations() 호출 시 예외 발생
		doThrow(new RuntimeException("API 호출 오류")).when(stationService)
			.saveStations(any(String[].class), anyInt());

		// when & then: POST 요청 시 오류 메시지 반환 ("저장 실패: API 호출 오류")
		mockMvc.perform(post("/stations/save"))
			.andExpect(status().isOk())
			.andExpect(content().string("저장 실패: API 호출 오류"));
	}
}