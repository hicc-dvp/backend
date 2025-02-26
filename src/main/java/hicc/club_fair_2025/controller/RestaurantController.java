package hicc.club_fair_2025.controller;

import java.util.List;

import hicc.club_fair_2025.entity.Restaurant;
import hicc.club_fair_2025.entity.SearchQuery;
import hicc.club_fair_2025.repository.SearchQueryRepository;
import hicc.club_fair_2025.service.RestaurantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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

    @Operation(summary = "네이버 API 데이터 저장", description = "각 SearchQuery마다 네이버 API를 호출하여 첫 번째 식당 정보를 저장합니다.")
    @PostMapping(value = "/saveOne", produces = "text/plain;charset=UTF-8")
    public String saveOneRestaurantPerSearchQuery() {
        try {
            restaurantService.saveOneRestaurantPerSearchQuery();
            return "저장 완료";
        } catch (Exception e) {
            e.printStackTrace();
            return "저장 실패: " + e.getMessage();
        }
    }

    @Operation(summary = "검색어 ID로 식당 조회", description = "SearchQuery의 PK를 통해 해당 식당 정보를 조회합니다.")
    @GetMapping("/search-queries/{queryId}/restaurant")
    public List<Restaurant> getRestaurantByQueryId(@PathVariable Long queryId) {
        SearchQuery sq = searchQueryRepository.findById(queryId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "잘못된 queryId: " + queryId));
        return restaurantService.findBySearchQuery(sq.getQuery());
    }
}