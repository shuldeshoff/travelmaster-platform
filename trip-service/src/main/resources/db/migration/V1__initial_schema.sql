-- Trip Service Database Schema

-- Создание таблицы поездок
CREATE TABLE trips (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    origin VARCHAR(100) NOT NULL,
    destination VARCHAR(100) NOT NULL,
    departure_date TIMESTAMP NOT NULL,
    return_date TIMESTAMP,
    price DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'RUB',
    total_seats INTEGER NOT NULL,
    available_seats INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    provider_id VARCHAR(100),
    provider VARCHAR(50),
    inclusions TEXT,
    exclusions TEXT,
    min_age INTEGER,
    max_age INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    
    CONSTRAINT chk_seats CHECK (available_seats >= 0 AND available_seats <= total_seats),
    CONSTRAINT chk_price CHECK (price >= 0)
);

-- Создание таблицы сегментов
CREATE TABLE segments (
    id BIGSERIAL PRIMARY KEY,
    trip_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL,
    segment_order INTEGER NOT NULL,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    provider VARCHAR(100),
    provider_reference VARCHAR(100),
    description TEXT,
    
    -- Поля для перелётов
    flight_number VARCHAR(20),
    airline VARCHAR(100),
    departure_airport VARCHAR(10),
    arrival_airport VARCHAR(10),
    cabin_class VARCHAR(50),
    
    -- Поля для отелей
    hotel_name VARCHAR(200),
    hotel_address VARCHAR(500),
    room_type VARCHAR(100),
    check_in TIMESTAMP,
    check_out TIMESTAMP,
    star_rating INTEGER,
    
    -- Поля для трансферов
    pickup_location VARCHAR(200),
    dropoff_location VARCHAR(200),
    vehicle_type VARCHAR(100),
    
    version BIGINT NOT NULL DEFAULT 0,
    
    FOREIGN KEY (trip_id) REFERENCES trips(id) ON DELETE CASCADE
);

-- Индексы для оптимизации запросов
CREATE INDEX idx_trips_origin_destination ON trips(origin, destination);
CREATE INDEX idx_trips_departure_date ON trips(departure_date);
CREATE INDEX idx_trips_price ON trips(price);
CREATE INDEX idx_trips_status ON trips(status);
CREATE INDEX idx_trips_provider ON trips(provider, provider_id);

CREATE INDEX idx_segments_trip_id ON segments(trip_id);
CREATE INDEX idx_segments_type ON segments(type);
CREATE INDEX idx_segments_order ON segments(trip_id, segment_order);

-- Вставка тестовых данных

-- Поездка 1: Москва → Сочи (перелёт + отель)
INSERT INTO trips (title, description, origin, destination, departure_date, return_date, price, total_seats, available_seats, provider)
VALUES (
    'Отдых в Сочи - 7 дней',
    'Незабываемый отдых на Черном море с проживанием в отеле 4*',
    'Москва',
    'Сочи',
    CURRENT_TIMESTAMP + INTERVAL '30 days',
    CURRENT_TIMESTAMP + INTERVAL '37 days',
    45000.00,
    20,
    20,
    'INTERNAL'
);

-- Сегменты для поездки 1
INSERT INTO segments (trip_id, type, segment_order, start_time, end_time, flight_number, airline, departure_airport, arrival_airport, cabin_class)
VALUES (
    1,
    'FLIGHT',
    1,
    CURRENT_TIMESTAMP + INTERVAL '30 days',
    CURRENT_TIMESTAMP + INTERVAL '30 days' + INTERVAL '2 hours',
    'SU1234',
    'Aeroflot',
    'SVO',
    'AER',
    'Economy'
);

INSERT INTO segments (trip_id, type, segment_order, check_in, check_out, hotel_name, hotel_address, room_type, star_rating)
VALUES (
    1,
    'HOTEL',
    2,
    CURRENT_TIMESTAMP + INTERVAL '30 days',
    CURRENT_TIMESTAMP + INTERVAL '37 days',
    'Radisson Blu Resort',
    'Курортный проспект, 103',
    'Standard Room',
    4
);

INSERT INTO segments (trip_id, type, segment_order, start_time, end_time, flight_number, airline, departure_airport, arrival_airport, cabin_class)
VALUES (
    1,
    'FLIGHT',
    3,
    CURRENT_TIMESTAMP + INTERVAL '37 days',
    CURRENT_TIMESTAMP + INTERVAL '37 days' + INTERVAL '2 hours',
    'SU1235',
    'Aeroflot',
    'AER',
    'SVO',
    'Economy'
);

-- Поездка 2: Санкт-Петербург → Казань
INSERT INTO trips (title, description, origin, destination, departure_date, return_date, price, total_seats, available_seats, provider)
VALUES (
    'Казань - культурная столица',
    'Экскурсионный тур по Казани с посещением Кремля и мечети Кул-Шариф',
    'Санкт-Петербург',
    'Казань',
    CURRENT_TIMESTAMP + INTERVAL '15 days',
    CURRENT_TIMESTAMP + INTERVAL '18 days',
    25000.00,
    15,
    12,
    'INTERNAL'
);

-- Поездка 3: Москва → Владивосток
INSERT INTO trips (title, description, origin, destination, departure_date, return_date, price, total_seats, available_seats, status, provider)
VALUES (
    'Путешествие на Дальний Восток',
    'Уникальная возможность увидеть Владивосток и окрестности',
    'Москва',
    'Владивосток',
    CURRENT_TIMESTAMP + INTERVAL '45 days',
    CURRENT_TIMESTAMP + INTERVAL '52 days',
    89000.00,
    10,
    10,
    'AVAILABLE',
    'INTERNAL'
);

-- Поездка 4: Екатеринбург → Байкал
INSERT INTO trips (title, description, origin, destination, departure_date, return_date, price, total_seats, available_seats, provider)
VALUES (
    'Байкал - жемчужина Сибири',
    'Путешествие к самому глубокому озеру в мире',
    'Екатеринбург',
    'Иркутск',
    CURRENT_TIMESTAMP + INTERVAL '60 days',
    CURRENT_TIMESTAMP + INTERVAL '70 days',
    67000.00,
    12,
    12,
    'INTERNAL'
);

-- Поездка 5: Москва → Санкт-Петербург (SOLD OUT для теста)
INSERT INTO trips (title, description, origin, destination, departure_date, return_date, price, total_seats, available_seats, status, provider)
VALUES (
    'Белые ночи в Питере',
    'Классический тур по Санкт-Петербургу в период белых ночей',
    'Москва',
    'Санкт-Петербург',
    CURRENT_TIMESTAMP + INTERVAL '10 days',
    CURRENT_TIMESTAMP + INTERVAL '13 days',
    18000.00,
    25,
    0,
    'FULL',
    'INTERNAL'
);

