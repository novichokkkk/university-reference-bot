-- Preserve the V4 checksum: the timetable rows are moved in a separate migration.
UPDATE schedule_lesson SET group_code = 'ПИбд-42'
WHERE group_code = 'ДЕМО-1' AND demo = TRUE;

UPDATE student_profile SET group_code = 'ПИбд-42'
WHERE group_code = 'ДЕМО-1';

UPDATE prototype_knowledge
SET answer = REPLACE(answer, 'ДЕМО-1', 'ПИбд-42')
WHERE intent = 'SCHEDULE' AND answer LIKE '%ДЕМО-1%';

UPDATE faq_entry
SET answer = REPLACE(answer, 'ДЕМО-1', 'ПИбд-42')
WHERE intent = 'SCHEDULE' AND answer LIKE '%ДЕМО-1%';
