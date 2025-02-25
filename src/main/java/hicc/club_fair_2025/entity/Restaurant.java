package hicc.club_fair_2025.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 레스토랑(Entity) - 기본 정보(이름, 카테고리, 주소, 좌표 등)를 저장
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;         // 레스토랑 이름
    private String category;     // 예: 한식, 일식, 중식, 양식 등
    private String roadAddress;  // 도로명 주소
    private String link;

    @Column(unique = true)
    private String searchQuery;  // 1:1 매핑용 검색어

    public Restaurant(String name, String category, String roadAddress, String link) {
        this.name = name;
        this.category = category;
        this.roadAddress = roadAddress;
        this.link = link;
    }
}