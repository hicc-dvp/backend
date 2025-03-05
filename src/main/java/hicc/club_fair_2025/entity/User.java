package hicc.club_fair_2025.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 사용자 Entity
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String introduction;

	@Column(nullable = false, unique = true)
	private String instagramId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "searchQuery_id")
	private SearchQuery searchQuery;

	public User(String instagramId, String introduction, SearchQuery searchQuery) {
		this.instagramId = instagramId;
		this.introduction = introduction;
		this.searchQuery = searchQuery;

	}
}