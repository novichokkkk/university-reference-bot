-- Sources checked on 2026-11-18: ulstu.ru/contacts/,
-- ulstu.ru/abitur/common/contacts/, ulstu.ru/abitur/bak_spec/faq/,
-- ulstu.ru/for_student/dormitories/, ulstu.ru/for_student/student-scholarship/.
-- The timetable below is fictional because the official service requires login.
ALTER TABLE prototype_knowledge ADD COLUMN demo BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE faq_entry ADD COLUMN demo BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE department_contact ADD COLUMN source_url VARCHAR(500);
ALTER TABLE schedule_lesson ADD COLUMN demo BOOLEAN NOT NULL DEFAULT TRUE;

UPDATE prototype_knowledge SET answer =
    'Поступление в УлГТУ: документы подают лично в приёмной комиссии или через Госуслуги. Сроки и правила новой кампании проверяйте на официальном сайте: https://ulstu.ru/abitur/bak_spec/faq/',
    demo = FALSE
WHERE intent = 'ADMISSION' AND (answer LIKE 'Демонстрационный ответ:%' OR answer = 'Пока не реализовано');
UPDATE prototype_knowledge SET answer =
    'Расписание занятий УлГТУ доступно через личный кабинет: https://lk.ulstu.ru/. В боте можно выбрать группу командой /group ДЕМО-1 и день командой /schedule 1.',
    demo = TRUE
WHERE intent = 'SCHEDULE' AND (answer LIKE 'Демонстрационный ответ:%' OR answer = 'Пока не реализовано');
UPDATE prototype_knowledge SET answer =
    'Приёмная комиссия: +7 (8422) 43-05-05, pk@ulstu.ru. Контакты факультетов доступны в /contacts и на https://ulstu.ru/abitur/common/contacts/.',
    demo = FALSE
WHERE intent = 'CONTACTS' AND (answer LIKE 'Демонстрационный ответ:%' OR answer = 'Пока не реализовано');
UPDATE prototype_knowledge SET answer =
    'УлГТУ располагает четырьмя общежитиями. Порядок заселения, документы и контакты заведующих: https://ulstu.ru/for_student/dormitories/.',
    demo = FALSE
WHERE intent = 'DORMITORY' AND (answer LIKE 'Демонстрационный ответ:%' OR answer = 'Пока не реализовано');
UPDATE prototype_knowledge SET answer =
    'В УлГТУ предусмотрены академическая, социальная и повышенная стипендии, а также материальная помощь. Действующие размеры и условия: https://ulstu.ru/for_student/student-scholarship/.',
    demo = FALSE
WHERE intent = 'SCHOLARSHIP' AND (answer LIKE 'Демонстрационный ответ:%' OR answer = 'Пока не реализовано');

UPDATE faq_entry SET answer = 'Выберите программу и подайте заявление лично в приёмной комиссии или через Госуслуги. Правила и способы подачи: https://ulstu.ru/abitur/bak_spec/faq/', demo = FALSE
WHERE question = 'Как поступить?';
UPDATE faq_entry SET answer = 'При подаче заявления нужны документ о личности, документ об образовании и СНИЛС; документы о достижениях и льготах — при наличии. Полный перечень: https://ulstu.ru/abitur/bak_spec/faq/', demo = FALSE
WHERE question = 'Какие документы нужны для поступления?';
UPDATE faq_entry SET answer = 'Сроки зависят от программы и вступительных испытаний. Кампания 2026 года завершена; даты следующего приёма проверяйте на https://ulstu.ru/abitur/bak_spec/faq/ или в приёмной комиссии: +7 (8422) 43-05-05.', demo = FALSE
WHERE question = 'Когда подать заявление?';
UPDATE faq_entry SET answer = 'Приёмная комиссия принимает документы по адресу: Ульяновск, ул. Северный Венец, 32, корпус 2; также пр. Созидателей, 13А. Телефон +7 (8422) 43-05-05, pk@ulstu.ru. Источник: https://ulstu.ru/news/17669/', demo = FALSE
WHERE question = 'Где приёмная комиссия?';

UPDATE faq_entry SET answer = 'Официальное расписание УлГТУ доступно в личном кабинете: https://lk.ulstu.ru/.', demo = FALSE
WHERE question = 'Где посмотреть расписание?';
UPDATE faq_entry SET answer = 'Выберите группу командой /group ДЕМО-1, затем отправьте /schedule 1.', demo = TRUE
WHERE question = 'Какие пары в понедельник?';
UPDATE faq_entry SET answer = 'Аудитория указана рядом с занятием в ответе на /schedule 1. Официальный источник расписания: https://lk.ulstu.ru/.', demo = TRUE
WHERE question = 'Где найти аудиторию?';
UPDATE faq_entry SET answer = 'После выбора группы отправьте /schedule 2 для просмотра вторника.', demo = TRUE
WHERE question = 'Есть ли занятия во вторник?';

UPDATE faq_entry SET answer = 'Уточните факультет. Для ФИСТ: +7 (8422) 778-585, fist@ulstu.ru. Контакты других факультетов: https://ulstu.ru/abitur/common/contacts/.', demo = FALSE
WHERE question = 'Как связаться с деканатом?';
UPDATE faq_entry SET answer = 'Контакты факультетов и телефонный справочник опубликованы на https://ulstu.ru/contacts/ и https://ulstu.ru/abitur/common/contacts/.', demo = FALSE
WHERE question = 'Как найти телефон кафедры?';
UPDATE faq_entry SET answer = 'Назовите факультет командой /contacts НАЗВАНИЕ. Полный перечень подразделений: https://ulstu.ru/abitur/common/contacts/.', demo = FALSE
WHERE question = 'Где найти подразделение?';
UPDATE faq_entry SET answer = 'Для ФИСТ адрес электронной почты — fist@ulstu.ru. Контакты остальных факультетов: https://ulstu.ru/abitur/common/contacts/.', demo = FALSE
WHERE question = 'Какая почта деканата?';

