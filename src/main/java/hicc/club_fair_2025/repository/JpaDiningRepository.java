package hicc.club_fair_2025.repository;

import hicc.club_fair_2025.domain.Dining;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
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

    public List<String> findAllTitles() {
        String jpql = "SELECT d.title FROM Dining d";
        TypedQuery<String> query = em.createQuery(jpql, String.class);
        return query.getResultList(); // 결과를 List<String>으로 반환
    }
}
