package entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
public class Book {
    @Id
    @GeneratedValue
    private long id;

    private String title;

    public Book(String title) {
        this.title = title;
    }
}
