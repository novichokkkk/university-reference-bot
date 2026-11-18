package ru.ulstu.referencebot.controller;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ru.ulstu.referencebot.entity.KnowledgeEntry;
import ru.ulstu.referencebot.repository.KnowledgeRepository;

@RestController
@RequestMapping("/api/admin/knowledge")
public class AdminController {
    private final KnowledgeRepository knowledge;
    private final String adminKey;

    public AdminController(KnowledgeRepository knowledge, @Value("${ADMIN_KEY:}") String adminKey) {
        this.knowledge = knowledge;
        this.adminKey = adminKey;
    }

    private void authorize(String supplied) {
        if (adminKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Admin API is disabled");
        }
        if (supplied == null || !MessageDigest.isEqual(adminKey.getBytes(StandardCharsets.UTF_8),
                supplied.getBytes(StandardCharsets.UTF_8))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid admin key");
        }
    }

    public record EntryInput(String category, String answer) { }

    @GetMapping
    public List<KnowledgeEntry> list(@RequestHeader(value = "X-Admin-Key", required = false) String key) {
        authorize(key);
        return knowledge.findAll();
    }

    @PostMapping("/{intent}")
    public KnowledgeEntry create(@RequestHeader(value = "X-Admin-Key", required = false) String key,
            @PathVariable String intent, @RequestBody EntryInput input) {
        authorize(key);
        validate(intent, input);
        if (knowledge.existsById(intent)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Entry exists");
        }
        return knowledge.save(new KnowledgeEntry(intent, input.category().trim(), input.answer().trim()));
    }

    @PutMapping("/{intent}")
    public KnowledgeEntry update(@RequestHeader(value = "X-Admin-Key", required = false) String key,
            @PathVariable String intent, @RequestBody EntryInput input) {
        authorize(key);
        validate(intent, input);
        if (!knowledge.existsById(intent)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Entry not found");
        }
        return knowledge.save(new KnowledgeEntry(intent, input.category().trim(), input.answer().trim()));
    }

    @DeleteMapping("/{intent}")
    public void delete(@RequestHeader(value = "X-Admin-Key", required = false) String key,
            @PathVariable String intent) {
        authorize(key);
        if (!knowledge.existsById(intent)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Entry not found");
        }
        knowledge.deleteById(intent);
    }

    private void validate(String intent, EntryInput input) {
        if (!intent.matches("[A-Z_]{2,40}") || input == null || input.category() == null
                || input.category().isBlank() || input.category().length() > 100 || input.answer() == null
                || input.answer().isBlank() || input.answer().length() > 2000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid entry");
        }
    }
}
