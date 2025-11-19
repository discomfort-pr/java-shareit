package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findByOwnerId(Long userId);

    @Query(
            "SELECT i FROM Item as i WHERE (i.name ILIKE CONCAT('%', ?1, '%')" +
            "OR i.description ILIKE CONCAT('%', ?1, '%')) AND i.available = true"
    )
    List<Item> findByText(String text);
}