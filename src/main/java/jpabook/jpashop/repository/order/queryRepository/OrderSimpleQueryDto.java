package jpabook.jpashop.repository.order.queryRepository;


import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderSimpleQueryDto {
    private Long orderId; // DTO 이기 때문에 json 스펙 변경 가능,
    private String name;
    private LocalDateTime orderDate;
    private OrderStatus orderStatus;
    private Address address; // Value Object (타입 정의)

    public OrderSimpleQueryDto(Long orderId, String name, LocalDateTime orderDate , OrderStatus orderStatus, Address address) { // 생성자 만들어주는 메서드, 파라미터로 VO 받는건 괜찮다.
        this.orderId = orderId;
        this.name = name; // LAZY 초기화 (영속성 컨텍스트 찾아보고 없으면 DB에서 가져오는거)
        this.orderDate = orderDate;
        this.orderStatus = orderStatus;
        this.address = address; // LAZY 초기화 (영속성 컨텍스트 찾아보고 없으면 DB에서 가져오는거)
    }
}