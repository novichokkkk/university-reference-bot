package ru.ulstu.referencebot.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "student_profile")
public class StudentProfile {
    @Id
    private Long telegramId;
    private String groupCode;

    protected StudentProfile() { }

    public StudentProfile(Long telegramId, String groupCode) {
        this.telegramId = telegramId;
        this.groupCode = groupCode;
    }

    public Long getTelegramId() { return telegramId; }
    public String getGroupCode() { return groupCode; }
}
