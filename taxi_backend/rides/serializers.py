"""
rides/serializers.py — Сериализаторы (Python-объект ↔ JSON)
"""
from rest_framework import serializers
from .models import Driver, RideOffer, Booking
from django.utils import timezone


class DriverSerializer(serializers.ModelSerializer):
    """
    Используется внутри RideOfferSerializer для отображения инфо о водителе.
    """
    class Meta:
        model = Driver
        fields = [
            'id', 'name', 'phone', 'card_number',
            'car_model', 'car_color', 'car_plate',
            'rating', 'total_rides',
        ]


class DriverCreateSerializer(serializers.ModelSerializer):
    """
    Регистрация нового водителя.
    """
    class Meta:
        model = Driver
        fields = [
            'name', 'phone', 'card_number',
            'car_model', 'car_color', 'car_plate',
        ]

    def validate_phone(self, value):
        # Оставляем только то, что может быть в номере
        cleaned = ''.join(filter(lambda c: c.isdigit() or c == '+', value))
        if not cleaned.replace('+', '').isdigit():
             raise serializers.ValidationError('Номер телефона должен содержать только цифры.')
        if len(cleaned) < 9:
            raise serializers.ValidationError('Слишком короткий номер телефона (минимум 9 цифр).')
        return cleaned

    def validate_card_number(self, value):
        if not value:
            return value
        # Только цифры
        cleaned = ''.join(filter(lambda c: c.isdigit(), value))
        if len(cleaned) != 16:
            raise serializers.ValidationError('Номер карты должен состоять из 16 цифр.')
        return cleaned


class RideOfferSerializer(serializers.ModelSerializer):
    """
    Полная карточка объявления — для списка и детального просмотра.
    """
    driver = DriverSerializer(read_only=True)
    is_available = serializers.ReadOnlyField()
    # Для Android: camelCase через source
    fromCity = serializers.CharField(source='from_city', read_only=True)
    toDistrict = serializers.CharField(source='to_district', read_only=True)
    pricePerSeat = serializers.IntegerField(source='price_per_seat', read_only=True)
    seatsAvailable = serializers.IntegerField(source='seats_available', read_only=True)
    departureTime = serializers.DateTimeField(source='departure_time', read_only=True)
    routeDescription = serializers.CharField(source='route_description', read_only=True)

    class Meta:
        model = RideOffer
        fields = [
            'id',
            'driver',
            # snake_case (для Postman/браузера)
            'from_city', 'to_district', 'route_description',
            'departure_time', 'price_per_seat', 'seats_available',
            # camelCase (удобно для Android с Gson)
            'fromCity', 'toDistrict', 'routeDescription',
            'departureTime', 'pricePerSeat', 'seatsAvailable',
            # общие
            'status', 'is_available', 'created_at',
        ]


class RideOfferCreateSerializer(serializers.ModelSerializer):
    """
    Создание нового объявления таксистом.
    Принимает driver_id, остальные поля — маршрут и время.
    """
    driver_id = serializers.IntegerField(write_only=True)

    class Meta:
        model = RideOffer
        fields = [
            'driver_id',
            'from_city', 'to_district', 'route_description',
            'departure_time', 'price_per_seat', 'seats_available',
        ]

    def validate_driver_id(self, value):
        try:
            driver = Driver.objects.get(id=value, is_active=True)
        except Driver.DoesNotExist:
            raise serializers.ValidationError('Таксист с таким ID не найден или неактивен.')
        return value

    def validate_departure_time(self, value):
        if value <= timezone.now():
            raise serializers.ValidationError('Время отправления должно быть в будущем.')
        return value

    def validate_price_per_seat(self, value):
        if value <= 0:
            raise serializers.ValidationError('Цена должна быть больше нуля.')
        return value

    def validate_seats_available(self, value):
        if value < 1 or value > 8:
            raise serializers.ValidationError('Количество мест: от 1 до 8.')
        return value

    def create(self, validated_data):
        driver_id = validated_data.pop('driver_id')
        driver = Driver.objects.get(id=driver_id)
        return RideOffer.objects.create(driver=driver, **validated_data)


class BookingSerializer(serializers.ModelSerializer):
    """
    Бронирование — пассажир оставляет заявку на поездку.
    """
    offer_id = serializers.IntegerField(write_only=True)
    offer_summary = serializers.SerializerMethodField(read_only=True)

    class Meta:
        model = Booking
        fields = [
            'id', 'offer_id', 'offer_summary',
            'passenger_name', 'passenger_phone',
            'seats_requested', 'status', 'note', 'created_at',
        ]
        read_only_fields = ['id', 'status', 'created_at', 'offer_summary']

    def get_offer_summary(self, obj):
        return {
            'id': obj.offer.id,
            'from': obj.offer.from_city,
            'to': obj.offer.to_district,
            'departure_time': obj.offer.departure_time,
            'driver_name': obj.offer.driver.name,
            'driver_phone': obj.offer.driver.phone,
        }

    def validate_offer_id(self, value):
        try:
            offer = RideOffer.objects.get(id=value)
        except RideOffer.DoesNotExist:
            raise serializers.ValidationError('Объявление не найдено.')
        if not offer.is_available:
            raise serializers.ValidationError('Это объявление больше недоступно.')
        return value

    def validate(self, data):
        offer_id = data.get('offer_id')
        seats = data.get('seats_requested', 1)
        try:
            offer = RideOffer.objects.get(id=offer_id)
            if offer.seats_available < seats:
                raise serializers.ValidationError({
                    'seats_requested': f'Доступно только {offer.seats_available} мест(а).'
                })
        except RideOffer.DoesNotExist:
            pass
        return data

    def create(self, validated_data):
        offer_id = validated_data.pop('offer_id')
        offer = RideOffer.objects.get(id=offer_id)
        booking = Booking.objects.create(offer=offer, **validated_data)
        # Уменьшаем количество свободных мест
        offer.seats_available -= booking.seats_requested
        if offer.seats_available == 0:
            offer.status = 'booked'
        offer.save()
        return booking
