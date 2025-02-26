package hicc.club_fair_2025.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 서브 카테고리(검색어) Entity
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
	private String query;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "category_id")
	@JsonIgnore
	private Category category;

	public SearchQuery(String query, Category category) {
		this.query = query;
		this.category = category;
	}
}