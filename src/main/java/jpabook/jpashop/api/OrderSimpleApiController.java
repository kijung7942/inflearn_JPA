package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.order.Order;
import jpabook.jpashop.domain.order.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryRepository;
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
    private final OrderSimpleQueryRepository orderSimpleQueryRepository;

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

    /* v3에서 필요한 데이터만 가져오도록 변경(Repository의 jpql에 custom dto를 이용함)
       원하는 데이터만 가져오기 때문에 DB통신과의 네트워크 비용을 아낄 수 있지만
       api 스펙에 따른 repository 변경이 있기 때문에 보통의 경우 v3를 더 추천

       하지만 성능 비교에 의해 v4방법을 사용하는 것도 고민해보긴 해야함.
         => 어쩔수 없이 사용해야 한다면 repository를 별개로 뺌(유지보수를 위해)
    */
    @GetMapping("/api/v4/simple-orders")
    public List<OrderSimpleQueryDto> ordersV4() {
        return orderSimpleQueryRepository.findOrderDtos();
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
