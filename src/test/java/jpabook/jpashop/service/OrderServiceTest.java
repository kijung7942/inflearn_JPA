package jpabook.jpashop.service;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.exception.NotEnoughStockException;
import jpabook.jpashop.repository.OrderRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@Transactional
@SpringBootTest
public class OrderServiceTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private OrderService orderService;

    @Autowired
    OrderRepository orderRepository;

    @Test
    public void 상품주문() throws Exception {
        //given
        Member member = createMember("회원1", new Address("서울", "강가", "123-123"));
        Item book = createBook("시골 JPA", 10000, 10);

        int orderCount = 2;
        //when
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

        //then
        Order order = orderRepository.findOne(orderId);

        assertEquals("상품 주문시 상태는 ORDER", OrderStatus.ORDER, order.getStatus());
        assertEquals("주문한 상품 종류 수가 정확해야 한다.", 1, order.getOrderItems().size());
        assertEquals("주문 가격은 가격 * 수량이다.", 10000 * orderCount, order.getTotalPrice());
        assertEquals("주문 수량만큼 재고가 줄어야 한다.", 8, book.getStockQuantity());

    }

    private Item createBook(String bookName, int price, int quantity) {
        Item book = new Book();
        book.setName(bookName);
        book.setPrice(price);
        book.setStockQuantity(quantity);
        em.persist(book);
        return book;
    }

    private Member createMember(String name, Address address) {
        Member member = new Member();
        member.setName(name);
        member.setAddress(address);
        em.persist(member);
        return member;
    }

    @Test(expected = NotEnoughStockException.class)
    public void 상품주문_재고수량초과() throws Exception {
        //given
        Member member = createMember("회원1", new Address("서울", "강가", "123-123"));
        Item book = createBook("시골 JPA", 10000, 10);
        int orderCount = 50;
        //when
        orderService.order(member.getId(), book.getId(), orderCount);

        //then
        fail("재고 수량 부족 예외가 발생하여야 한다.");
    }

    @Test
    public void 주문취소() throws Exception {
        //given
        Member memeber = createMember("kim", new Address("서울", "강가", "123-332"));
        Item book = createBook("시골 JPA", 10000, 10);
        int orderCount = 2;
        Long orderId = orderService.order(memeber.getId(), book.getId(), orderCount);

        //when
        orderService.cancelOrder(orderId);

        //then
        Order findedOrder = orderRepository.findOne(orderId);

        assertEquals("주문 취소시 상태는 CANCEL이어야 한다.",findedOrder.getStatus(), OrderStatus.CANCEL);
        assertEquals("주문 취소시 재고는 주문 이전 재고로 돌아가야한다.",10, book.getStockQuantity());

    }

}