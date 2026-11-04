package ru.ulstu.referencebot;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import ru.ulstu.referencebot.repository.KnowledgeRepository;
import ru.ulstu.referencebot.service.AnswerService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PrototypeTest {
    @Autowired
    private AnswerService answers;
    @Autowired
    private KnowledgeRepository repository;
    @Autowired
    private MockMvc mvc;

    @ParameterizedTest
    @CsvSource({
        "Как поступить, ADMISSION", "ПРИЁМНАЯ комиссия, ADMISSION", "Я абитуриент, ADMISSION",
        "Какие документы нужны, ADMISSION", "Поступление в вуз, ADMISSION",
        "Расписание занятий, SCHEDULE", "Когда пары, SCHEDULE", "Где аудитория, SCHEDULE",
        "Когда первая пара, SCHEDULE", "Начало занятий, SCHEDULE",
        "Контакты вуза, CONTACTS", "Где деканат, CONTACTS", "Телефон кафедры, CONTACTS",
        "Подразделения вуза, CONTACTS", "Как связаться с деканатом, CONTACTS",
        "Как попасть в общежитие, DORMITORY", "Заселение студентов, DORMITORY",
        "Стоимость проживания, DORMITORY", "Место в общежитии, DORMITORY", "Дата заселения, DORMITORY",
        "Когда стипендия, SCHOLARSHIP", "Сроки выплаты, SCHOLARSHIP", "Материальная помощь, SCHOLARSHIP",
        "Размер стипендии, SCHOLARSHIP", "Повышенная стипендия, SCHOLARSHIP",
        "Прогноз погоды, UNKNOWN", "Заказать пиццу, UNKNOWN", "Контакты и расписание, AMBIGUOUS",
        "/start, HELP", "/help, HELP"
    })
    void recognizesControlPhrases(String text, String expected) {
        assertEquals(expected, answers.answer(text).intent());
    }

    @Test
    void migrationSeedsFiveTopics() {
        assertEquals(5, repository.count());
        assertEquals(repository.findById("ADMISSION").orElseThrow().getAnswer(),
                answers.answer("Поступление").answer());
    }

    @Test
    void rejectsInvalidServiceInput() {
        assertThrows(IllegalArgumentException.class, () -> answers.answer(null));
        assertThrows(IllegalArgumentException.class, () -> answers.answer(" "));
        assertThrows(IllegalArgumentException.class, () -> answers.answer("x".repeat(1001)));
    }

    @Test
    void returnsAnswerThroughHttp() throws Exception {
        mvc.perform(post("/api/prototype/ask").contentType(MediaType.APPLICATION_JSON)
                .content("{\"text\":\"Как поступить?\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.intent").value("ADMISSION"))
                .andExpect(jsonPath("$.demo").value(true));
    }

    @Test
    void rejectsBlankHttpQuestion() throws Exception {
        mvc.perform(post("/api/prototype/ask").contentType(MediaType.APPLICATION_JSON)
                .content("{\"text\":\" \"}")) .andExpect(status().isBadRequest());
    }
}
