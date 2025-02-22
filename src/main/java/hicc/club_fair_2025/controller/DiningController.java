package hicc.club_fair_2025.controller;

import hicc.club_fair_2025.service.DiningService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
@RestController
@RequestMapping("/dining")
public class DiningController {

    private final DiningService diningService;

    public DiningController(DiningService diningService) {
        this.diningService = diningService;
    }

    // 네이버 API에서 데이터 받아오기
    @GetMapping("/data")
    public String naver() {
        return diningService.fetchNaverData();
    }

    // 네이버 API 데이터를 DB에 저장
    @PostMapping("/save")
    public String saveNaverData() {
        try {

            String jsonData = diningService.fetchNaverData();

            diningService.saveFromJson(jsonData);

            return "저장 완료";
        } catch (Exception e) {
            e.printStackTrace();
            return "저장 실패: " + e.getMessage();
        }
    }
}