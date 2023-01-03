package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.item.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ItemRepository {
    private final EntityManager em;

    public void save(Item item){
        if(item.getId() == null){ // jpa 저장 전까지 아이디 값이 없음,, 새로 생성하는 객체이기 때문에 신규로 등록해버림,
            em.persist(item);
        }
        else{
            em.merge(item); // 이미 id값이 있음,, >> 업데이트
        }
    }

    public Item findOne(Long id){
        return em.find(Item.class, id);
    }

    public List<Item> findAll(){
        return em.createQuery("select i from Item i", Item.class)
                .getResultList();
    }

}
