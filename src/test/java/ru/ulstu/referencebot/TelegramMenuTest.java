package ru.ulstu.referencebot;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ru.ulstu.referencebot.repository.ContactRepository;
import ru.ulstu.referencebot.telegram.TelegramMenuService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class TelegramMenuTest {
    @Autowired
    private TelegramMenuService menu;
    @Autowired
    private ContactRepository contacts;

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
    void topicsAndProfileHelpStayAccessible() {
        assertTrue(menu.onText(81003L, "📚 Темы").markup().containsKey("inline_keyboard"));
        assertTrue(menu.onCallback(81003L, "topic:dormitory").text().contains("ulstu.ru"));
        assertTrue(menu.onText(81003L, "⚙️ Админка").text().contains("ADMIN_KEY"));
        assertTrue(menu.onText(81003L, "/cancel").markup().containsKey("keyboard"));
    }
}
