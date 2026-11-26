package ru.ulstu.referencebot.telegram;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;
import ru.ulstu.referencebot.entity.DepartmentContact;
import ru.ulstu.referencebot.repository.ContactRepository;
import ru.ulstu.referencebot.service.AnswerService;
import ru.ulstu.referencebot.service.ReferenceService;

@Service
public class TelegramMenuService {
    private static final String SCHEDULE = "📅 Расписание";
    private static final String FACULTIES = "🏛 Факультеты";
    private static final String PROFILE = "👤 Профиль";
    private static final String TOPICS = "📚 Темы";
    private static final String ADMIN = "Управление базой";

    private final AnswerService answers;
    private final ReferenceService reference;
    private final ContactRepository contacts;
    private final TelegramDatabaseManager databaseManager;
    private final Set<Long> waitingForGroup = ConcurrentHashMap.newKeySet();

    public TelegramMenuService(AnswerService answers, ReferenceService reference, ContactRepository contacts,
            TelegramDatabaseManager databaseManager) {
        this.answers = answers;
        this.reference = reference;
        this.contacts = contacts;
        this.databaseManager = databaseManager;
    }

    public record Reply(String text, Map<String, Object> markup) { }

    public Reply onText(long userId, String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return main(userId, "Бот принимает текстовые сообщения. Выберите раздел кнопкой ниже.");
        }
        if (rawText.length() > 2000) {
            return main(userId, "Сообщение слишком длинное. Напишите не более 2000 символов.");
        }
        String text = rawText.trim();
        if (text.equals("/cancel")) {
            waitingForGroup.remove(userId);
            databaseManager.cancel(userId);
            return main(userId, "Действие отменено. Выберите раздел.");
        }
        if (text.equals(ADMIN) || text.equals("/admin")) {
            waitingForGroup.remove(userId);
            return databaseManager.isOwner(userId) ? fromAdmin(databaseManager.home(userId))
                    : main(userId, "Раздел недоступен.");
        }
        if (text.startsWith("/group ")) {
            databaseManager.cancel(userId);
            return saveGroup(userId, text.substring(7).trim());
        }
        if (databaseManager.hasPending(userId) && !text.startsWith("/") && !isMenuButton(text)) {
            return fromAdmin(databaseManager.onText(userId, text));
        }
        databaseManager.cancel(userId);
        if (waitingForGroup.contains(userId) && !text.startsWith("/") && !isMenuButton(text)) {
            return saveGroup(userId, text);
        }
        waitingForGroup.remove(userId);
        if (text.equals("/start") || text.equals("/menu")) {
            return main(userId, "Привет! Выберите раздел кнопкой ниже или напишите вопрос своими словами.");
        }
        if (text.equals("/help") || text.equals("ℹ️ Помощь")) {
            return main(userId, "Кнопки открывают расписание, контакты факультетов, профиль "
                    + "и ответы на частые вопросы. "
                    + "В профиле сохраняется группа для расписания. Можно также написать вопрос текстом. "
                    + "Команды: /group ПИбд-42, /schedule 1, /contacts ФИСТ, /cancel.");
        }
        if (text.equals(SCHEDULE) || text.equals("/schedule")) {
            return scheduleMenu(userId);
        }
        if (text.matches("/schedule [1-7]")) {
            return day(userId, Integer.parseInt(text.substring(10)));
        }
        if (text.equals(FACULTIES) || text.equals("/faculties") || text.equals("/contacts")) {
            return facultyMenu(userId);
        }
        if (text.startsWith("/contacts ")) {
            return main(userId, reference.contactText(text.substring(10)));
        }
        if (text.equals(PROFILE) || text.equals("/profile")) {
            return profile(userId);
        }
        if (text.equals(TOPICS) || text.equals("/topics")) {
            return topicMenu(userId);
        }
        if (text.length() > 1000) {
            return main(userId, "Вопрос должен содержать не более 1000 символов.");
        }
        var answer = answers.answer(text);
        return main(userId, answer.answer());
    }

    public Reply onCallback(long userId, String data) {
        if (data == null) {
            return main(userId, "Кнопка недоступна. Откройте меню заново.");
        }
        waitingForGroup.remove(userId);
        if (data.startsWith("admin:")) {
            return databaseManager.isOwner(userId) ? fromAdmin(databaseManager.onCallback(userId, data))
                    : main(userId, "Раздел недоступен.");
        }
        databaseManager.cancel(userId);
        if (data.equals("profile:group")) {
            waitingForGroup.add(userId);
            return main(userId, "Напишите название своей группы одним сообщением, например ПИбд-42. "
                    + "Для отмены отправьте /cancel.");
        }
        if (data.equals("menu:faculties")) {
            return facultyMenu(userId);
        }
        if (data.equals("menu:profile")) {
            return profile(userId);
        }
        if (data.startsWith("faculty:")) {
            try {
                long id = Long.parseLong(data.substring(8));
                return new Reply(reference.facultyContact(id), inline(List.of(
                        List.of(button("← К факультетам", "menu:faculties")))));
            } catch (IllegalArgumentException exception) {
                return main(userId, "Факультет не найден. Откройте список заново.");
            }
        }
        if (data.matches("day:[1-7]")) {
            return day(userId, Integer.parseInt(data.substring(4)));
        }
        if (data.startsWith("topic:")) {
            String question = switch (data.substring(6)) {
                case "admission" -> "Как поступить?";
                case "dormitory" -> "Как заселиться в общежитие?";
                case "scholarship" -> "Как получить повышенную стипендию?";
                default -> null;
            };
            return question == null ? topicMenu(userId)
                    : new Reply(answers.answer(question).answer(), topicButtons());
        }
        return main(userId, "Кнопка устарела. Откройте меню заново.");
    }

    private Reply saveGroup(long userId, String group) {
        try {
            reference.saveProfile(userId, group);
            waitingForGroup.remove(userId);
            return new Reply("Группа «" + group + "» сохранена в профиле. Выберите день расписания.", dayButtons());
        } catch (IllegalArgumentException exception) {
            return main(userId, exception.getMessage() + " Пример: ПИбд-42. Для отмены — /cancel.");
        }
    }

    private Reply profile(long userId) {
        String group = reference.profileGroup(userId).orElse("не указана");
        return new Reply("Ваш профиль\nГруппа: " + group
                + "\nГруппа используется для поиска расписания. Изменить её можно кнопкой ниже.",
                inline(List.of(List.of(button("✏️ Изменить группу", "profile:group")))));
    }

    private Reply scheduleMenu(long userId) {
        if (reference.profileGroup(userId).isEmpty()) {
            return new Reply("Сначала укажите группу в профиле, чтобы я знал, чьё расписание показать.",
                    inline(List.of(List.of(button("Указать группу", "profile:group")))));
        }
        return new Reply("Группа: " + reference.profileGroup(userId).orElseThrow()
                + ". Выберите день недели.", dayButtons());
    }

    private Reply day(long userId, int day) {
        return new Reply(reference.personalSchedule(userId, day), dayButtons());
    }

    private Reply facultyMenu(long userId) {
        List<DepartmentContact> faculties = contacts.findByFacultyTrueOrderByNameAsc();
        if (faculties.isEmpty()) {
            return main(userId, "Список факультетов пока пуст.");
        }
        List<List<Map<String, String>>> rows = new ArrayList<>();
        for (DepartmentContact faculty : faculties) {
            rows.add(List.of(button(facultyLabel(faculty.getName()), "faculty:" + faculty.getId())));
        }
        return new Reply("Выберите факультет — покажу телефон, почту и официальный источник.", inline(rows));
    }

    private Reply topicMenu(long userId) {
        return new Reply("Выберите тему или задайте вопрос своими словами.", topicButtons());
    }

    private Map<String, Object> topicButtons() {
        return inline(List.of(
                List.of(button("Поступление", "topic:admission")),
                List.of(button("Общежитие", "topic:dormitory")),
                List.of(button("Стипендия", "topic:scholarship"))));
    }

    private Map<String, Object> dayButtons() {
        return inline(List.of(
                List.of(button("Пн", "day:1"), button("Вт", "day:2"), button("Ср", "day:3")),
                List.of(button("Чт", "day:4"), button("Пт", "day:5")),
                List.of(button("Сб", "day:6"), button("Вс", "day:7")),
                List.of(button("👤 Профиль", "menu:profile"))));
    }

    private Reply main(long userId, String text) {
        List<List<String>> rows = new ArrayList<>();
        rows.add(List.of(SCHEDULE, FACULTIES));
        rows.add(List.of(PROFILE, TOPICS));
        if (databaseManager.isOwner(userId)) {
            rows.add(List.of(ADMIN));
        }
        return new Reply(text, Map.of("keyboard", rows, "resize_keyboard", true, "is_persistent", true));
    }

    private Reply fromAdmin(TelegramDatabaseManager.Reply reply) {
        return new Reply(reply.text(), reply.markup());
    }

    private Map<String, Object> inline(List<List<Map<String, String>>> rows) {
        return Map.of("inline_keyboard", rows);
    }

    private Map<String, String> button(String text, String data) {
        return Map.of("text", text, "callback_data", data);
    }

    private boolean isMenuButton(String text) {
        return text.equals(SCHEDULE) || text.equals(FACULTIES) || text.equals(PROFILE)
                || text.equals(TOPICS) || text.equals("ℹ️ Помощь") || text.equals(ADMIN);
    }

    private String facultyLabel(String name) {
        if (name.startsWith("ФИСТ")) {
            return "ФИСТ";
        }
        return name.replace(" факультет", "").replace(" факультет ИАТУ", " ИАТУ");
    }
}
