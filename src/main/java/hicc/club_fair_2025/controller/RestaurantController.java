package hicc.club_fair_2025.controller;

import hicc.club_fair_2025.entity.Restaurant;
import hicc.club_fair_2025.entity.SearchQuery;
import hicc.club_fair_2025.repository.SearchQueryRepository;
import hicc.club_fair_2025.service.RestaurantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

/**
 * 식당(Restaurant) 관련 API 컨트롤러
 */
@Tag(name = "Restaurant", description = "식당 관련 API")
@RestController
@RequestMapping("/restaurants")
public class RestaurantController {

    private final RestaurantService restaurantService;
    private final SearchQueryRepository searchQueryRepository;

    public RestaurantController(RestaurantService restaurantService,
        SearchQueryRepository searchQueryRepository) {
        this.restaurantService = restaurantService;
        this.searchQueryRepository = searchQueryRepository;
    }

    /**
     * 네이버 API를 통해 식당 정보를 수집하고 DB에 저장
     */
    @Operation(summary = "네이버 API 데이터 저장", description = "네이버 지역검색 API로 검색어를 순회 호출하고, 응답 데이터를 DB에 저장합니다.")
    @PostMapping(
        value = "/save",
        produces = "text/plain;charset=UTF-8" // 한글 문자열을 UTF-8로 인코딩
    )
    public String saveNaverData() {
        try {
            List<Map<String, String>> jsonData = restaurantService.fetchMultipleQueriesFromNaver();
            restaurantService.saveFromJsonList(jsonData);
            return "저장 완료";
        } catch (Exception e) {
            e.printStackTrace();
            return "저장 실패: " + e.getMessage();
        }
    }

    /**
     * 특정 검색어 ID(queryId)에 해당하는 식당을 조회
     * @param queryId SearchQuery의 식별자(PK)
     */
    @Operation(summary = "검색어 ID로 식당 조회", description = "SearchQuery PK를 통해 식당 정보를 조회합니다.")
    @GetMapping("/search-queries/{queryId}/restaurant")
    public Restaurant getRestaurantByQueryId(@PathVariable Long queryId) {
        SearchQuery sq = searchQueryRepository.findById(queryId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "잘못된 queryId: " + queryId
            ));
        return restaurantService.findBySearchQuery(sq.getQuery());
    }
}