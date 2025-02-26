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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    public List<Restaurant> findBySearchQuery(String query) {
        return restaurantRepository.findBySearchQuery(query);
    }

    public void saveOneRestaurantPerSearchQuery() {
        RestTemplate restTemplate = new RestTemplate();
        List<SearchQuery> searchQueries = searchQueryRepository.findAll();
        String[] stationPrefixes = {"홍대입구역", "상수역"};

        for (SearchQuery sq : searchQueries) {
            List<Restaurant> candidateList = new ArrayList<>();
            for (String stationPrefix : stationPrefixes) {
                try {
                    String categoryName = (sq.getCategory() != null) ? sq.getCategory().getName() : "";
                    String finalQuery = stationPrefix + " " + categoryName + " " + sq.getQuery();
                    System.out.println("Query: " + finalQuery);

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
                    System.out.println("Response: " + responseEntity.getBody());

                    Map<String, Object> responseMap = objectMapper.readValue(
                        responseEntity.getBody(),
                        new TypeReference<Map<String, Object>>() {}
                    );
                    Integer total = (Integer) responseMap.get("total");
                    System.out.println("Total: " + total);

                    List<Map<String, String>> items = objectMapper.convertValue(
                        responseMap.get("items"),
                        new TypeReference<List<Map<String, String>>>() {}
                    );

                    if (items != null && !items.isEmpty()) {
                        Map<String, String> item = items.get(0);
                        String restaurantName = item.get("title");
                        if (restaurantName != null) {
                            restaurantName = restaurantName.replaceAll("<[^>]*>", "");
                        }
                        String restaurantCategory = item.get("category");
                        String roadAddress = item.get("roadAddress");

                        double mapx = 0, mapy = 0;
                        try {
                            mapx = Double.parseDouble(item.get("mapx"));
                        } catch (Exception ex) {
                            System.err.println("mapx error: " + ex.getMessage());
                        }
                        try {
                            mapy = Double.parseDouble(item.get("mapy"));
                        } catch (Exception ex) {
                            System.err.println("mapy error: " + ex.getMessage());
                        }

                        Restaurant candidate = new Restaurant(restaurantName, restaurantCategory, roadAddress);
                        candidate.setMapx(mapx);
                        candidate.setMapy(mapy);
                        candidate.setSearchQuery(stationPrefix + " " + sq.getQuery());
                        candidate.setStation(stationPrefix);
                        candidateList.add(candidate);
                    } else {
                        System.out.println("Empty result for " + sq.getQuery() + " with prefix " + stationPrefix);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.err.println("API error for " + sq.getQuery() + " with prefix " + stationPrefix + ": " + e.getMessage());
                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }

            for (Restaurant candidate : candidateList) {
                Station station1 = stationRepository.findByName("홍대입구역")
                    .orElseGet(() -> new Station("홍대입구역", 1269240000, 375550000, "홍대입구역"));
                Station station2 = stationRepository.findByName("상수역")
                    .orElseGet(() -> new Station("상수역", 1269180000, 375480000, "상수역"));

                double distance1 = calculateDistance((long) candidate.getMapx(), (long) candidate.getMapy(),
                    station1.getMapx(), station1.getMapy());
                double distance2 = calculateDistance((long) candidate.getMapx(), (long) candidate.getMapy(),
                    station2.getMapx(), station2.getMapy());
                if (distance1 < distance2) {
                    candidate.setStation("홍대입구역");
                } else {
                    candidate.setStation("상수역");
                }
            }

            if (!candidateList.isEmpty()) {
                restaurantRepository.saveAll(candidateList);
                System.out.println(candidateList.size() + " restaurants saved for query: " + sq.getQuery());
            } else {
                System.out.println("No candidates for query: " + sq.getQuery());
            }
        }
    }

    private double calculateDistance(long x1, long y1, long x2, long y2) {
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }
}