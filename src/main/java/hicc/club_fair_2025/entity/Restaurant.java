package hicc.club_fair_2025.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;         // 식당명
    private String category;     // 카테고리
    private String address;      // 지번 주소
    private String roadAddress;  // 도로명 주소
    private double mapx;         // 경도 (Naver API의 mapx)
    private double mapy;         // 위도 (Naver API의 mapy)
    private String link;         // 네이버 지도 링크 등 추가 정보

    public Restaurant(String name, String category, String address, String roadAddress,
        double mapx, double mapy, String link) {
        this.name = name;
        this.category = category;
        this.address = address;
        this.roadAddress = roadAddress;
        this.mapx = mapx;
        this.mapy = mapy;
        this.link = link;
    }
}