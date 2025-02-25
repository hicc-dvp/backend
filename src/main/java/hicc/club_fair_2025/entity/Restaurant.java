package hicc.club_fair_2025.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Restaurant 엔티티 - 식당의 기본 정보와 위치, 그리고 분류된 역 정보를 저장합니다.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;         // 식당 이름
    private String category;     // 예: 한식, 일식, 중식, 양식 등
    private String roadAddress;  // 도로명 주소

    @Column(unique = true)
    private String searchQuery;  // 1:1 매핑용 검색어

    // 네이버 API 응답 좌표 (mapx, mapy)
    private double mapx;
    private double mapy;

    // 분류된 역 정보 (예: "홍대입구역", "상수역")
    private String station;

    public Restaurant(String name, String category, String roadAddress) {
        this.name = name;
        this.category = category;
        this.roadAddress = roadAddress;
    }
}