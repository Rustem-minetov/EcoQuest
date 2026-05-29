"""
seed_data.py — Создаёт тестовых водителей и рейсы
Запуск: python manage.py shell < seed_data.py
"""
import os, sys, django
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'config.settings')

# Ensure Django is set up
if not django.conf.settings.configured:
    django.setup()

from django.utils import timezone
from datetime import timedelta
from rides.models import Driver, RideOffer

print("🚕 Создаю тестовые данные...")

# ─── Водители ────────────────────────────────────
d1, _ = Driver.objects.get_or_create(
    phone='+998901234567',
    defaults={
        'name': 'Азамат',
        'telegram_username': 'azamat_driver',
        'car_model': 'Cobalt',
        'car_color': 'Белый',
        'car_plate': '01 A 123 BA',
        'rating': 4.8,
        'total_rides': 52,
    }
)

d2, _ = Driver.objects.get_or_create(
    phone='+998913456789',
    defaults={
        'name': 'Сарвар',
        'telegram_username': 'sarvar_kungrad',
        'car_model': 'Lacetti',
        'car_color': 'Серый',
        'car_plate': '01 B 456 BA',
        'rating': 4.5,
        'total_rides': 30,
    }
)

d3, _ = Driver.objects.get_or_create(
    phone='+998935552211',
    defaults={
        'name': 'Даулет',
        'telegram_username': 'daulet_nukus',
        'car_model': 'Nexia 3',
        'car_color': 'Чёрный',
        'car_plate': '01 C 789 BA',
        'rating': 4.9,
        'total_rides': 120,
    }
)

print(f"  ✅ Водители: {Driver.objects.count()}")

# ─── Рейсы ────────────────────────────────────────
now = timezone.now()

rides_data = [
    {
        'driver': d1,
        'from_city': 'Nukus shahri',
        'to_district': "Mo'ynoq",
        'route_description': 'Выезд в 14:00, еду через Кунград',
        'departure_time': now + timedelta(hours=2),
        'price_per_seat': 50000,
        'seats_available': 3,
    },
    {
        'driver': d2,
        'from_city': 'Nukus shahri',
        'to_district': "Qo'ng'irot",
        'route_description': 'Машина пустая, выезжаю скоро',
        'departure_time': now + timedelta(hours=1),
        'price_per_seat': 35000,
        'seats_available': 2,
    },
    {
        'driver': d3,
        'from_city': 'Chimboy',
        'to_district': 'Nukus shahri',
        'route_description': 'Ежедневные рейсы утром',
        'departure_time': now + timedelta(hours=3),
        'price_per_seat': 20000,
        'seats_available': 4,
    },
    {
        'driver': d1,
        'from_city': 'Nukus shahri',
        'to_district': 'Xo\'jayli',
        'route_description': 'Заеду на базар по пути',
        'departure_time': now + timedelta(hours=5),
        'price_per_seat': 15000,
        'seats_available': 3,
    },
    {
        'driver': d2,
        'from_city': "Qo'ng'irot",
        'to_district': 'Nukus shahri',
        'route_description': 'Обратный рейс, вечером',
        'departure_time': now + timedelta(hours=8),
        'price_per_seat': 35000,
        'seats_available': 4,
    },
]

created_count = 0
for data in rides_data:
    _, created = RideOffer.objects.get_or_create(
        driver=data['driver'],
        from_city=data['from_city'],
        to_district=data['to_district'],
        departure_time=data['departure_time'],
        defaults={
            'route_description': data['route_description'],
            'price_per_seat': data['price_per_seat'],
            'seats_available': data['seats_available'],
        }
    )
    if created:
        created_count += 1

print(f"  ✅ Рейсы: {RideOffer.objects.count()} (новых: {created_count})")
print("🎉 Готово!")
