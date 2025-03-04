package hicc.club_fair_2025.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hicc.club_fair_2025.entity.Station;
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
public class StationService {

    private final StationRepository stationRepository;
    private final ObjectMapper objectMapper;

    @Value("${naver.client-id}")
    private String clientId;

    @Value("${naver.client-secret}")
    private String clientSecret;

    public StationService(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
        this.objectMapper = new ObjectMapper();
    }

    public void saveStations(String[] queries, int displayCount) {
        RestTemplate restTemplate = new RestTemplate();
        List<Station> stationList = new ArrayList<>();

        for (String query : queries) {
            try {
                URI uri = UriComponentsBuilder.fromUriString("https://openapi.naver.com")
                    .path("/v1/search/local.json")
                    .queryParam("query", query)
                    .queryParam("display", displayCount)
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
                System.out.println("Station query response for '" + query + "': " + responseEntity.getBody());

                Map<String, Object> responseMap = objectMapper.readValue(
                    responseEntity.getBody(),
                    new TypeReference<Map<String, Object>>() {}
                );
                Integer total = (Integer) responseMap.get("total");
                System.out.println("Total for '" + query + "': " + total);

                List<Map<String, String>> items = objectMapper.convertValue(
                    responseMap.get("items"),
                    new TypeReference<List<Map<String, String>>>() {}
                );

                if (items != null && !items.isEmpty()) {
                    Map<String, String> item = items.get(0);
                    String title = item.get("title");
                    if (title != null) {
                        title = title.replaceAll("<[^>]*>", "");
                    }
                    long mapx = 0, mapy = 0;
                    try {
                        mapx = Double.valueOf(item.get("mapx")).longValue();
                    } catch (Exception ex) {
                        System.err.println("mapx error: " + ex.getMessage());
                    }
                    try {
                        mapy = Double.valueOf(item.get("mapy")).longValue();
                    } catch (Exception ex) {
                        System.err.println("mapy error: " + ex.getMessage());
                    }
                    Station station = new Station(title, mapx, mapy, query);
                    stationList.add(station);
                } else {
                    System.out.println("Empty result for station query: " + query);
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("API error for station query: " + query + ": " + e.getMessage());
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
        if (!stationList.isEmpty()) {
            stationRepository.saveAll(stationList);
            System.out.println("Saved " + stationList.size() + " stations.");
        } else {
            System.out.println("No station data to save.");
        }
    }
}