package jpabook.jpashop.service;

import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.ItemRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class ItemServiceTest {

    @Autowired
    ItemService itemService;
    @Autowired
    ItemRepository itemRepository;

    @Test
    public void 상품등록() throws Exception {
        //given
        Item item = new Item();
        item.setName("item1");

        //when
        itemService.saveItem(item);

        //then
        Assert.assertEquals(item, itemRepository.findOne(item.getId()));
    }

    @Test
    public void 상품_전체조회() throws Exception {
        //given
        Item item1 = new Item();
        item1.setName("item1");
        Item item2 = new Item();
        item2.setName("item2");

        //when
        itemService.saveItem(item1);
        itemService.saveItem(item2);

        //then
        List<Item> list = itemService.findAll();

        Assert.assertEquals(list.size(), 2);
    }

        @Test
        public void 특정_상품_조회() throws Exception {
            //given
            Item item1 = new Item();
            item1.setName("item1");
            Item item2 = new Item();
            item2.setName("item2");

            //when
            itemService.saveItem(item1);
            itemService.saveItem(item2);

            //then
            Item finded = itemService.findOne(item1.getId());

            Assert.assertEquals(finded.getName(), item1.getName());
        }
}