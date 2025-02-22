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


    public DiningService(DiningRepository diningRepository)
    {
        this.diningRepository = diningRepository;
        this.objectMapper = new ObjectMapper();
    }

    // ✅ 네이버 API에서 데이터를 가져오는 메서드 (Service에서 관리)
    public String fetchNaverData() {
        URI uri = UriComponentsBuilder
                .fromUriString("https://openapi.naver.com")
                .path("/v1/search/local.json")
                .queryParam("query", "홍대 맛집")
                .queryParam("display", 5)
                .queryParam("start", 1)
                .queryParam("sort", "random")
                .encode(StandardCharsets.UTF_8)
                .build()
                .toUri();

        RestTemplate restTemplate = new RestTemplate();

        RequestEntity<Void> req = RequestEntity
                .get(uri)
                .header("X-Naver-Client-Id", clientId)
                .header("X-Naver-Client-Secret", clientSecret)
                .build();

        ResponseEntity<String> result = restTemplate.exchange(req, String.class);
        return result.getBody();
    }
    @Transactional
    public void saveFromJson(String jsonData) {
        try {
            // JSON 문자열을 JsonNode 객체로 변환
            JsonNode rootNode = objectMapper.readTree(jsonData);
            JsonNode itemsNode = rootNode.get("items");

            List<Dining> diningList = new ArrayList<>();

            // items 배열을 순회하면서 Dining 객체로 변환 후 저장
            for (JsonNode item : itemsNode) {
                Dining dining = new Dining(
                        item.get("title").asText(),
                        item.get("category").asText(),
                        item.has("address") ? item.get("address").asText() : "",
                        item.has("roadAddress") ? item.get("roadAddress").asText() : "",
                        item.has("mapx") ? item.get("mapx").asLong() : 0, // 좌표 변환
                        item.has("mapy") ? item.get("mapy").asLong() : 0  // 좌표 변환
                );
                diningList.add(dining);
            }

            // DB에 저장
            for (Dining dining : diningList) {
                diningRepository.save(dining);
            }

            System.out.println("✅ JSON 데이터 저장 완료!");

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("❌ JSON 데이터 저장 중 오류 발생: " + e.getMessage());
        }
    }
}
