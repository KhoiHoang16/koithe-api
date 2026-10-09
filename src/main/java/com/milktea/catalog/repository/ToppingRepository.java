package com.milktea.catalog.repository;

import com.milktea.catalog.entity.Topping;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ToppingRepository extends JpaRepository<Topping, Long> {
    java.util.Optional<Topping> findByIdAndDeletedAtIsNull(Long id);

    List<Topping> findAllByDeletedAtIsNullOrderByTenToppingAsc();
}