UPDATE faq_entry SET answer = 'Для заселения получите справку о зачислении в центре обслуживания студентов (корпус 6, каб. 301), затем ордер в общежитии и оформите договор. Порядок: https://ulstu.ru/for_student/dormitories/.', demo = FALSE
WHERE question = 'Как заселиться в общежитие?';
UPDATE faq_entry SET answer = 'Понадобятся паспорт, справка о зачислении, ордер на заселение и медицинская справка. Перечень и этапы: https://ulstu.ru/for_student/dormitories/.', demo = FALSE
WHERE question = 'Какие документы нужны для общежития?';
UPDATE faq_entry SET answer = 'Стоимость зависит от общежития и формы обучения. Актуальная таблица размещена на https://ulstu.ru/for_student/dormitories/.', demo = FALSE
WHERE question = 'Сколько стоит общежитие?';
UPDATE faq_entry SET answer = 'Срок и наличие мест уточняйте в выбранном общежитии. Контакты заведующих и порядок заселения: https://ulstu.ru/for_student/dormitories/.', demo = FALSE
WHERE question = 'Когда начинается заселение?';

UPDATE faq_entry SET answer = 'Конкретную дату выплаты уточняйте в деканате или в центре обслуживания студентов. Виды стипендий и документы: https://ulstu.ru/for_student/student-scholarship/.', demo = FALSE
WHERE question = 'Когда выплачивают стипендию?';
UPDATE faq_entry SET answer = 'Повышенную государственную стипендию назначают за достижения в учёбе, научной, общественной, культурной или спортивной деятельности. Условия: https://ulstu.ru/for_student/student-scholarship/.', demo = FALSE
WHERE question = 'Как получить повышенную стипендию?';
UPDATE faq_entry SET answer = 'Материальная помощь предусмотрена для перечисленных университетом категорий студентов при наличии подтверждающих документов. Порядок и перечень: https://ulstu.ru/for_student/student-scholarship/.', demo = FALSE
WHERE question = 'Как оформить материальную помощь?';
UPDATE faq_entry SET answer = 'Размер зависит от вида стипендии и условий назначения. Актуальная таблица и приказы: https://ulstu.ru/for_student/student-scholarship/.', demo = FALSE
WHERE question = 'Каков размер стипендии?';

DELETE FROM department_contact WHERE name IN ('Учебный деканат (демо)', 'Приёмная комиссия (демо)');
INSERT INTO department_contact (name, phone, email, source_url) VALUES
('Приёмная комиссия УлГТУ', '+7 (8422) 43-05-05', 'pk@ulstu.ru', 'https://ulstu.ru/news/17669/'),
('Приёмная ректора УлГТУ', '+7 (8422) 43-06-43', 'rector@ulstu.ru', 'https://ulstu.ru/contacts/'),
('ФИСТ (деканат факультета информационных систем и технологий)', '+7 (8422) 778-585', 'fist@ulstu.ru', 'https://ulstu.ru/abitur/common/contacts/'),
('Радиотехнический факультет', '+7 (8422) 778-389', 'rtf@ulstu.ru', 'https://ulstu.ru/abitur/common/contacts/'),
('Гуманитарный факультет', '+7 (8422) 778-372', 'gf@ulstu.ru', 'https://ulstu.ru/abitur/common/contacts/'),
('Машиностроительный факультет', '+7 (8422) 41-79-96', 'dmf@ulstu.ru', 'https://ulstu.ru/abitur/common/contacts/'),
('Инженерно-экономический факультет', '+7 (8422) 778-601', 'emf@ulstu.ru', 'https://ulstu.ru/abitur/common/contacts/'),
('Строительный факультет', '+7 (8422) 778-423', 'g.petuhova@ulstu.ru', 'https://ulstu.ru/abitur/common/contacts/'),
('Институт авиационных технологий и управления', '+7 (8422) 20-89-95', 'avia@ulstu.ru', 'https://ulstu.ru/contacts/'),
('Институт непрерывного образования', '+7 (8422) 77-88-45', 'inno@ulstu.ru', 'https://ulstu.ru/contacts/');

DELETE FROM schedule_lesson WHERE group_code = 'ДЕМО-1';
INSERT INTO schedule_lesson (group_code, day_of_week, starts_at, subject, room, demo) VALUES
('ДЕМО-1', 1, '09:00', 'Конструирование программного обеспечения', '3-421', TRUE),
('ДЕМО-1', 1, '10:40', 'Базы данных', '3-315', TRUE),
('ДЕМО-1', 2, '09:00', 'Разработка веб-приложений', '3-416', TRUE),
('ДЕМО-1', 2, '10:40', 'Тестирование программного обеспечения', '3-407', TRUE),
('ДЕМО-1', 3, '12:20', 'Информационная безопасность', '3-312', TRUE),
('ДЕМО-1', 3, '14:00', 'Управление проектами', '3-424', TRUE),
('ДЕМО-1', 4, '09:00', 'Программная инженерия', '3-421', TRUE),
('ДЕМО-1', 4, '10:40', 'Анализ и проектирование систем', '3-416', TRUE),
('ДЕМО-1', 5, '10:40', 'Экономика программных проектов', '3-312', TRUE),
('ДЕМО-1', 5, '12:20', 'Практикум по Java', '3-407', TRUE);
