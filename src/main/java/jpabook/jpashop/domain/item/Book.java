package jpabook.jpashop.domain.item;

import jpabook.jpashop.dto.BookFormDto;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@Getter
@Setter
@DiscriminatorValue("B")
public class Book extends Item {
    private String author;
    private String isbn;

    public static Book createBookFromDto(BookFormDto formDto) {
        Book book = new Book();
        book.setName(formDto.getName());
        book.setPrice(formDto.getPrice());
        book.setAuthor(formDto.getAuthor());
        book.setIsbn(formDto.getIsbn());
        book.setStockQuantity(formDto.getStockQuantity());
        return book;
    }
}
