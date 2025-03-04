package hicc.club_fair_2025.dto;

import lombok.Getter;

@Getter
public class CategoryDto {
	private Long id;
	private String name;

	public CategoryDto(Long id, String name) {
		this.id = id;
		this.name = name;
	}
}