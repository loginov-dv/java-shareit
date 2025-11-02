package ru.practicum.shareit.server.exception;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExceptionConstants {
    public static final String USER_NOT_FOUND_BY_ID = "Пользователь с id = %d не найден";
    public static final String EMAIL_CONFLICT = "Этот email уже используется";

    public static final String ITEM_NOT_FOUND_BY_ID = "Предмет с id = %d не найден";

    public static final String BOOKING_NOT_FOUND_BY_ID = "Бронирование с id = %d не найдено";
    public static final String INVALID_BOOKING_STATE = "Некорректное значение статуса для запроса бронирований";
}
