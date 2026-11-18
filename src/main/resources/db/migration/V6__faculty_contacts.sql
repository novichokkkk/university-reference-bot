-- Faculty contacts checked on 2026-11-18 against the official UlSTU pages.
ALTER TABLE department_contact ADD COLUMN faculty BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE department_contact SET faculty = TRUE WHERE name IN (
    'ФИСТ (деканат факультета информационных систем и технологий)',
    'Радиотехнический факультет',
    'Гуманитарный факультет',
    'Машиностроительный факультет',
    'Инженерно-экономический факультет',
    'Строительный факультет'
);

INSERT INTO department_contact (name, phone, email, source_url, faculty) VALUES
('Энергетический факультет', '+7 (8422) 778-397', 'a.dubov@ulstu.ru',
 'https://ulstu.ru/abitur/common/contacts/', TRUE),
('Заочно-вечерний факультет', '+7 (8422) 41-80-04', 'zvf@ulstu.ru',
 'https://ulstu.ru/education/faculties/zvf/', TRUE),
('Самолетостроительный факультет ИАТУ', '+7 (8422) 208-857', 'dekanat-ssf@yandex.ru',
 'https://ulstu.ru/education/faculties/ssf-iatu/', TRUE),
('Подготовительный факультет МИ', '+7 (8422) 778-427', 'v.soloscheva@ulstu.ru',
 'https://ulstu.ru/education/faculties/pf-mi/', TRUE);
