package ru.ulstu.referencebot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ulstu.referencebot.entity.StudentProfile;

public interface ProfileRepository extends JpaRepository<StudentProfile, Long> { }
