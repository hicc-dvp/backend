package hicc.club_fair_2025.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 서브 카테고리(검색어) 엔티티
 * 예: "홍대 한식 제육", "홍대 한식 국밥" 등
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class SearchQuery {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private String query;  // 실제 검색어

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "category_id")
	@JsonIgnore
	private Category category;  // 상위 카테고리 참조

	public SearchQuery(String query, Category category) {
		this.query = query;
		this.category = category;
	}
}