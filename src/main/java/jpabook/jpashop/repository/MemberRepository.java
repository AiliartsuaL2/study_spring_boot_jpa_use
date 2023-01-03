package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository // 스프링 빈에 등록해줌
@RequiredArgsConstructor// final이 붙은 모든 필드에 생성자를 만들어줌
public class MemberRepository {

    private final EntityManager em; //생성자 인젝션,, 스프링 Data JPA가 지원해줌, 원래는 필드 인젝션시 @Autowired는 안되고, @PersistenceContext만 됨,

    /* 직접 엔티티 매니저 팩토리 생성하고싶으면 이렇게,, 거의 안씀
    @PersistenceUnit
    private EntityManagerFactory emf
     */

    public void save(Member member){ // 저장 , insert
        em.persist(member);
    }

    public Member findOne(Long id){ // 단 건 조회
        return em.find(Member.class,id);
    }

    public List<Member> findAll(){ // JPQL 사용함, FROM 대상이 테이블아 이난, 엔티티가 된다.
        return em.createQuery("select m from Member m",Member.class)
                .getResultList(); //JPA 리스트 출력 시 JPQL 사용해서 꺼내와야함
    }

    public List<Member> findByName(String name){ // 파리마터 바인딩하여 특정 이름으로 조회
        return em.createQuery("select m from Member m where m.name = :name",Member.class)
                .setParameter("name",name)
                .getResultList();
    }

}
