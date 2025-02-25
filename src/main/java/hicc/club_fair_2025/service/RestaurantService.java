package hicc.club_fair_2025.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hicc.club_fair_2025.entity.Restaurant;
import hicc.club_fair_2025.entity.SearchQuery;
import hicc.club_fair_2025.repository.RestaurantRepository;
import hicc.club_fair_2025.repository.SearchQueryRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * RestaurantService는 DB에 저장된 SearchQuery를 기반으로 네이버 지역검색 API를 호출하여
 * 식당 정보를 조회하고 DB에 저장하는 기능을 제공합니다.
 */
@Service
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final SearchQueryRepository searchQueryRepository;
    private final ObjectMapper objectMapper;

    @Value("${naver.client-id}")
    private String clientId;

    @Value("${naver.client-secret}")
    private String clientSecret;

    public RestaurantService(RestaurantRepository restaurantRepository,
        SearchQueryRepository searchQueryRepository) {
        this.restaurantRepository = restaurantRepository;
        this.searchQueryRepository = searchQueryRepository;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 주어진 검색어와 일치하는 Restaurant 엔티티를 DB에서 조회합니다.
     *
     * @param query 검색어
     * @return 일치하는 Restaurant 엔티티가 없으면 null 반환
     */
    public Restaurant findBySearchQuery(String query) {
        return restaurantRepository.findBySearchQuery(query).orElse(null);
    }

    /**
     * 네이버 API의 응답 JSON에서 다수의 식당 정보를 파싱하여 Restaurant 엔티티로 변환한 후 저장합니다.
     * (참고용 메서드로, 여러 결과를 저장할 때 사용합니다)
     *
     * @param jsonData 네이버 API의 items 배열 (각 항목은 Map<String, String> 형식)
     */
    public void saveFromJsonList(List<Map<String, String>> jsonData) {
        List<Restaurant> restaurants = new ArrayList<>();
        for (Map<String, String> item : jsonData) {
            String name = item.get("title");
            String category = item.get("category");
            String roadAddress = item.get("roadAddress");

            // HTML 태그 제거
            if (name != null) {
                String cleanedName = name.replaceAll("<[^>]*>", "");
                System.out.println("원본 제목: " + name + " -> 정제된 제목: " + cleanedName);
                name = cleanedName;
            }

            Restaurant restaurant = new Restaurant(name, category, roadAddress);
            // 이 메서드는 SearchQuery 값 저장 없이 여러 결과를 저장할 때 사용됨
            restaurants.add(restaurant);
        }
        System.out.println("최종 저장할 Restaurant 수: " + restaurants.size());
        restaurantRepository.saveAll(restaurants);
    }

    /**
     * 각 SearchQuery마다 네이버 지역검색 API를 호출하여, 첫 번째 식당 결과만 파싱 후 Restaurant 엔티티로 저장합니다.
     * 이 때, 해당 SearchQuery 값을 Restaurant 엔티티의 searchQuery 필드에 함께 저장합니다.
     */
    public void saveOneRestaurantPerSearchQuery() {
        RestTemplate restTemplate = new RestTemplate();
        List<SearchQuery> searchQueries = searchQueryRepository.findAll();

        for (SearchQuery sq : searchQueries) {
            try {
                // 최종 검색어 구성: 예) "홍대 한식 국밥"
                String categoryName = (sq.getCategory() != null) ? sq.getCategory().getName() : "";
                String finalQuery = "홍대 " + categoryName + " " + sq.getQuery();
                System.out.println("원본 query: " + sq.getQuery() + ", 최종 조합: " + finalQuery);

                // UriComponentsBuilder를 사용해 API 요청 URL을 생성 (UTF-8 인코딩 적용)
                URI uri = UriComponentsBuilder.fromUriString("https://openapi.naver.com")
                    .path("/v1/search/local.json")
                    .queryParam("query", finalQuery)
                    .queryParam("display", 1)
                    .queryParam("start", 1)
                    .build()
                    .encode(StandardCharsets.UTF_8)
                    .toUri();

                // curl과 동일한 헤더 설정
                HttpHeaders headers = new HttpHeaders();
                headers.set("X-Naver-Client-Id", clientId);
                headers.set("X-Naver-Client-Secret", clientSecret);
                headers.set("User-Agent", "curl/8.7.1");

                RequestEntity<Void> requestEntity = RequestEntity.get(uri)
                    .headers(headers)
                    .build();

                // 네이버 API 호출 및 응답 받기 (JSON 문자열)
                ResponseEntity<String> responseEntity = restTemplate.exchange(
                    requestEntity,
                    String.class
                );
                System.out.println("Raw JSON response for query '" + sq.getQuery() + "': " + responseEntity.getBody());

                // 응답 JSON 파싱
                Map<String, Object> responseMap = objectMapper.readValue(
                    responseEntity.getBody(),
                    new TypeReference<Map<String, Object>>() {}
                );
                Integer total = (Integer) responseMap.get("total");
                System.out.println("Total results available: " + total);

                // "items" 배열을 List<Map<String, String>>으로 변환하고, 첫 번째 결과만 사용
                List<Map<String, String>> items = objectMapper.convertValue(
                    responseMap.get("items"),
                    new TypeReference<List<Map<String, String>>>() {}
                );

                if (items != null && !items.isEmpty()) {
                    Map<String, String> item = items.get(0);
                    System.out.println("선택된 식당: " + item);

                    String restaurantName = item.get("title");
                    if (restaurantName != null) {
                        String cleanedName = restaurantName.replaceAll("<[^>]*>", "");
                        System.out.println("원본 제목: " + restaurantName + " -> 정제된 제목: " + cleanedName);
                        restaurantName = cleanedName;
                    }
                    String restaurantCategory = item.get("category");
                    String roadAddress = item.get("roadAddress");

                    Restaurant restaurant = new Restaurant(restaurantName, restaurantCategory, roadAddress);
                    // 저장 시 SearchQuery 값도 함께 저장
                    restaurant.setSearchQuery(sq.getQuery());

                    restaurantRepository.save(restaurant);
                    System.out.println("식당 저장 완료 for query: " + sq.getQuery());
                } else {
                    System.out.println("검색 결과가 비어 있음 (total: " + total + ") for query: " + sq.getQuery());
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("네이버 API 호출 중 오류 for query: " + sq.getQuery() + " : " + e.getMessage());
            }

            try {
                Thread.sleep(2000); // API 호출 간격 유지
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * 모든 SearchQuery에 대해 네이버 API를 호출하고, 응답 결과를 반환합니다.
     * (여러 결과를 저장하는 기존 방식 – 참고용)
     *
     * @return 검색 결과(Map<String, String>)들의 리스트
     */
    public List<Map<String, String>> fetchMultipleQueriesFromNaver() {
        RestTemplate restTemplate = new RestTemplate();
        List<SearchQuery> searchQueries = searchQueryRepository.findAll();
        List<Map<String, String>> result = new ArrayList<>();

        for (SearchQuery sq : searchQueries) {
            try {
                String categoryName = (sq.getCategory() != null) ? sq.getCategory().getName() : "";
                String finalQuery = "홍대 " + categoryName + " " + sq.getQuery();
                System.out.println("원본 query: " + sq.getQuery() + ", 최종 조합: " + finalQuery);

                URI uri = UriComponentsBuilder.fromUriString("https://openapi.naver.com")
                    .path("/v1/search/local.json")
                    .queryParam("query", finalQuery)
                    .queryParam("display", 1)
                    .queryParam("start", 1)
                    .build()
                    .encode(StandardCharsets.UTF_8)
                    .toUri();

                HttpHeaders headers = new HttpHeaders();
                headers.set("X-Naver-Client-Id", clientId);
                headers.set("X-Naver-Client-Secret", clientSecret);
                headers.set("User-Agent", "curl/8.7.1");

                RequestEntity<Void> requestEntity = RequestEntity.get(uri)
                    .headers(headers)
                    .build();

                ResponseEntity<String> stringResponse = restTemplate.exchange(
                    requestEntity,
                    String.class
                );
                System.out.println("Raw JSON response: " + stringResponse.getBody());

                Map<String, Object> responseMap = objectMapper.readValue(
                    stringResponse.getBody(),
                    new TypeReference<Map<String, Object>>() {}
                );
                Integer total = (Integer) responseMap.get("total");
                System.out.println("Total results available: " + total);

                List<Map<String, String>> items = objectMapper.convertValue(
                    responseMap.get("items"),
                    new TypeReference<List<Map<String, String>>>() {}
                );
                if (items != null && !items.isEmpty()) {
                    System.out.println("검색 결과 아이템 수: " + items.size());
                    System.out.println("첫 번째 항목: " + items.get(0));
                    result.addAll(items);
                } else {
                    System.out.println("검색 결과가 비어 있음 (total: " + total + ") for query: " + sq.getQuery());
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("네이버 API 호출 중 오류: " + e.getMessage());
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
        return result;
    }
}