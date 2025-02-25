package hicc.club_fair_2025.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 대분류 카테고리(Entity) - 예: 한식, 일식, 중식, 양식 등
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Category {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private String name;  // 카테고리명

	/**
	 * 해당 카테고리에 속한 상세 검색어 목록
	 * 예: "홍대 한식 제육", "홍대 한식 국밥" 등
	 */
	@OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<SearchQuery> searchQueries = new ArrayList<>();

	public Category(String name) {
		this.name = name;
	}
}