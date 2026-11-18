package ru.ulstu.referencebot.service;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import ru.ulstu.referencebot.entity.DepartmentContact;
import ru.ulstu.referencebot.entity.ScheduleLesson;
import ru.ulstu.referencebot.entity.StudentProfile;
import ru.ulstu.referencebot.repository.ContactRepository;
import ru.ulstu.referencebot.repository.ProfileRepository;
import ru.ulstu.referencebot.repository.ScheduleRepository;

@Service
public class ReferenceService {
    private final ScheduleRepository schedules;
    private final ContactRepository contacts;
    private final ProfileRepository profiles;

    public ReferenceService(ScheduleRepository schedules, ContactRepository contacts, ProfileRepository profiles) {
        this.schedules = schedules;
        this.contacts = contacts;
        this.profiles = profiles;
    }

    public List<ScheduleLesson> schedule(String group, int day) {
        if (group == null || !group.matches("[\\p{L}0-9-]{1,30}") || day < 1 || day > 7) {
            throw new IllegalArgumentException("Укажите группу и день недели от 1 до 7.");
        }
        return schedules.findByGroupCodeIgnoreCaseAndDayOfWeekOrderByStartsAt(group, day);
    }

    public String scheduleText(String group, int day) {
        var lessons = schedule(group, day);
        if (lessons.isEmpty()) {
            return "Для группы " + group + " на день " + day + " расписание не найдено.";
        }
        StringBuilder text = new StringBuilder("Расписание группы " + group + ", день " + day + ":");
        for (var lesson : lessons) {
            text.append("\n").append(lesson.getStartsAt()).append(" ")
                    .append(lesson.getSubject()).append(" (ауд. ").append(lesson.getRoom()).append(")");
        }
        return text.toString();
    }

    public List<DepartmentContact> contacts(String query) {
        if (query == null || query.isBlank() || query.length() > 100) {
            throw new IllegalArgumentException("Укажите название подразделения.");
        }
        return contacts.findByNameContainingIgnoreCase(query.trim());
    }

    public String contactText(String query) {
        var found = contacts(query);
        if (found.isEmpty()) {
            return "Подразделение не найдено. Уточните название.";
        }
        if (found.size() > 1) {
            return "Найдено несколько подразделений. Уточните название: "
                    + String.join(", ", found.stream().map(DepartmentContact::getName).toList());
        }
        var contact = found.get(0);
        return formatContact(contact);
    }

    public String facultyContact(long id) {
        var contact = contacts.findById(id)
                .filter(DepartmentContact::isFaculty)
                .orElseThrow(() -> new IllegalArgumentException("Факультет не найден."));
        return formatContact(contact);
    }

    private String formatContact(DepartmentContact contact) {
        String text = contact.getName() + ": телефон " + contact.getPhone() + ", e-mail " + contact.getEmail();
        return contact.getSourceUrl() == null || contact.getSourceUrl().isBlank()
                ? text : text + "\nИсточник: " + contact.getSourceUrl();
    }

    public StudentProfile saveProfile(long telegramId, String group) {
        if (telegramId <= 0 || group == null || !group.matches("[\\p{L}0-9-]{1,30}")) {
            throw new IllegalArgumentException("Некорректный идентификатор или группа.");
        }
        return profiles.save(new StudentProfile(telegramId, group));
    }

    public Optional<String> profileGroup(long telegramId) {
        return profiles.findById(telegramId).map(StudentProfile::getGroupCode);
    }

    public String personalSchedule(long telegramId, int day) {
        return profiles.findById(telegramId)
                .map(profile -> scheduleText(profile.getGroupCode(), day))
                .orElse("Сначала укажите группу командой /group ГРУППА.");
    }
}
