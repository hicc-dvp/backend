package hicc.club_fair_2025.repository;

import hicc.club_fair_2025.domain.Dining;
import jakarta.persistence.EntityManager;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class JpaDiningRepository implements DiningRepository {

    private final EntityManager em;

    public JpaDiningRepository(EntityManager em) {
        this.em = em;
    }

    @Transactional
    public Dining save(Dining dining) {
        em.persist(dining);
        return dining;
    }

    @Transactional
    public void saveAll(List<Dining> diningList) {
        for (Dining dining : diningList) {
            save(dining);
        }
    }
}
