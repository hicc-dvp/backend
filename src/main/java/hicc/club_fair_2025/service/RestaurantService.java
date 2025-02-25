package hicc.club_fair_2025.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hicc.club_fair_2025.entity.Restaurant;
import hicc.club_fair_2025.entity.SearchQuery;
import hicc.club_fair_2025.entity.Station;
import hicc.club_fair_2025.repository.RestaurantRepository;
import hicc.club_fair_2025.repository.SearchQueryRepository;
import hicc.club_fair_2025.repository.StationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final SearchQueryRepository searchQueryRepository;
    private final StationRepository stationRepository;
    private final ObjectMapper objectMapper;

    @Value("${naver.client-id}")
    private String clientId;

    @Value("${naver.client-secret}")
    private String clientSecret;

    public RestaurantService(RestaurantRepository restaurantRepository,
        SearchQueryRepository searchQueryRepository,
        StationRepository stationRepository) {
        this.restaurantRepository = restaurantRepository;
        this.searchQueryRepository = searchQueryRepository;
        this.stationRepository = stationRepository;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 주어진 검색어와 일치하는 Restaurant 엔티티 리스트를 DB에서 조회합니다.
     *
     * @param query 검색어 (SearchQuery의 query 값과 일치해야 함)
     * @return 해당 검색어에 매핑된 Restaurant 엔티티 리스트 (없으면 빈 리스트)
     */
    public List<Restaurant> findBySearchQuery(String query) {
        return restaurantRepository.findBySearchQuery(query);
    }

    /**
     * 각 SearchQuery마다 네이버 지역검색 API를 호출하여,
     * 두 가지 역 후보(예: "홍대입구역", "상수역")에 대해 첫 번째 식당 결과를 각각 구합니다.
     * 각 후보에 대해, DB에 저장된 Station 정보(없으면 기본 좌표 사용)를 두 역 모두 조회하여
     * 두 역과의 거리를 비교하고, 더 가까운 역의 이름으로 candidate.station을 업데이트한 후
     * 후보 리스트에 담긴 모든 Restaurant 객체를 저장합니다.
     *
     * 저장 시에는:
     * - mapx, mapy: 네이버 API 응답에서 추출한 좌표 값,
     * - station: 두 역 중 더 가까운 역의 이름,
     * - searchQuery: "역 접두어 + 원래 검색어" 형태로 저장됩니다.
     */
    public void saveOneRestaurantPerSearchQuery() {
        RestTemplate restTemplate = new RestTemplate();
        List<SearchQuery> searchQueries = searchQueryRepository.findAll();
        // 두 가지 역 후보 접두어 배열
        String[] stationPrefixes = {"홍대입구역", "상수역"};

        for (SearchQuery sq : searchQueries) {
            List<Restaurant> candidateList = new ArrayList<>();
            for (String stationPrefix : stationPrefixes) {
                try {
                    String categoryName = (sq.getCategory() != null) ? sq.getCategory().getName() : "";
                    // 최종 검색어 구성: 예) "홍대입구역 한식 국밥" 또는 "상수역 한식 국밥"
                    String finalQuery = stationPrefix + " " + categoryName + " " + sq.getQuery();
                    System.out.println("원본 query: " + sq.getQuery() + ", 접두어: " + stationPrefix + ", 최종 조합: " + finalQuery);

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

                    ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);
                    System.out.println("Raw JSON response for query '" + sq.getQuery() + "' with prefix '" + stationPrefix + "': "
                        + responseEntity.getBody());

                    Map<String, Object> responseMap = objectMapper.readValue(
                        responseEntity.getBody(),
                        new TypeReference<Map<String, Object>>() {}
                    );
                    Integer total = (Integer) responseMap.get("total");
                    System.out.println("Total results available: " + total);

                    List<Map<String, String>> items = objectMapper.convertValue(
                        responseMap.get("items"),
                        new TypeReference<List<Map<String, String>>>() {}
                    );

                    if (items != null && !items.isEmpty()) {
                        Map<String, String> item = items.get(0);
                        System.out.println("선택된 식당 (prefix " + stationPrefix + "): " + item);

                        String restaurantName = item.get("title");
                        if (restaurantName != null) {
                            String cleanedName = restaurantName.replaceAll("<[^>]*>", "");
                            System.out.println("원본 제목: " + restaurantName + " -> 정제된 제목: " + cleanedName);
                            restaurantName = cleanedName;
                        }
                        String restaurantCategory = item.get("category");
                        String roadAddress = item.get("roadAddress");

                        double mapx = 0, mapy = 0;
                        try {
                            mapx = Double.parseDouble(item.get("mapx"));
                        } catch (Exception ex) {
                            System.err.println("mapx 파싱 오류: " + ex.getMessage());
                        }
                        try {
                            mapy = Double.parseDouble(item.get("mapy"));
                        } catch (Exception ex) {
                            System.err.println("mapy 파싱 오류: " + ex.getMessage());
                        }

                        Restaurant candidate = new Restaurant(restaurantName, restaurantCategory, roadAddress);
                        candidate.setMapx(mapx);
                        candidate.setMapy(mapy);
                        // 임시로 각 후보는 원래 접두어로 searchQuery와 station을 설정함
                        candidate.setSearchQuery(stationPrefix + " " + sq.getQuery());
                        candidate.setStation(stationPrefix);

                        candidateList.add(candidate);
                    } else {
                        System.out.println("검색 결과가 비어 있음 (total: " + total + ") for query: "
                            + sq.getQuery() + " with prefix: " + stationPrefix);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.err.println("네이버 API 호출 중 오류 for query: " + sq.getQuery()
                        + " with prefix: " + stationPrefix + " : " + e.getMessage());
                }
                try {
                    Thread.sleep(2000); // API 호출 간격 유지
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            } // end for each stationPrefix

            // 각 후보에 대해 두 역("홍대입구역"과 "상수역")의 좌표와의 거리를 비교하여,
            // 더 가까운 역의 이름으로 candidate.station을 업데이트합니다.
            for (Restaurant candidate : candidateList) {
                // DB에서 두 역 정보를 조회 (없으면 기본 좌표 사용)
                Station station1 = stationRepository.findByName("홍대입구역")
                    .orElseGet(() -> {
                        Station defaultStation = new Station("홍대입구역", 1269240000, 375550000, "홍대입구역");
                        System.out.println("DB에 Station 정보가 없어서 기본 좌표 사용: " + defaultStation.getName());
                        return defaultStation;
                    });
                Station station2 = stationRepository.findByName("상수역")
                    .orElseGet(() -> {
                        Station defaultStation = new Station("상수역", 1269180000, 375480000, "상수역");
                        System.out.println("DB에 Station 정보가 없어서 기본 좌표 사용: " + defaultStation.getName());
                        return defaultStation;
                    });

                double distance1 = calculateDistance((long) candidate.getMapx(), (long) candidate.getMapy(),
                    station1.getMapx(), station1.getMapy());
                double distance2 = calculateDistance((long) candidate.getMapx(), (long) candidate.getMapy(),
                    station2.getMapx(), station2.getMapy());
                System.out.println("Distance for candidate (" + candidate.getSearchQuery() + "): "
                    + distance1 + " (홍대입구역) vs " + distance2 + " (상수역)");

                // 두 역 중 더 가까운 역의 이름으로 candidate.station 업데이트
                if (distance1 < distance2) {
                    candidate.setStation("홍대입구역");
                } else {
                    candidate.setStation("상수역");
                }
            }

            // 각 SearchQuery에 대해, 두 후보 모두 저장 (각각의 searchQuery 값은 접두어와 원래 검색어 조합)
            if (!candidateList.isEmpty()) {
                restaurantRepository.saveAll(candidateList);
                System.out.println("각 SearchQuery에 대해 " + candidateList.size() + "개의 식당이 저장되었습니다 for query: " + sq.getQuery());
            } else {
                System.out.println("어떠한 후보도 생성되지 않음 for query: " + sq.getQuery());
            }
        } // end for each SearchQuery
    }

    /**
     * 두 좌표 간 유클리드 거리를 계산합니다.
     *
     * @param x1 첫 번째 좌표 x
     * @param y1 첫 번째 좌표 y
     * @param x2 두 번째 좌표 x
     * @param y2 두 번째 좌표 y
     * @return 두 점 사이의 유클리드 거리
     */
    private double calculateDistance(long x1, long y1, long x2, long y2) {
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }
}