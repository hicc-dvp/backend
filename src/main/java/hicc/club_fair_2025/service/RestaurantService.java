package hicc.club_fair_2025.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import hicc.club_fair_2025.entity.Restaurant;
import hicc.club_fair_2025.repository.RestaurantRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class DiningService {
    private final RestaurantRepository diningRepository;
    private final ObjectMapper objectMapper;

    @Value("${naver.client-id}")
    private String clientId;

    @Value("${naver.client-secret}")
    private String clientSecret;


    public DiningService(RestaurantRepository diningRepository) {
        this.diningRepository = diningRepository;
        this.objectMapper = new ObjectMapper();
    }

    // ✅ 네이버 API에서 데이터를 가져오는 메서드 (Service에서 관리)
    public List<Map<String, String>> fetchMultipleQueriesFromNaver() {
        List<Map<String, String>> allResults = new ArrayList<>();
        String[] queries = {
                "홍대 한식 제육", "홍대 한식 국밥", "홍대 한식 백반", "홍대 한식 비빔밥", "홍대 한식 칼국수",
                "홍대 일식 스시", "홍대 일식 우동", "홍대 일식 덮밥", "홍대 일식 라멘", "홍대 일식 돈가스",
                "홍대 중식 짬뽕", "홍대 중식 마라탕", "홍대 중식 탄탄면", "홍대 중식 양꼬치", "홍대 중식 딤섬",
                "홍대 양식 파스타", "홍대 양식 피자", "홍대 양식 햄버거", "홍대 양식 샌드위치", "홍대 양식 스테이크",
        };

        RestTemplate restTemplate = new RestTemplate();

        for (String query : queries) {
            try {
                URI uri = UriComponentsBuilder
                        .fromUriString("https://openapi.naver.com")
                        .path("/v1/search/local.json")
                        .queryParam("query", query)
                        .queryParam("display", 1)  // 최대 5개, 식당 겹치도록 해야하니까 일단 1로 넣어둠
                        .queryParam("start", 1)  // start 값 변경 불가
                        .queryParam("sort", "random")  // 리뷰 많은 순 정렬이 성능이 너무 구려서 random으로 했음
                        .encode(StandardCharsets.UTF_8)
                        .build()
                        .toUri();

                RequestEntity<Void> req = RequestEntity
                        .get(uri)
                        .header("X-Naver-Client-Id", clientId)
                        .header("X-Naver-Client-Secret", clientSecret)
                        .header("User-Agent", "Mozilla/5.0")
                        .build();

                ResponseEntity<String> result = restTemplate.exchange(req, String.class);

                // ✅ JSON 형태로 저장하기 위해 Map 사용
                Map<String, String> resultMap = new HashMap<>();
                resultMap.put("query", query);
                resultMap.put("response", result.getBody());

                allResults.add(resultMap);

                Thread.sleep(50); // 이거 안넣으면 네이버 api 요청 과다로 에러 뜸

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Thread interrupted", e);
            } catch (HttpClientErrorException.TooManyRequests e) {
                System.out.println("❌ API Rate Limit 초과, 요청 중단.");
                break;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return allResults; // ✅ JSON 형식으로 검색어와 결과를 반환
    }


    @Transactional
    public void saveFromJsonList(List<Map<String, String>> jsonList) {
        try {
            List<Restaurant> diningList = new ArrayList<>();
            Set<String> existingTitles = new HashSet<>(diningRepository.findAllTitles()); // ✅ DB에서 모든 title 가져오기
            Set<String> newTitles = new HashSet<>(); // ✅ JSON에서 중복 방지를 위한 Set

            for (Map<String, String> jsonMap : jsonList) {
                String query = jsonMap.get("query");  // ✅ 검색어 정보
                String jsonData = jsonMap.get("response"); // ✅ API 응답 JSON

                if (jsonData == null || jsonData.isEmpty()) {
                    System.out.println("⚠️ [WARNING] 검색어 '" + query + "'의 응답 데이터가 없음. 스킵됨.");
                    continue;
                }

                // ✅ JSON 문자열을 JsonNode 객체로 변환
                JsonNode rootNode = objectMapper.readTree(jsonData);

                // ✅ "items" 필드가 존재하는지 확인
                if (!rootNode.has("items")) {
                    System.out.println("❌ [ERROR] 검색어 '" + query + "' 응답에 'items' 필드 없음! 스킵됨.");
                    continue;
                }

                JsonNode itemsNode = rootNode.get("items");

                // ✅ itemsNode가 비어있는지 확인
                if (itemsNode == null || !itemsNode.isArray()) {
                    System.out.println("❌ [ERROR] 검색어 '" + query + "'의 'items' 필드가 null이거나 배열이 아님! 스킵됨.");
                    continue;
                }

                // ✅ items 배열이 비어있는 경우 확인
                if (itemsNode.size() == 0) {
                    System.out.println("⚠️ [WARNING] 검색어 '" + query + "'의 'items' 배열이 비어 있음. 저장할 데이터 없음.");
                    continue;
                }

                // ✅ items 배열을 순회하면서 Dining 객체로 변환 후 저장
                for (JsonNode item : itemsNode) {
                    String title = item.get("title").asText();

                    // ✅ 중복 확인: DB에 존재하는지 + 현재 리스트에서 중복인지 체크
                    if (existingTitles.contains(title) || newTitles.contains(title)) {
                        System.out.println("⚠️ [WARNING] 검색어 '" + query + "'에서 중복된 데이터: " + title);
                        continue;
                    }

                    // ✅ Set에 추가 (중복 방지)
                    newTitles.add(title);

                    Restaurant dining = new Restaurant(
                            title,
                            item.has("category") ? item.get("category").asText() : "미분류",
                            item.has("address") ? item.get("address").asText() : "",
                            item.has("roadAddress") ? item.get("roadAddress").asText() : "",
                            item.has("mapx") ? item.get("mapx").asLong() : 0,  // ✅ 좌표 변환
                            item.has("mapy") ? item.get("mapy").asLong() : 0,   // ✅ 좌표 변환
                            query
                    );

                    diningList.add(dining);
                }
            }

            // ✅ 한꺼번에 DB에 저장 (Batch Insert)
            if (!diningList.isEmpty()) {
                diningRepository.saveAll(diningList);
                System.out.println("✅ JSON 데이터 저장 완료! 총 저장된 개수: " + diningList.size());
            } else {
                System.out.println("⚠️ 저장할 데이터가 없습니다.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("❌ JSON 데이터 저장 중 오류 발생: " + e.getMessage());
        }
    }
}
