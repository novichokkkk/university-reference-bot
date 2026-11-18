package ru.ulstu.referencebot.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "faq_entry")
public class FaqEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String intent;
    private String question;
    private String answer;
    private boolean demo;

    protected FaqEntry() { }

    public FaqEntry(String intent, String question, String answer) {
        this.intent = intent;
        this.question = question;
        this.answer = answer;
    }

    public Long getId() { return id; }
    public String getIntent() { return intent; }
    public String getQuestion() { return question; }
    public String getAnswer() { return answer; }
    public boolean isDemo() { return demo; }
    public void changeAnswer(String value) { this.answer = value; }
}
