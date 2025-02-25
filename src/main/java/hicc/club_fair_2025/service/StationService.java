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
import java.util.*;

/**
 * StationService는 지정된 역(예: "홍대입구역", "상수역")에 대해 네이버 API를 호출하여
 * 역 정보를 파싱하고, Station 엔티티로 DB에 저장하는 기능을 제공합니다.
 */
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

    /**
     * 지정된 역 검색어 배열에 대해 네이버 API를 호출하고, Station 엔티티로 변환 후 DB에 저장합니다.
     *
     * @param queries      역 검색어 배열 (예: {"홍대입구역", "상수역"})
     * @param displayCount API에서 반환할 결과 건수 (일반적으로 1)
     */
    public void saveStations(String[] queries, int displayCount) {
        RestTemplate restTemplate = new RestTemplate();
        List<Station> stationList = new ArrayList<>();

        for (String query : queries) {
            try {
                // 네이버 API URL 구성
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

                // API 호출 및 응답 받기
                ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);
                System.out.println("Raw JSON response for station query '" + query + "': "
                    + responseEntity.getBody());

                // JSON 응답 파싱
                Map<String, Object> responseMap = objectMapper.readValue(
                    responseEntity.getBody(),
                    new TypeReference<Map<String, Object>>() {}
                );
                Integer total = (Integer) responseMap.get("total");
                System.out.println("Total results available: " + total);

                // items 배열를 List<Map<String, String>>으로 변환 후 첫 번째 결과 선택
                List<Map<String, String>> items = objectMapper.convertValue(
                    responseMap.get("items"),
                    new TypeReference<List<Map<String, String>>>() {}
                );

                if (items != null && !items.isEmpty()) {
                    Map<String, String> item = items.get(0);
                    System.out.println("선택된 역 정보 for query '" + query + "': " + item);

                    String title = item.get("title");
                    if (title != null) {
                        title = title.replaceAll("<[^>]*>", ""); // HTML 태그 제거
                    }
                    long mapx = 0, mapy = 0;
                    try {
                        mapx = Double.valueOf(item.get("mapx")).longValue();
                    } catch (Exception ex) {
                        System.err.println("mapx 파싱 오류: " + ex.getMessage());
                    }
                    try {
                        mapy = Double.valueOf(item.get("mapy")).longValue();
                    } catch (Exception ex) {
                        System.err.println("mapy 파싱 오류: " + ex.getMessage());
                    }

                    Station station = new Station(title, mapx, mapy, query);
                    stationList.add(station);
                } else {
                    System.out.println("검색 결과가 비어 있음 (total: " + total + ") for station query: " + query);
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("네이버 API 호출 중 오류 for station query: " + query + " : " + e.getMessage());
            }

            try {
                Thread.sleep(2000); // API 호출 간격 유지
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }

        if (!stationList.isEmpty()) {
            stationRepository.saveAll(stationList);
            System.out.println("Station 데이터 저장 완료! 총 저장된 개수: " + stationList.size());
        } else {
            System.out.println("저장할 Station 데이터가 없습니다.");
        }
    }
}