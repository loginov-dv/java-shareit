package ru.practicum.shareit.exception;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LogConstants {
    public static final String USER_NOT_FOUND_BY_ID = "Пользователь с id = {} не найден";
    public static final String EMAIL_CONFLICT = "email = {} уже используется";

    public static final String ITEM_NOT_FOUND_BY_ID = "Предмет с id = {} не найден";

    public static final String BOOKING_NOT_FOUND_BY_ID = "Бронирование с id = {} не найдено";
    public static final String INVALID_BOOKING_STATE = "Некорректное значение статуса для запроса бронирований";
}
