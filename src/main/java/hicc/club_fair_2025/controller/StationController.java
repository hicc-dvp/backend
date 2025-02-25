package hicc.club_fair_2025.controller;

import hicc.club_fair_2025.service.StationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

/**
 * StationController는 네이버 API를 통해 역 정보를 DB에 저장하는 엔드포인트를 제공합니다.
 */
@Tag(name = "Station", description = "역(Station) 관련 API")
@RestController
@RequestMapping("/stations")
public class StationController {

    private final StationService stationService;

    public StationController(StationService stationService) {
        this.stationService = stationService;
    }

    /**
     * 지정된 역 검색어 배열에 대해 네이버 API를 호출하여, Station 데이터를 DB에 저장합니다.
     *
     * @return 저장 결과 메시지 ("저장 완료" 또는 오류 메시지)
     */
    @Operation(
        summary = "역 데이터 저장",
        description = "네이버 API를 통해 지정된 역 검색어(예: 홍대입구역, 상수역)에 대한 데이터를 DB에 저장합니다."
    )
    @PostMapping(value = "/save", produces = "text/plain;charset=UTF-8")
    public String saveStationData() {
        try {
            String[] queries = {"홍대입구역", "상수역"};
            int displayCount = 1;
            stationService.saveStations(queries, displayCount);
            return "저장 완료";
        } catch (Exception e) {
            e.printStackTrace();
            return "저장 실패: " + e.getMessage();
        }
    }
}