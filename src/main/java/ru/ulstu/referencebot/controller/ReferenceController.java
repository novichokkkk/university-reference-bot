package ru.ulstu.referencebot.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ru.ulstu.referencebot.entity.DepartmentContact;
import ru.ulstu.referencebot.entity.ScheduleLesson;
import ru.ulstu.referencebot.service.ReferenceService;

@RestController
@RequestMapping("/api/reference")
public class ReferenceController {
    private final ReferenceService reference;

    public ReferenceController(ReferenceService reference) {
        this.reference = reference;
    }

    @GetMapping("/schedule")
    public List<ScheduleLesson> schedule(@RequestParam String group, @RequestParam int day) {
        try {
            return reference.schedule(group, day);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        }
    }

    @GetMapping("/contacts")
    public List<DepartmentContact> contacts(@RequestParam String query) {
        try {
            return reference.contacts(query);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        }
    }

}
