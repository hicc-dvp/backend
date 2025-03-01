package hicc.club_fair_2025.dto;

import hicc.club_fair_2025.entity.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserDto {
    private String instagramId;
    private Long restaurantId;
    private String introduction;

    public UserDto(User user) {
        this.instagramId = user.getInstagramId();
        this.restaurantId = user.getRestaurant().getId();  // restaurant 객체 대신 ID만 전달
        this.introduction = user.getIntroduction();
    }
}
