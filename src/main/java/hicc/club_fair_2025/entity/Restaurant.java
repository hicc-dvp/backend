package hicc.club_fair_2025.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 식당 Entity
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String category;
    private String roadAddress;

    private String searchQuery;

    private double mapx;
    private double mapy;

    private String station;

    public Restaurant(String name, String category, String roadAddress) {
        this.name = name;
        this.category = category;
        this.roadAddress = roadAddress;
    }
}