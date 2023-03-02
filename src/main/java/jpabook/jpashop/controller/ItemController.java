package jpabook.jpashop.controller;

import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.dto.BookFormDto;
import jpabook.jpashop.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @GetMapping("/items/new")
    public String createForm(Model model) {
        model.addAttribute("form", new BookFormDto());
        return "items/createItemForm";
    }

    @PostMapping("/items/new")
    public String create(BookFormDto formDto) {
        Book book = Book.createBookFromDto(formDto);
        itemService.saveItem(book);
        return "redirect:/";
    }

    @GetMapping("/items")
    public String list(Model model) {
        model.addAttribute("items", itemService.findAll());
        return "items/itemList";
    }

    @GetMapping("/items/{itemId}/edit")
    public String updateItemForm(@PathVariable Long itemId, Model model) {
        Item book = itemService.findOne(itemId);
        BookFormDto dto = new BookFormDto();

        if (book.getClass() == Book.class) {
            dto.setId(book.getId());
            dto.setName(book.getName());
            dto.setPrice(book.getPrice());
            dto.setStockQuantity(book.getStockQuantity());
            dto.setAuthor(((Book) book).getAuthor());
            dto.setIsbn(((Book) book).getIsbn());
        }

        model.addAttribute("form", dto);
        return "items/updateItemForm";
    }

    @PostMapping("/items/{itemId}/edit")
    public String updateItem(@ModelAttribute BookFormDto form) {
//        Book book = Book.createBookFromDto(form);
//        book.setId(form.getId());
//        itemService.saveItem(book);
        itemService.updateItem(form.getId(), form.getName(), form.getPrice(), form.getStockQuantity());
        return "redirect:/items";
    }
}
