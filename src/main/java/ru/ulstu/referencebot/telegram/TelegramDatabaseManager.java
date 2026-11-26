package ru.ulstu.referencebot.telegram;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.ulstu.referencebot.entity.FaqEntry;
import ru.ulstu.referencebot.repository.FaqRepository;
import ru.ulstu.referencebot.repository.KnowledgeRepository;

@Service
public class TelegramDatabaseManager {
    private static final int PAGE_SIZE = 5;
    private final long ownerId;
    private final FaqRepository faq;
    private final KnowledgeRepository knowledge;
    private final ConcurrentMap<Long, Pending> pending = new ConcurrentHashMap<>();

    private enum Step { TOPIC_ANSWER, FAQ_QUESTION, FAQ_ANSWER, FAQ_EDIT_ANSWER }

    private record Pending(Step step, String intent, Long faqId, String question) { }

    public record Reply(String text, Map<String, Object> markup) { }

    public TelegramDatabaseManager(@Value("${TELEGRAM_ADMIN_ID:0}") long ownerId,
            FaqRepository faq, KnowledgeRepository knowledge) {
        this.ownerId = ownerId;
        this.faq = faq;
        this.knowledge = knowledge;
    }

    public boolean isOwner(long userId) {
        return ownerId > 0 && userId == ownerId;
    }

    public boolean hasPending(long userId) {
        return isOwner(userId) && pending.containsKey(userId);
    }

    public void cancel(long userId) {
        pending.remove(userId);
    }

    public Reply home(long userId) {
        if (!isOwner(userId)) {
            return denied();
        }
        return new Reply("Управление базой\nВыбери, что изменить. Основные ответы используются для "
                + "распознанных тем, а частые вопросы — для точного совпадения текста вопроса.",
                inline(List.of(List.of(button("Основные ответы", "admin:topics")),
                        List.of(button("Частые вопросы", "admin:faq-list:0")),
                        List.of(button("➕ Добавить вопрос", "admin:faq-add")))));
    }

    public Reply onCallback(long userId, String data) {
        if (!isOwner(userId)) {
            return denied();
        }
        if (data == null || !data.startsWith("admin:")) {
            return home(userId);
        }
        cancel(userId);
        if (data.equals("admin:home")) {
            return home(userId);
        }
        if (data.equals("admin:topics")) {
            return topics();
        }
        if (data.startsWith("admin:topic-edit:")) {
            String intent = data.substring("admin:topic-edit:".length());
            if (!knowledge.existsById(intent)) {
                return topics();
            }
            pending.put(userId, new Pending(Step.TOPIC_ANSWER, intent, null, null));
            return plain("Отправь новый текст ответа для темы " + intent + ". Для отмены — /cancel.");
        }
        if (data.startsWith("admin:topic:")) {
            return topic(data.substring("admin:topic:".length()));
        }
        if (data.equals("admin:faq-add")) {
            return addTopicMenu();
        }
        if (data.startsWith("admin:faq-add:")) {
            String intent = data.substring("admin:faq-add:".length());
            if (!knowledge.existsById(intent)) {
                return addTopicMenu();
            }
            pending.put(userId, new Pending(Step.FAQ_QUESTION, intent, null, null));
            return plain("Отправь текст нового вопроса одним сообщением. Для отмены — /cancel.");
        }
        if (data.startsWith("admin:faq-list:")) {
            return faqList((int) parseNumber(data.substring("admin:faq-list:".length()), 0));
        }
        if (data.startsWith("admin:faq-edit:")) {
            long id = parseNumber(data.substring("admin:faq-edit:".length()), -1);
            if (!faq.existsById(id)) {
                return plain("Вопрос не найден.");
            }
            pending.put(userId, new Pending(Step.FAQ_EDIT_ANSWER, null, id, null));
            return plain("Отправь новый ответ на вопрос одним сообщением. Для отмены — /cancel.");
        }
        if (data.startsWith("admin:faq-delete-confirm:")) {
            long id = parseNumber(data.substring("admin:faq-delete-confirm:".length()), -1);
            if (!faq.existsById(id)) {
                return plain("Вопрос уже удалён.");
            }
            faq.deleteById(id);
            return new Reply("Вопрос удалён из базы.",
                    inline(List.of(List.of(button("К списку вопросов", "admin:faq-list:0")))));
        }
        if (data.startsWith("admin:faq-delete:")) {
            long id = parseNumber(data.substring("admin:faq-delete:".length()), -1);
            if (!faq.existsById(id)) {
                return plain("Вопрос не найден.");
            }
            return new Reply("Удалить этот вопрос и его ответ?", inline(List.of(
                    List.of(button("Да, удалить", "admin:faq-delete-confirm:" + id)),
                    List.of(button("Отмена", "admin:faq:" + id)))));
        }
        if (data.startsWith("admin:faq:")) {
            return faqDetail(parseNumber(data.substring("admin:faq:".length()), -1));
        }
        return home(userId);
    }

