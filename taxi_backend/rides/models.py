"""
rides/models.py — Модели базы данных
"""
from django.db import models
from django.utils import timezone


class Driver(models.Model):
    """
    Таксист. Регистрируется один раз, потом создаёт объявления.
    """
    name = models.CharField(max_length=100, verbose_name='Имя')
    phone = models.CharField(max_length=20, unique=True, verbose_name='Телефон')
    card_number = models.CharField(
        max_length=20, blank=True, null=True,
        verbose_name='Номер карты (Humo/Uzcard)',
        help_text='Только цифры, например: 8600123412341234'
    )
    car_model = models.CharField(max_length=100, blank=True, verbose_name='Марка машины')
    car_color = models.CharField(max_length=50, blank=True, verbose_name='Цвет машины')
    car_plate = models.CharField(max_length=20, blank=True, verbose_name='Гос. номер')
    rating = models.DecimalField(
        max_digits=3, decimal_places=2,
        default=5.00, verbose_name='Рейтинг'
    )
    total_rides = models.PositiveIntegerField(default=0, verbose_name='Поездок всего')
    is_active = models.BooleanField(default=True, verbose_name='Активен')
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        verbose_name = 'Таксист'
        verbose_name_plural = 'Таксисты'
        ordering = ['-rating', '-total_rides']

    def __str__(self):
        return f'{self.name} ({self.phone})'


class RideOffer(models.Model):
    """
    Объявление таксиста — «Еду из A в B в такое-то время».
    Это главная сущность приложения.
    """
    STATUS_CHOICES = [
        ('active', 'Активно'),
        ('booked', 'Забронировано'),
        ('done', 'Завершено'),
        ('cancelled', 'Отменено'),
    ]

    driver = models.ForeignKey(
        Driver, on_delete=models.CASCADE,
        related_name='offers', verbose_name='Таксист'
    )

    # Маршрут
    from_city = models.CharField(max_length=150, verbose_name='Откуда (город/район)')
    to_district = models.CharField(max_length=150, verbose_name='Куда (посёлок/район)')
    route_description = models.TextField(
        blank=True,
        verbose_name='Описание маршрута',
        help_text='Доп. информация: заезды, промежуточные остановки и т.д.'
    )

    # Время
    departure_time = models.DateTimeField(verbose_name='Время отправления')

    # Цена и места
    price_per_seat = models.PositiveIntegerField(verbose_name='Цена за место (сум)')
    seats_available = models.PositiveSmallIntegerField(
        default=3, verbose_name='Свободных мест'
    )

    # Статус
    status = models.CharField(
        max_length=20, choices=STATUS_CHOICES,
        default='active', verbose_name='Статус'
    )

    # Мета
    created_at = models.DateTimeField(auto_now_add=True, verbose_name='Создано')
    updated_at = models.DateTimeField(auto_now=True, verbose_name='Обновлено')

    class Meta:
        verbose_name = 'Объявление о поездке'
        verbose_name_plural = 'Объявления о поездках'
        ordering = ['departure_time']

    def __str__(self):
        return f'{self.from_city} → {self.to_district} | {self.departure_time:%d.%m %H:%M} | {self.driver.name}'

    @property
    def is_available(self):
        return self.status == 'active' and self.seats_available > 0 and self.departure_time > timezone.now()


class Booking(models.Model):
    """
    Заявка пассажира на конкретное объявление.
    Создаётся когда пользователь нажал «Хочу ехать».
    """
    STATUS_CHOICES = [
        ('pending', 'Ожидает подтверждения'),
        ('confirmed', 'Подтверждено'),
        ('cancelled', 'Отменено'),
    ]

    offer = models.ForeignKey(
        RideOffer, on_delete=models.CASCADE,
        related_name='bookings', verbose_name='Объявление'
    )
    passenger_name = models.CharField(max_length=100, verbose_name='Имя пассажира')
    passenger_phone = models.CharField(max_length=20, verbose_name='Телефон пассажира')
    seats_requested = models.PositiveSmallIntegerField(
        default=1, verbose_name='Запрошено мест'
    )
    status = models.CharField(
        max_length=20, choices=STATUS_CHOICES,
        default='pending', verbose_name='Статус'
    )
    note = models.TextField(blank=True, verbose_name='Комментарий пассажира')
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        verbose_name = 'Бронирование'
        verbose_name_plural = 'Бронирования'
        ordering = ['-created_at']

    def __str__(self):
        return f'{self.passenger_name} → {self.offer}'
