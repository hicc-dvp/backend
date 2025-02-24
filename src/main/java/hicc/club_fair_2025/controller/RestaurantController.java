package hicc.club_fair_2025.controller;

import hicc.club_fair_2025.service.RestaurantService;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dining")
public class DiningController {

    private final RestaurantService diningService;

    public DiningController(RestaurantService diningService) {
        this.diningService = diningService;
    }

    // 네이버 API에서 데이터 받아오기
    @GetMapping("/data")
    public List<Map<String, String>> naver() {
        return diningService.fetchMultipleQueriesFromNaver();
    }

    // 네이버 API 데이터를 DB에 저장
    @PostMapping("/save")
    public String saveNaverData() {
        try {

            List<Map<String, String>>  jsonData = diningService.fetchMultipleQueriesFromNaver();

            diningService.saveFromJsonList(jsonData);

            return "저장 완료";
        } catch (Exception e) {
            e.printStackTrace();
            return "저장 실패: " + e.getMessage();
        }
    }
}