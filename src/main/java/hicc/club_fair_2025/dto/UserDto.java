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
    private String introduction;
    private Long restaurantId;

    public UserDto(User user) {
        this.instagramId = user.getInstagramId();
        this.introduction = user.getIntroduction();
        this.restaurantId = user.getRestaurant().getId();
    }
}
