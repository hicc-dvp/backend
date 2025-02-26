package hicc.club_fair_2025.controller;

import hicc.club_fair_2025.service.StationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Station", description = "역(Station) 관련 API")
@RestController
@RequestMapping("/stations")
public class StationController {

    private final StationService stationService;

    public StationController(StationService stationService) {
        this.stationService = stationService;
    }

    @Operation(summary = "역 데이터 저장", description = "네이버 API를 통해 지정된 역 데이터를 DB에 저장합니다.")
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