package hicc.club_fair_2025.service;

import hicc.club_fair_2025.entity.Restaurant;
import hicc.club_fair_2025.entity.SearchQuery;
import hicc.club_fair_2025.repository.RestaurantRepository;
import hicc.club_fair_2025.repository.SearchQueryRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Restaurant 엔티티 관련 비즈니스 로직과
 * 네이버 지역검색 API 연동을 담당하는 서비스
 */
@Service
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final SearchQueryRepository searchQueryRepository;

    @Value("${naver.client-id}")
    private String clientId;

    @Value("${naver.client-secret}")
    private String clientSecret;

    public RestaurantService(
        RestaurantRepository restaurantRepository,
        SearchQueryRepository searchQueryRepository
    ) {
        this.restaurantRepository = restaurantRepository;
        this.searchQueryRepository = searchQueryRepository;
    }

    /**
     * 주어진 searchQuery로 Restaurant를 조회합니다.
     * 찾지 못하면 null을 반환합니다.
     *
     * @param query 검색어
     * @return 해당 검색어에 해당하는 Restaurant 엔티티 (또는 null)
     */
    public Restaurant findBySearchQuery(String query) {
        return restaurantRepository.findBySearchQuery(query).orElse(null);
    }

    /**
     * Naver API 응답(JSON)을 Restaurant 엔티티로 변환 후 DB에 저장합니다.
     *
     * @param jsonData Naver API의 items 리스트
     */
    public void saveFromJsonList(List<Map<String, String>> jsonData) {
        List<Restaurant> restaurants = new ArrayList<>();

        for (Map<String, String> item : jsonData) {
            String name = item.get("title");
            String category = item.get("category");
            String roadAddress = item.get("roadAddress");

            double mapx = 0.0;
            double mapy = 0.0;
            try {
                mapx = Double.parseDouble(item.get("mapx"));
                mapy = Double.parseDouble(item.get("mapy"));
            } catch (NumberFormatException e) {
                System.err.println("mapx/mapy 파싱 오류. 기본값 0.0 적용: " + e.getMessage());
            }

            // HTML 태그 제거 (예: <b>태그</b>)
            if (name != null) {
                name = name.replaceAll("<[^>]*>", "");
            }

            Restaurant restaurant = new Restaurant(name, category, roadAddress, mapx, mapy);
            restaurants.add(restaurant);
        }

        restaurantRepository.saveAll(restaurants);
    }

    /**
     * DB에 저장된 모든 SearchQuery에 대해
     * 네이버 지역검색 API를 호출하고, 결과를 합쳐서 반환합니다.
     *
     * @return 검색 결과(Map<String, String>)들의 리스트
     */
    public List<Map<String, String>> fetchMultipleQueriesFromNaver() {
        RestTemplate restTemplate = new RestTemplate();
        List<SearchQuery> searchQueries = searchQueryRepository.findAll();
        List<Map<String, String>> result = new ArrayList<>();

        for (SearchQuery sq : searchQueries) {
            try {
                String query = sq.getQuery();
                String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
                String url = "https://openapi.naver.com/v1/search/local.json?query="
                    + encoded + "&display=5&start=1";

                HttpHeaders headers = new HttpHeaders();
                headers.set("X-Naver-Client-Id", clientId);
                headers.set("X-Naver-Client-Secret", clientSecret);

                HttpEntity<?> requestEntity = new HttpEntity<>(headers);
                ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.GET, requestEntity, Map.class
                );

                if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                    Map<String, Object> body = response.getBody();
                    List<Map<String, String>> items = (List<Map<String, String>>) body.get("items");
                    if (items != null) {
                        result.addAll(items);
                    }
                }
            } catch (Exception e) {
                System.err.println("네이버 API 호출 중 오류: " + e.getMessage());
            }
        }

        return result;
    }
}