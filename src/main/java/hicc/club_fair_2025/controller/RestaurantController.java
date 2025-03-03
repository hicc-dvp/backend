package hicc.club_fair_2025.controller;

import java.util.ArrayList;
import java.util.List;

import hicc.club_fair_2025.entity.Category;
import hicc.club_fair_2025.entity.Restaurant;
import hicc.club_fair_2025.entity.SearchQuery;
import hicc.club_fair_2025.repository.CategoryRepository;
import hicc.club_fair_2025.repository.SearchQueryRepository;
import hicc.club_fair_2025.service.CategoryService;
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
    private final CategoryService categoryService;

    public RestaurantController(RestaurantService restaurantService,
        SearchQueryRepository searchQueryRepository, CategoryService categoryService) {
        this.restaurantService = restaurantService;
        this.searchQueryRepository = searchQueryRepository;
        this.categoryService = categoryService;
    }

    @Operation(summary = "네이버 API 데이터 저장", description = "각 SearchQuery마다 네이버 API를 호출하여 첫 번째 식당 정보를 저장합니다.")
    @PostMapping(value = "/save", produces = "text/plain;charset=UTF-8")
    public String saveRestaurantPerSearchQuery() {
        int displayCount = 5;
        try {
            restaurantService.saveRestaurantPerSearchQuery(displayCount);
            return "저장 완료";
        } catch (Exception e) {
            e.printStackTrace();
            return "저장 실패: " + e.getMessage();
        }
    }

    @Operation(summary = "검색어 ID와 지하철역으로 식당 조회", description = "SearchQuery의 PK와 Station의 이름을 통해 해당 식당 정보를 조회합니다.search-queries/{queryId}/restaurant?station=홍대입구역 ")
    @GetMapping("/search-queries/{queryId}/restaurant")
    public List<Restaurant> getRestaurantByQueryIdAndStation(@PathVariable Long queryId, @RequestParam String station) {
        SearchQuery sq = searchQueryRepository.findById(queryId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "잘못된 queryId: " + queryId));
        return restaurantService.findBySearchQueryAndStation(sq.getQuery(), station);
    }

    @Operation(summary = "카테고리 ID와 지하철역으로 식당 조회", description = "Category의 PK와 Station의 이름을 통해 해당 식당 정보를 조회합니다.")
    @GetMapping("/categories/{categoryId}/restaurant")
    public List<Restaurant> getRestaurantByCategoryIdAndStation(@PathVariable Long categoryId, @RequestParam String station) {

        List<SearchQuery> queries = categoryService.findSearchQueriesByCategoryId(categoryId);
        List<Restaurant> allRestaurants = new ArrayList<>();

        for (SearchQuery query : queries) {
            List<Restaurant> restaurants = restaurantService.findBySearchQueryAndStation(query.getQuery(), station);
            allRestaurants.addAll(restaurants); // 리스트에 결과 추가
        }

        return allRestaurants;
    }

}