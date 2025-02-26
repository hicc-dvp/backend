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

	@Column(nullable = false, unique = true)
	private String instagramId;

	private String station;
	private String searchQuery;

	public User(String instagramId, String station, String searchQuery) {
		this.instagramId = instagramId;
		this.station = station;
		this.searchQuery = searchQuery;
	}
}