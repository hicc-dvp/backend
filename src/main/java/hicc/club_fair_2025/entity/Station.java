package hicc.club_fair_2025.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 역(Station) Entity
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Station {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true, nullable = false)
	private String name;

	private long mapx;
	private long mapy;

	private String searchQuery;

	public Station(String name, long mapx, long mapy, String searchQuery) {
		this.name = name;
		this.mapx = mapx;
		this.mapy = mapy;
		this.searchQuery = searchQuery;
	}
}