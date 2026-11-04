package ru.ulstu.referencebot.service;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class IntentService {
    private final Map<String, String> patterns = new LinkedHashMap<>();

    public IntentService() {
        patterns.put("ADMISSION", "приемн|поступ|абитуриент|документ");
        patterns.put("SCHEDULE", "расписан|заняти|пары|пара|аудитори");
        patterns.put("CONTACTS", "контакт|деканат|телефон|подразделен");
        patterns.put("DORMITORY", "общежити|заселен|проживан");
        patterns.put("SCHOLARSHIP", "стипенди|выплат|материальн");
    }

    public String classify(String text) {
        String normalized = text.trim().toLowerCase(Locale.ROOT).replace('ё', 'е');
        if (normalized.equals("/start") || normalized.equals("/help")) {
            return "HELP";
        }
        String found = "UNKNOWN";
        for (var entry : patterns.entrySet()) {
            if (java.util.regex.Pattern.compile("(?U)\\b(?:" + entry.getValue() + ")")
                    .matcher(normalized).find()) {
                if (!found.equals("UNKNOWN")) {
                    return "AMBIGUOUS";
                }
                found = entry.getKey();
            }
        }
        return found;
    }
}
