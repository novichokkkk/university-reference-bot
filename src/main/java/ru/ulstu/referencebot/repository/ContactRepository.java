package ru.ulstu.referencebot.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.ulstu.referencebot.entity.DepartmentContact;

public interface ContactRepository extends JpaRepository<DepartmentContact, Long> {
    List<DepartmentContact> findByNameContainingIgnoreCase(String name);
}