    public Reply onText(long userId, String text) {
        if (!isOwner(userId)) {
            return denied();
        }
        Pending action = pending.get(userId);
        if (action == null) {
            return home(userId);
        }
        String value = text == null ? "" : text.trim();
        if (value.isBlank() || value.length() > 2000) {
            return plain("Нужен текст длиной от 1 до 2000 символов. Попробуй ещё раз или отправь /cancel.");
        }
        if (action.step() == Step.FAQ_QUESTION) {
            if (value.length() > 250) {
                return plain("Вопрос должен быть не длиннее 250 символов.");
            }
            if (faq.findByQuestionIgnoreCase(value).isPresent()) {
                return plain("Такой вопрос уже есть в базе. Введи другой или отправь /cancel.");
            }
            pending.put(userId, new Pending(Step.FAQ_ANSWER, action.intent(), null, value));
            return plain("Теперь отправь ответ на вопрос «" + value + "». Можно добавить ссылку на источник.");
        }
        pending.remove(userId);
        if (action.step() == Step.FAQ_ANSWER) {
            FaqEntry entry = faq.save(new FaqEntry(action.intent(), action.question(), value));
            return faqDetail(entry.getId());
        }
        if (action.step() == Step.FAQ_EDIT_ANSWER) {
            FaqEntry entry = faq.findById(action.faqId()).orElse(null);
            if (entry == null) {
                return plain("Вопрос уже удалён.");
            }
            entry.changeAnswer(value);
            faq.save(entry);
            return faqDetail(entry.getId());
        }
        var entry = knowledge.findById(action.intent()).orElse(null);
        if (entry == null) {
            return plain("Тема не найдена.");
        }
        entry.changeAnswer(value);
        knowledge.save(entry);
        return topic(entry.getIntent());
    }

    private Reply topics() {
        List<List<Map<String, String>>> rows = new ArrayList<>();
        knowledge.findAll(Sort.by("intent")).forEach(entry -> rows.add(List.of(
                button(entry.getCategory(), "admin:topic:" + entry.getIntent()))));
        rows.add(List.of(button("← Управление базой", "admin:home")));
        return new Reply("Основные ответы по темам:", inline(rows));
    }

    private Reply topic(String intent) {
        var entry = knowledge.findById(intent).orElse(null);
        if (entry == null) {
            return plain("Тема не найдена.");
        }
        return new Reply(entry.getCategory() + " (" + intent + ")\n\n" + entry.getAnswer(), inline(List.of(
                List.of(button("✏️ Изменить ответ", "admin:topic-edit:" + intent)),
                List.of(button("← К темам", "admin:topics")))));
    }

    private Reply faqList(int page) {
        List<FaqEntry> entries = faq.findAll(Sort.by("id"));
        int pages = Math.max(1, (entries.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        int current = Math.min(Math.max(page, 0), pages - 1);
        List<List<Map<String, String>>> rows = new ArrayList<>();
        int start = current * PAGE_SIZE;
        for (int i = start; i < Math.min(start + PAGE_SIZE, entries.size()); i++) {
            FaqEntry entry = entries.get(i);
            rows.add(List.of(button(entry.getQuestion(), "admin:faq:" + entry.getId())));
        }
        if (current > 0) {
            rows.add(List.of(button("← Назад", "admin:faq-list:" + (current - 1))));
        }
        if (current + 1 < pages) {
            rows.add(List.of(button("Дальше →", "admin:faq-list:" + (current + 1))));
        }
        rows.add(List.of(button("➕ Добавить вопрос", "admin:faq-add")));
        rows.add(List.of(button("← Управление базой", "admin:home")));
        return new Reply("Частые вопросы: " + entries.size() + ". Страница " + (current + 1) + " из " + pages + ".",
                inline(rows));
    }

    private Reply faqDetail(long id) {
        var entry = faq.findById(id).orElse(null);
        if (entry == null) {
            return plain("Вопрос не найден.");
        }
        return new Reply("Вопрос: " + entry.getQuestion() + "\nОтвет: " + entry.getAnswer(), inline(List.of(
                List.of(button("✏️ Изменить ответ", "admin:faq-edit:" + id)),
                List.of(button("🗑 Удалить", "admin:faq-delete:" + id)),
                List.of(button("← К списку", "admin:faq-list:0")))));
    }

    private Reply addTopicMenu() {
        List<List<Map<String, String>>> rows = new ArrayList<>();
        knowledge.findAll(Sort.by("intent")).forEach(entry -> rows.add(List.of(
                button(entry.getCategory(), "admin:faq-add:" + entry.getIntent()))));
        rows.add(List.of(button("← Управление базой", "admin:home")));
        return new Reply("К какой теме относится новый вопрос?", inline(rows));
    }

    private long parseNumber(String text, long fallback) {
        try {
            return Long.parseLong(text);
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private Reply plain(String text) {
        return new Reply(text, inline(List.of(List.of(button("← Управление базой", "admin:home")))));
    }

    private Reply denied() {
        return new Reply("Раздел недоступен.", Map.of());
    }

    private Map<String, Object> inline(List<List<Map<String, String>>> rows) {
        return Map.of("inline_keyboard", rows);
    }

    private Map<String, String> button(String text, String data) {
        return Map.of("text", text, "callback_data", data);
    }
}
