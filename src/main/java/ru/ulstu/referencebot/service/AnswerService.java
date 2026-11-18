package ru.ulstu.referencebot.service;

import org.springframework.stereotype.Service;
import ru.ulstu.referencebot.repository.KnowledgeRepository;
import ru.ulstu.referencebot.repository.FaqRepository;

@Service
public class AnswerService {
    private final IntentService intents;
    private final KnowledgeRepository repository;
    private final FaqRepository faq;

    public AnswerService(IntentService intents, KnowledgeRepository repository, FaqRepository faq) {
        this.intents = intents;
        this.repository = repository;
        this.faq = faq;
    }

    public record Reply(String intent, String answer, boolean demo) { }

    public Reply answer(String text) {
        if (text == null || text.isBlank() || text.length() > 1000) {
            throw new IllegalArgumentException("Введите текст длиной от 1 до 1000 символов.");
        }
        var exact = faq.findByQuestionIgnoreCase(text.trim());
        if (exact.isPresent()) {
            var entry = exact.get();
            return new Reply(entry.getIntent(), entry.getAnswer(), true);
        }
        String intent = intents.classify(text);
        String message = switch (intent) {
            case "HELP" -> "Учебный прототип. Темы: поступление, расписание, контакты, общежитие, стипендия.";
            case "UNKNOWN" -> "Не удалось понять вопрос. Напишите /help, чтобы увидеть доступные темы.";
            case "AMBIGUOUS" -> "В вопросе несколько тем. Задайте, пожалуйста, один вопрос за раз.";
            default -> repository.findById(intent).map(entry -> entry.getAnswer())
                    .orElse("Для этой темы пока нет ответа в базе знаний.");
        };
        return new Reply(intent, message, true);
    }
}
