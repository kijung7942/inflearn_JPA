package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.order.Order;
import jpabook.jpashop.domain.order.OrderItem;
import jpabook.jpashop.domain.order.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.query.OrderFlatDto;
import jpabook.jpashop.repository.order.query.OrderItemQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.*;

/**
 * OneToMany 관계(Collection)를 조회하는 Api
 */
@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;

    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByCriteria(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName();
            order.getDelivery().getAddress();
            List<OrderItem> orderItems = order.getOrderItems();
            orderItems.forEach(orderItem -> orderItem.getItem().getName());
        }
        return all;
    }

    @GetMapping("/api/v2/orders")
    public List<OrderDto> ordersV2() {
        List<Order> orders = orderRepository.findAllByCriteria(new OrderSearch());
        return orders.stream()
                .map(OrderDto::new)
                .collect(toList());
    }

    /**
     * fetch join을 이용한 OneToMany 조회
     * 제약조건 1. fetch join 하는 순간 페이징이 불가해짐
     * -> 한번에 다 조회하고 어플리케이션 내에서 sort(paging) 처리를 하기 때문.
     * 제약조건 2. collection을 위한 fetch join은 collection 1개에 대해서만 가능함.
     * -> 돌아가긴 하는데 잘못된 데이터가 나올 가능성이 큼.
     */
    @GetMapping("/api/v3/orders")
    public List<OrderDto> ordersV3() {
        List<Order> oders = orderRepository.findAllWithItem();
        return oders.stream().map(OrderDto::new).collect(toList());
    }

    @GetMapping("/api/v3.1/orders")
    public List<OrderDto> ordersV3_paging(@RequestParam(defaultValue = "1") int offset,
                                          @RequestParam(defaultValue = "100") int limit) {
        List<Order> oders = orderRepository.findAllWithMemberDelivery(offset, limit);
        return oders.stream().map(OrderDto::new).collect(toList());
    }

    @GetMapping("/api/v4/orders")
    public List<OrderQueryDto> ordersV4() {
        return orderQueryRepository.findOrderQueryDtos();
    }

    @GetMapping("/api/v5/orders")
    public List<OrderQueryDto> ordersV5() {
        return orderQueryRepository.findAllByDto_optimization();
    }

    @GetMapping("/api/v6/orders")
    public List<OrderQueryDto> ordersV6() {
        List<OrderFlatDto> flats = orderQueryRepository.findAllByDto_flat();

        return flats.stream()
                .collect(groupingBy(o -> new OrderQueryDto(o.getOrderID(), o.getName(),
                                o.getOrderDate(), o.getOrderStatus(), o.getAddress()),
                        mapping(o -> new OrderItemQueryDto(o.getOrderID(), o.getItemName(), o.getCount(), o.getOrderPrice()), toList()))
                ).keySet().stream()
                .map(orderItemQueryDtos -> new OrderQueryDto(orderItemQueryDtos.getOrderID(), orderItemQueryDtos.getName(), orderItemQueryDtos.getOrderDate(), orderItemQueryDtos.getOrderStatus(), orderItemQueryDtos.getAddress()))
                .collect(toList());


    }

    @Data

    private class OrderDto {

        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        //        private List<OrderItem> orderItems;
        private List<OrderItemDto> orderItems;

        public OrderDto(Order order) {
            this.orderId = order.getId();
            this.name = order.getMember().getName();
            this.orderDate = order.getOrderDate();
            this.orderStatus = order.getStatus();
            this.address = order.getDelivery().getAddress();
//            order.getOrderItems().forEach(o -> o.getItem().getName());
//            this.orderItems = order.getOrderItems();
            this.orderItems = order.getOrderItems().stream().map(OrderItemDto::new).collect(toList());
        }
    }

    @Data
    private class OrderItemDto {

        private String itemName; // 상품 명
        private int orderPrice; // 주문 금액
        private int count; // 주문 갯수

        public OrderItemDto(OrderItem orderItem) {
            this.itemName = orderItem.getItem().getName();
            this.orderPrice = orderItem.getOrderPrice();
            this.count = orderItem.getCount();
        }
    }
}
