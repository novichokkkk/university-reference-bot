package ru.ulstu.referencebot;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import ru.ulstu.referencebot.repository.ProfileRepository;
import ru.ulstu.referencebot.repository.FaqRepository;
import ru.ulstu.referencebot.repository.ContactRepository;
import ru.ulstu.referencebot.service.AnswerService;
import ru.ulstu.referencebot.service.ReferenceService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = "ADMIN_KEY=test-admin-key")
class ReferenceTest {
    @Autowired
    private ReferenceService reference;
    @Autowired
    private ProfileRepository profiles;
    @Autowired
    private MockMvc mvc;
    @Autowired
    private FaqRepository faq;
    @Autowired
    private AnswerService answers;
    @Autowired
    private ContactRepository contacts;

    @Test
    void twentyFaqEntriesCanAnswerExactQuestion() {
        assertEquals(20, faq.count());
        assertEquals("ADMISSION", answers.answer("Когда подать заявление?").intent());
        assertTrue(answers.answer("Когда подать заявление?").answer().contains("ulstu.ru"));
        assertEquals(false, answers.answer("Когда подать заявление?").demo());
        assertEquals(true, answers.answer("Какие пары в понедельник?").demo());
    }

    @Test
    void scheduleAndMissingData() throws Exception {
        assertEquals(2, reference.schedule("ПИбд-42", 1).size());
        assertTrue(reference.scheduleText("ПИбд-42", 6).contains("не найдено"));
        assertTrue(reference.schedule("ПИбд-42", 1).get(0).isDemo());
        mvc.perform(get("/api/reference/schedule").param("group", "ПИбд-42").param("day", "8"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void contactSearchAndMissingData() throws Exception {
        assertEquals(14, contacts.count());
        assertEquals(10, contacts.findByFacultyTrueOrderByNameAsc().size());
        assertTrue(reference.contactText("Энергетический").contains("a.dubov@ulstu.ru"));
        assertTrue(reference.contactText("деканат").contains("fist@ulstu.ru"));
        assertTrue(reference.contactText("деканат").contains("https://ulstu.ru/"));
        assertTrue(reference.contactText("несуществующее").contains("не найдено"));
        mvc.perform(get("/api/reference/contacts").param("query", " "))
                .andExpect(status().isBadRequest());
    }

    @Test
    void profilePersistsAndPersonalizesSchedule() {
        reference.saveProfile(123456L, "ПИбд-42");
        assertEquals("ПИбд-42", profiles.findById(123456L).orElseThrow().getGroupCode());
        assertTrue(reference.personalSchedule(123456L, 1).contains("Базы данных"));
        assertTrue(reference.personalSchedule(987654L, 1).contains("/group"));
    }

    @Test
    void adminRejectsMissingKeyAndValidatesData() throws Exception {
        mvc.perform(get("/api/admin/knowledge")).andExpect(status().isForbidden());
        mvc.perform(post("/api/admin/knowledge/NEW_TOPIC").header("X-Admin-Key", "test-admin-key")
                .contentType(MediaType.APPLICATION_JSON).content("{\"category\":\"x\",\"answer\":\" \"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void adminChangesAreVisibleWithoutRestart() throws Exception {
        mvc.perform(post("/api/admin/knowledge/NEW_TOPIC").header("X-Admin-Key", "test-admin-key")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"category\":\"Новая тема\",\"answer\":\"Первый ответ\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.answer").value("Первый ответ"));
        mvc.perform(put("/api/admin/knowledge/NEW_TOPIC").header("X-Admin-Key", "test-admin-key")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"category\":\"Новая тема\",\"answer\":\"Исправленный ответ\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.answer").value("Исправленный ответ"));
        mvc.perform(delete("/api/admin/knowledge/NEW_TOPIC").header("X-Admin-Key", "test-admin-key"))
                .andExpect(status().isOk());
    }
}
