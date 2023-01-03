package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.queryRepository.OrderSimpleQueryDto;
import jpabook.jpashop.repository.order.queryRepository.OrderSimpleQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * XtoOne (manyToOne, OneToOne)
 * Order
 * Order -> Member // manyToOne
 * Order -> Delivery// oneToOne
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {
    private final OrderRepository orderRepository;
    private final OrderSimpleQueryRepository orderSimpleQueryRepository;

    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1(){ //엔티티 직접노출 (리턴 타입이 엔티티)
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for(Order order : all){
            order.getMember().getName(); // order.getMember 까지는 프록시 객체인데,, getName하는 순간 Lazy가 강제 초기화가 됨
            order.getMember().getAddress(); // order.getMember 까지는 프록시 객체인데,, getAddress하는 순간 Lazy가 강제 초기화가 됨
        }
        return all; // 양방향 연관관계에 있기 때문에 무한루프에 빠짐, 양방향 연관관계에 있는 둘 중 하나는 JsonIgnore 처리해줘야한다.
    }
    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDto> orderV2(){
        // Order 2개 조회 됨
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());

        // 루프가 2번 돌음,
        return orders.stream()
                .map(o-> new SimpleOrderDto(o))
                .collect(Collectors.toList());
    }
    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> orderV3(){
        // Order 2개 조회 됨
        List<Order> orders = orderRepository.findAllWithMemberDelivery();
        List<SimpleOrderDto> result = orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());
        return result;
    }
    @GetMapping("/api/v4/simple-orders")
    public List<OrderSimpleQueryDto> ordersV4(){
        return orderSimpleQueryRepository.findOrderDtos();
    }

    @Data
    static class SimpleOrderDto{
        private Long orderId; // DTO 이기 때문에 json 스펙 변경 가능,
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address; // Value Object (타입 정의)

        public SimpleOrderDto(Order order) { // 생성자 만들어주는 메서드, 파라미터로 VO 받는건 괜찮다.
            orderId = order.getId();
            name = order.getMember().getName(); // LAZY 초기화 (영속성 컨텍스트 찾아보고 없으면 DB에서 가져오는거)
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress(); // LAZY 초기화 (영속성 컨텍스트 찾아보고 없으면 DB에서 가져오는거)
        }
    }

}
