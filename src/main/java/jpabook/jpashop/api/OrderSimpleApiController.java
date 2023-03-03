package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.order.Order;
import jpabook.jpashop.domain.order.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * xToOne(Many to one, one to one)
 * 주문
 * 주문 -> 회원
 * 주문 -> 배송
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;

    /**
     * 엔티티를 외부로 직접 노출: 순환참조로 인한 stackOverFlow 발생, n + 1 문제 발생
     * ({@link com.fasterxml.jackson.annotation.JsonIgnore}를 이용하여 순환참조는 회피 가능)
     */
    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByCriteria(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName(); // 프로퍼티를 조회하기 위해서 Lazy Loading 수행함.
            order.getDelivery().getAddress(); // 프로퍼티를 조회하기 위해서 Lazy Loading 수행함.
        }
        return all;
    }

    // 엔티티를 dto로 변환: 필요한 정보만 내보내는 API 스펙에 맞춘 컨트롤러 // n + 1 문제는 여전히 남아있음.
    // LAZY loading은 바로 DB를 찌르는것이 아니고 영속성 컨텍스트를 먼저 찔러서 영속성 컨텍스트에 해당 엔티티가 없는 경우
    // DB에 접근하여 Entity를 영속성 컨텍스트에 캐싱한 후에 데이터를 가져옴.
    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDto> ordersV2() {
        return orderRepository.findAllByCriteria(new OrderSearch()).stream()
                .map(SimpleOrderDto::new)
                .collect(Collectors.toList());
    }

    // v2에서 fetch join 이용, 재사용성 높음
    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> ordersV3() {
        return orderRepository.findAllWithMemberDelivery().stream()
                .map(SimpleOrderDto::new)
                .collect(Collectors.toList());
    }


    @Data
    static private class SimpleOrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        public SimpleOrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
        }
    }
}
