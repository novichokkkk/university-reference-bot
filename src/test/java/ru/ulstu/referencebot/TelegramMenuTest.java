package ru.ulstu.referencebot;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import ru.ulstu.referencebot.repository.FaqRepository;
import ru.ulstu.referencebot.repository.ContactRepository;
import ru.ulstu.referencebot.service.AnswerService;
import ru.ulstu.referencebot.telegram.TelegramDatabaseManager;
import ru.ulstu.referencebot.telegram.TelegramMenuService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = "TELEGRAM_ADMIN_ID=81099")
class TelegramMenuTest {
    @Autowired
    private TelegramMenuService menu;
    @Autowired
    private ContactRepository contacts;
    @Autowired
    private TelegramDatabaseManager databaseManager;
    @Autowired
    private FaqRepository faq;
    @Autowired
    private AnswerService answers;

    @Test
    void menuGuidesUserThroughGroupAndSchedule() {
        long userId = 81001L;
        assertTrue(menu.onText(userId, "/start").markup().containsKey("keyboard"));
        assertTrue(menu.onText(userId, "📅 Расписание").text().contains("укажите группу"));
        assertTrue(menu.onCallback(userId, "profile:group").text().contains("Напишите"));
        assertTrue(menu.onText(userId, "ПИбд-42").text().contains("сохранена"));
        assertTrue(menu.onText(userId, "👤 Профиль").text().contains("ПИбд-42"));
        assertTrue(menu.onCallback(userId, "day:1").text().contains("Базы данных"));
    }

    @Test
    void facultyButtonsShowVerifiedContacts() {
        var faculties = contacts.findByFacultyTrueOrderByNameAsc();
        assertEquals(10, faculties.size());
        var reply = menu.onText(81002L, "🏛 Факультеты");
        assertTrue(reply.markup().containsKey("inline_keyboard"));
        long energyId = faculties.stream().filter(f -> f.getName().equals("Энергетический факультет"))
                .findFirst().orElseThrow().getId();
        var contact = menu.onCallback(81002L, "faculty:" + energyId);
        assertTrue(contact.text().contains("778-397"));
        assertTrue(contact.text().contains("ulstu.ru"));
        assertTrue(menu.onCallback(81002L, "faculty:-1").text().contains("не найден"));
    }

    @Test
    void topicsAndProfileStayAccessibleWithoutHelpButton() {
        assertTrue(menu.onText(81003L, "📚 Темы").markup().containsKey("inline_keyboard"));
        assertTrue(menu.onCallback(81003L, "topic:dormitory").text().contains("ulstu.ru"));
        assertFalse(menu.onText(81003L, "/start").markup().toString().contains("Помощь"));
        assertTrue(menu.onText(81003L, "/cancel").markup().containsKey("keyboard"));
    }

    @Test
    void leavingGroupPromptDoesNotSaveUnrelatedMessageAsGroup() {
        long userId = 81005L;
        menu.onCallback(userId, "profile:group");
        menu.onCallback(userId, "menu:faculties");
        menu.onText(userId, "обычный вопрос");
        assertTrue(menu.onText(userId, "👤 Профиль").text().contains("не указана"));
    }

    @Test
    void onlyOwnerCanSeeAndUseDatabaseManagement() {
        long owner = 81099L;
        long stranger = 81004L;
        assertTrue(menu.onText(owner, "/start").markup().toString().contains("Управление базой"));
        assertFalse(menu.onText(stranger, "/start").markup().toString().contains("Управление базой"));
        assertTrue(menu.onText(stranger, "Управление базой").text().contains("недоступен"));
        assertTrue(menu.onCallback(stranger, "admin:home").text().contains("недоступен"));
        assertTrue(menu.onText(owner, "Управление базой").text().contains("частые вопросы"));
    }

    @Test
    void ownerCanAddEditAndDeleteFaqWhileStrangerCannot() {
        long owner = 81099L;
        String question = "Тестовый вопрос Telegram о приёме?";
        menu.onCallback(owner, "admin:faq-add:ADMISSION");
        assertTrue(menu.onText(owner, question).text().contains("Теперь отправь ответ"));
        menu.onText(owner, "Первый ответ");
        long id = faq.findByQuestionIgnoreCase(question).orElseThrow().getId();
        assertEquals("Первый ответ", answers.answer(question).answer());

        menu.onCallback(owner, "admin:faq-edit:" + id);
        menu.onText(owner, "Исправленный ответ");
        assertEquals("Исправленный ответ", answers.answer(question).answer());

        assertTrue(menu.onCallback(81004L, "admin:faq-delete-confirm:" + id).text().contains("недоступен"));
        assertTrue(faq.existsById(id));
        menu.onCallback(owner, "admin:faq-delete:" + id);
        menu.onCallback(owner, "admin:faq-delete-confirm:" + id);
        assertFalse(faq.existsById(id));
        assertFalse(databaseManager.hasPending(owner));
    }
}
