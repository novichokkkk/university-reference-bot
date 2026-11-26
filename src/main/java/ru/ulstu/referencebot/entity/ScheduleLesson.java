package ru.ulstu.referencebot.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "schedule_lesson")
public class ScheduleLesson {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String groupCode;
    private int dayOfWeek;
    private String startsAt;
    private String subject;
    private String room;
    private boolean demo;

    public Long getId() { return id; }
    public String getGroupCode() { return groupCode; }
    public int getDayOfWeek() { return dayOfWeek; }
    public String getStartsAt() { return startsAt; }
    public String getSubject() { return subject; }
    public String getRoom() { return room; }
    public boolean isDemo() { return demo; }
}
