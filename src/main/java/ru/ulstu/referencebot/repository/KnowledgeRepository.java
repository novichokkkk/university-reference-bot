package ru.ulstu.referencebot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ulstu.referencebot.entity.KnowledgeEntry;

public interface KnowledgeRepository extends JpaRepository<KnowledgeEntry, String> { }
