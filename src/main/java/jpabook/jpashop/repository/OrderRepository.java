package jpabook.jpashop.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

import static jpabook.jpashop.domain.QMember.member;
import static jpabook.jpashop.domain.QOrder.order;

@Repository
@RequiredArgsConstructor
public class OrderRepository {
    private final EntityManager em;
    public void save(Order order){
        em.persist(order);
    }

    public Order findOne(Long id){
        return em.find(Order.class,id);
    }

    public List<Order> findAllByString(OrderSearch orderSearch) {
        //language=JPAQL
        String jpql = "select o From Order o join o.member m";
        boolean isFirstCondition = true;
//주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " o.status = :status";
        }
//회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " m.name like :name";
        }
        TypedQuery<Order> query = em.createQuery(jpql, Order.class) .setMaxResults(1000); //최대 1000건
        if (orderSearch.getOrderStatus() != null) {
            query = query.setParameter("status", orderSearch.getOrderStatus());
        }
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            query = query.setParameter("name", orderSearch.getMemberName());
        }
        return query.getResultList();
    }

    public List<Order> findAllWithMemberDelivery() {
        return em.createQuery( // 레이지를 다 무시하고 프록시를 다 채워서 값을 한번에 뽑아옴 (fetchjoin 방법)
                "select o from Order o" +
                        " join fetch o.member m" +
                        " join fetch o.delivery d",Order.class
        ).getResultList();
    }

    public List<Order> findAllWithItem() {
        return em.createQuery(
                "select distinct o from Order o" + // distinct 키워드는 중복을 제거시킴 db distinct는 로우 전체가 똑같아야 중복이 제거된다.
                                                // (db distinct + 설정된 엔티티의 중복을 걸러서 담아준다. )
                        " join fetch o.member m" +
                        " join fetch o.delivery d" +
                        " join fetch o.orderItems oi" + //orderItem과 같이 조인되기때문에 orederItem 만큼 뻥튀기됨
                        " join fetch oi.item i",Order.class)
                .getResultList();
    }

    public List<Order> findAllWithMemberDelivery(int offset, int limit) {
        return em.createQuery( // 레이지를 다 무시하고 프록시를 다 채워서 값을 한번에 뽑아옴 (fetchjoin 방법)
                "select o from Order o" +
                        " join fetch o.member m" +
                        " join fetch o.delivery d",Order.class)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    // QueryDSL
    public List<Order> findAll(OrderSearch orderSearch){
        JPAQueryFactory query = new JPAQueryFactory(em);

        return query
                .select(order)
                .from(order)
                .join(order.member, member) //order의 member를 member로 alias줌
                .where(statusEq(orderSearch.getOrderStatus()), nameLike(orderSearch.getMemberName()))
                .limit(1000)
                .fetch();
    }

    private static BooleanExpression nameLike(String memberName) {
        if(!StringUtils.hasText(memberName)){
            return null;
        }
        return member.name.like(memberName);
    }

    private BooleanExpression statusEq(OrderStatus statusCond){
        if(statusCond == null){
            return null;
        }
        return order.status.eq(statusCond);
    }

/*
// QueryDSL
    public List<Order> findALl(OrderSearch orderSearch){
        QOrder order = Qorder.order;
        QMember member = QMember.member;

        return query
                .select(order)
                .from(order)
                .join(order.member, member)
                .where(statusEq(orderSEarch.getOrderStatus()),
                        nameLike(orderSearch.getMemberName()))
                .limit(1000)
                .fetch();
}
    private BooleanExpression statusEq(OrderStatus statusCond){
        if(statusCond == null){
            return null;
        }
        return order.status.eq(statusCond);
    }

    private BooleanExpression nameLike(String nameCond){
        if(!StringUtils.hasText(nameCond)){
            return null;
        }
        return member.name.link(nameCond);
    }
*/
}