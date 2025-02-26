package hicc.club_fair_2025.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * User 엔티티 - 사용자가 선택한 Instagram ID, Station, SearchQuery 정보를 저장합니다.
 */
@Entity
@Table(name = "users")  // 'user'는 예약어이므로 'users'로 지정
@Getter
@Setter
@NoArgsConstructor
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// 사용자의 Instagram ID
	@Column(nullable = false, unique = true)
	private String instagramId;

	// 사용자가 선택한 역 정보 (예: "홍대입구역" 또는 "상수역")
	private String station;

	// 사용자가 선택한 검색어 (예: "제육", "백반" 등)
	private String searchQuery;

	public User(String instagramId, String station, String searchQuery) {
		this.instagramId = instagramId;
		this.station = station;
		this.searchQuery = searchQuery;
	}
}