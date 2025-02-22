package hicc.club_fair_2025.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import hicc.club_fair_2025.domain.Dining;
import hicc.club_fair_2025.repository.DiningRepository;
import hicc.club_fair_2025.repository.JpaDiningRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class DiningService {
    private final DiningRepository diningRepository;
    private final ObjectMapper objectMapper;

    @Value("${naver.client-id}")
    private String clientId;

    @Value("${naver.client-secret}")
    private String clientSecret;


    public DiningService(DiningRepository diningRepository) {
        this.diningRepository = diningRepository;
        this.objectMapper = new ObjectMapper();
    }

    // ✅ 네이버 API에서 데이터를 가져오는 메서드 (Service에서 관리)
    public List<String> fetchMultipleQueriesFromNaver() {
        List<String> allResults = new ArrayList<>();
        String[] queries = {
                "홍대 맛집", "홍대 밥집", "홍대 레스토랑", "홍대 술집", "홍대 카페",
                "홍대 한식 맛집", "홍대 양식 맛집", "홍대 일식 맛집", "홍대 중식 맛집"
        };

        for (String query : queries) {
            URI uri = UriComponentsBuilder
                    .fromUriString("https://openapi.naver.com")
                    .path("/v1/search/local.json")
                    .queryParam("query", query)
                    .queryParam("display", 5)  // 최대 5개
                    .queryParam("start", 1)  // start 값 변경 불가
                    .queryParam("sort", "comment")  // 리뷰 많은 순 정렬
                    .encode(StandardCharsets.UTF_8)
                    .build()
                    .toUri();

            RestTemplate restTemplate = new RestTemplate();

            RequestEntity<Void> req = RequestEntity
                    .get(uri)
                    .header("X-Naver-Client-Id", clientId)
                    .header("X-Naver-Client-Secret", clientSecret)
                    .header("User-Agent", "Mozilla/5.0")
                    .build();

            ResponseEntity<String> result = restTemplate.exchange(req, String.class);

            allResults.add(result.getBody()); // ✅ 가져온 데이터 저장
        }

        return allResults; // ✅ 모든 검색어의 결과를 리스트로 반환
    }

    @Transactional
    public void saveFromJsonList(List<String> jsonList) {
        try {
            List<Dining> diningList = new ArrayList<>();

            for (String jsonData : jsonList) {
                // ✅ JSON 문자열을 JsonNode 객체로 변환
                JsonNode rootNode = objectMapper.readTree(jsonData);

                // ✅ "items" 필드가 존재하는지 확인
                if (!rootNode.has("items")) {
                    System.out.println("❌ [ERROR] 네이버 API 응답에 'items' 필드가 없음! 스킵됨.");
                    continue; // 다음 JSON 처리
                }

                JsonNode itemsNode = rootNode.get("items");

                // ✅ itemsNode가 비어있는지 확인
                if (itemsNode == null || !itemsNode.isArray()) {
                    System.out.println("❌ [ERROR] 'items' 필드가 null이거나 배열이 아님! 스킵됨.");
                    continue;
                }

                // ✅ items 배열이 비어있는 경우 확인
                if (itemsNode.size() == 0) {
                    System.out.println("⚠️ [WARNING] 'items' 배열이 비어 있음. 저장할 데이터 없음.");
                    continue;
                }

                // ✅ items 배열을 순회하면서 Dining 객체로 변환 후 저장
                for (JsonNode item : itemsNode) {
                    Dining dining = new Dining(
                            item.get("title").asText(),
                            item.get("category").asText(),
                            item.has("address") ? item.get("address").asText() : "",
                            item.has("roadAddress") ? item.get("roadAddress").asText() : "",
                            item.has("mapx") ? item.get("mapx").asLong() : 0,// ✅ 좌표 변환
                            item.has("mapy") ? item.get("mapy").asLong() : 0  // ✅ 좌표 변환
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
