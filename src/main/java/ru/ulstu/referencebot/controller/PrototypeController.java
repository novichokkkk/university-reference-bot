package ru.ulstu.referencebot.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.ulstu.referencebot.service.AnswerService;

@RestController
@RequestMapping("/api/prototype")
public class PrototypeController {
    private final AnswerService answers;

    public PrototypeController(AnswerService answers) {
        this.answers = answers;
    }

    public record Question(@NotBlank @Size(max = 1000) String text) { }

    @PostMapping("/ask")
    public AnswerService.Reply ask(@Valid @RequestBody Question question) {
        return answers.answer(question.text());
    }
}
