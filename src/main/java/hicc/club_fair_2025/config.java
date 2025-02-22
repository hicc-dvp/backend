package hicc.club_fair_2025;

import hicc.club_fair_2025.repository.DiningRepository;
import hicc.club_fair_2025.repository.JpaDiningRepository;
import hicc.club_fair_2025.service.DiningService;
import jakarta.persistence.EntityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class config {

    private final EntityManager em;

    public config(EntityManager em) {
        this.em = em;
    }

    @Bean
    public DiningService diningService() {
        return new DiningService(diningRepository());
    }

    @Bean
    public DiningRepository diningRepository() {
        return new JpaDiningRepository(em);
    }
}
