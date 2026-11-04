package ru.ulstu.referencebot.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "prototype_knowledge")
public class KnowledgeEntry {
    @Id
    private String intent;
    @Column(nullable = false)
    private String category;
    @Column(nullable = false, length = 2000)
    private String answer;

    protected KnowledgeEntry() { }

    public KnowledgeEntry(String intent, String category, String answer) {
        this.intent = intent;
        this.category = category;
        this.answer = answer;
    }

    public String getIntent() { return intent; }
    public String getCategory() { return category; }
    public String getAnswer() { return answer; }
}
