package ru.ulstu.referencebot.service;

import org.springframework.stereotype.Service;
import ru.ulstu.referencebot.repository.FaqRepository;
import ru.ulstu.referencebot.repository.KnowledgeRepository;

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
            return new Reply(entry.getIntent(), entry.getAnswer(), entry.isDemo());
        }
        String intent = intents.classify(text);
        String message = switch (intent) {
            case "HELP" -> "Темы: поступление, расписание, контакты, общежитие, стипендия. "
                    + "Для поиска: /contacts ФИСТ, /group ДЕМО-1, /schedule 1.";
            case "UNKNOWN" -> "Не удалось понять вопрос. Напишите /help, чтобы увидеть доступные темы.";
            case "AMBIGUOUS" -> "В вопросе несколько тем. Задайте, пожалуйста, один вопрос за раз.";
            default -> null;
        };
        if (message != null) {
            return new Reply(intent, message, false);
        }
        return repository.findById(intent)
                .map(entry -> new Reply(intent, entry.getAnswer(), entry.isDemo()))
                .orElse(new Reply(intent, "Для этой темы пока нет ответа в базе знаний.", false));
    }
}
