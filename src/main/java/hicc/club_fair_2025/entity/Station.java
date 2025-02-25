package hicc.club_fair_2025.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Station 엔티티
 *
 * 역의 상세 정보(역 이름, 좌표, 그리고 해당 역과 매핑된 검색어 등)를 저장합니다.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Station {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// 역 이름 (예: "홍대입구역", "상수역") - 고유 제약 추가
	@Column(unique = true, nullable = false)
	private String name;

	// 역의 좌표 정보 (네이버 API에서 제공)
	private long mapx;
	private long mapy;

	// 해당 역과 관련된 검색어 정보 (예: "홍대입구역"으로 호출 시 사용한 검색어)
	private String searchQuery;

	public Station(String name, long mapx, long mapy, String searchQuery) {
		this.name = name;
		this.mapx = mapx;
		this.mapy = mapy;
		this.searchQuery = searchQuery;
	}
}