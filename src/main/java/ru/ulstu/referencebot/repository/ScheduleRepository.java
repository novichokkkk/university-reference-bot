package ru.ulstu.referencebot.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.ulstu.referencebot.entity.ScheduleLesson;

public interface ScheduleRepository extends JpaRepository<ScheduleLesson, Long> {
    List<ScheduleLesson> findByGroupCodeIgnoreCaseAndDayOfWeekOrderByStartsAt(String groupCode, int dayOfWeek);
}
