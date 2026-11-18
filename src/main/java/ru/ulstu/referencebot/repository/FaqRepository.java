package ru.ulstu.referencebot.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.ulstu.referencebot.entity.FaqEntry;

public interface FaqRepository extends JpaRepository<FaqEntry, Long> {
    Optional<FaqEntry> findByQuestionIgnoreCase(String question);
}
