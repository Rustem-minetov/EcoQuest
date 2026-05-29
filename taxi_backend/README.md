# 🚕 Taxi Backend — Django REST API

Бэкенд для приложения поиска такси на районы и посёлки.

## Структура проекта

```
taxi_backend/
├── config/
│   ├── __init__.py
│   ├── settings.py      ← Все настройки
│   ├── urls.py          ← Главный роутер
│   └── wsgi.py
├── rides/
│   ├── models.py        ← Модели: Driver, RideOffer, Booking
│   ├── serializers.py   ← JSON ↔ Python
│   ├── views.py         ← Логика эндпоинтов
│   ├── urls.py          ← Маршруты /api/...
│   └── admin.py         ← Настройка Django Admin
├── manage.py
├── requirements.txt
└── README.md
```

---

## ⚡ Быстрый старт

### 1. Установить зависимости

```bash
# Создать виртуальное окружение
python -m venv venv

# Активировать (Windows)
venv\Scripts\activate

# Активировать (Mac / Linux)
source venv/bin/activate

# Установить библиотеки
pip install -r requirements.txt
```

### 2. Создать базу данных

```bash
python manage.py makemigrations
python manage.py migrate
```

### 3. Создать суперпользователя (для /admin/)

```bash
python manage.py createsuperuser
# Введи: имя, email, пароль
```

### 4. Запустить сервер

```bash
# Только на своём компьютере:
python manage.py runserver

# Чтобы телефон тоже мог подключиться (по локальной сети Wi-Fi):
python manage.py runserver 0.0.0.0:8000
```

После этого:
- Браузер: `http://127.0.0.1:8000/api/`
- Телефон в той же Wi-Fi: `http://192.168.X.X:8000/api/`
  _(замени на свой IP — посмотри в настройках Wi-Fi или через `ipconfig` / `ifconfig`)_

---

## 📡 API Эндпоинты

### Объявления о поездках

| Метод | URL | Описание |
|-------|-----|----------|
| `GET` | `/api/rides/` | Список активных объявлений |
| `GET` | `/api/rides/?search=Сергели` | Поиск по направлению |
| `GET` | `/api/rides/?to=Сергели&from=Ташкент` | Фильтр откуда/куда |
| `GET` | `/api/rides/?ordering=price_per_seat` | Сортировка по цене |
| `GET` | `/api/rides/<id>/` | Детали объявления |
| `POST` | `/api/rides/create/` | Создать объявление |
| `PATCH` | `/api/rides/<id>/status/` | Изменить статус |

### Таксисты

| Метод | URL | Описание |
|-------|-----|----------|
| `POST` | `/api/drivers/register/` | Зарегистрировать таксиста |
| `GET` | `/api/drivers/` | Список таксистов |
| `GET` | `/api/drivers/<id>/` | Профиль + объявления |

### Бронирования

| Метод | URL | Описание |
|-------|-----|----------|
| `POST` | `/api/bookings/create/` | Оставить заявку |
| `GET` | `/api/bookings/?phone=+998...` | Мои заявки |

---

## 📋 Примеры запросов (для Postman)

### Зарегистрировать таксиста
```
POST http://localhost:8000/api/drivers/register/
Content-Type: application/json

{
    "name": "Алибек Юсупов",
    "phone": "+998901234567",
    "telegram_username": "alibek_taxi",
    "car_model": "Chevrolet Cobalt",
    "car_color": "Белый",
    "car_plate": "01 A 123 BA"
}
```

### Создать объявление
```
POST http://localhost:8000/api/rides/create/
Content-Type: application/json

{
    "driver_id": 1,
    "from_city": "Ташкент (м. Буюк Ипак Йули)",
    "to_district": "Сергели, массив Куйлюк",
    "route_description": "Заеду в Юнусабад по пути, звони заранее",
    "departure_time": "2025-07-01T18:30:00",
    "price_per_seat": 15000,
    "seats_available": 3
}
```

### Забронировать место
```
POST http://localhost:8000/api/bookings/create/
Content-Type: application/json

{
    "offer_id": 1,
    "passenger_name": "Санжар",
    "passenger_phone": "+998901112233",
    "seats_requested": 1,
    "note": "Буду у входа в метро"
}
```

### Поиск рейсов
```
GET http://localhost:8000/api/rides/?to=Сергели
GET http://localhost:8000/api/rides/?search=Куйлюк&ordering=price_per_seat
```

---

## 🤖 BASE_URL для Android

```kotlin
// В Android (Retrofit) — замени IP на свой!
const val BASE_URL = "http://192.168.1.100:8000/api/"
```

Узнать свой IP:
- **Windows**: `ipconfig` → IPv4-адрес
- **Mac/Linux**: `ifconfig` → inet под Wi-Fi интерфейсом

---

## 🗄️ Модели базы данных

### Driver (Таксист)
| Поле | Тип | Описание |
|------|-----|----------|
| `name` | CharField | Имя |
| `phone` | CharField (unique) | Телефон |
| `telegram_username` | CharField | @username в Telegram |
| `car_model` | CharField | Марка машины |
| `car_color` | CharField | Цвет |
| `car_plate` | CharField | Гос. номер |
| `rating` | Decimal | Рейтинг (0-5) |
| `total_rides` | Integer | Всего поездок |

### RideOffer (Объявление)
| Поле | Тип | Описание |
|------|-----|----------|
| `driver` | FK → Driver | Таксист |
| `from_city` | CharField | Откуда |
| `to_district` | CharField | Куда (посёлок/район) |
| `departure_time` | DateTime | Время отправления |
| `price_per_seat` | Integer | Цена за место (сум) |
| `seats_available` | SmallInt | Свободных мест |
| `status` | Choice | active/booked/done/cancelled |

### Booking (Бронирование)
| Поле | Тип | Описание |
|------|-----|----------|
| `offer` | FK → RideOffer | Объявление |
| `passenger_name` | CharField | Имя пассажира |
| `passenger_phone` | CharField | Телефон |
| `seats_requested` | SmallInt | Кол-во мест |
| `status` | Choice | pending/confirmed/cancelled |
| `note` | TextField | Комментарий |
