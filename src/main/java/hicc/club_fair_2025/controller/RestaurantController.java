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

/**
 * RestaurantController는 클라이언트로부터의 요청을 받아
 * RestaurantService를 통해 식당 데이터를 저장 및 조회하는 REST API를 제공합니다.
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
     * 각 SearchQuery마다 네이버 API를 호출하여 첫 번째 식당 정보를 DB에 저장합니다.
     *
     * @return "저장 완료" 문자열 (저장 실패 시 오류 메시지 포함)
     */
    @Operation(
        summary = "네이버 API 데이터 저장",
        description = "각 SearchQuery마다 네이버 API를 호출하여 첫 번째 식당 정보를 저장합니다."
    )
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

    /**
     * 특정 SearchQuery의 ID에 해당하는 식당 정보를 조회합니다.
     *
     * @param queryId SearchQuery의 기본 키
     * @return 해당 검색어에 매핑된 Restaurant 엔티티 (없으면 HTTP 400 에러)
     */
    @Operation(
        summary = "검색어 ID로 식당 조회",
        description = "SearchQuery의 PK를 통해 해당 식당 정보를 모두 조회합니다."
    )
    @GetMapping("/search-queries/{queryId}/restaurant")
    public List<Restaurant> getRestaurantByQueryId(@PathVariable Long queryId) {
        SearchQuery sq = searchQueryRepository.findById(queryId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "잘못된 queryId: " + queryId
            ));
        return restaurantService.findBySearchQuery(sq.getQuery());
    }
}