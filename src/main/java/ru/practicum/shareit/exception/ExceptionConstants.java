package ru.practicum.shareit.exception;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExceptionConstants {
    public static final String USER_NOT_FOUND_BY_ID = "Пользователь с id = %d не найден";
    public static final String EMAIL_CONFLICT = "Этот email уже используется";

    public static final String ITEM_NOT_FOUND_BY_ID = "Предмет с id = %d не найден";

    public static final String NO_ACCESS = "Нет доступа";
    public static final String NO_ACCESS_FOR_BOOKING_APPROVAL = "Нет доступа на изменение статуса бронирования";
    public static final String NO_ACCESS_TO_VIEW_BOOKING = "Нет доступа на просмотр бронирования";

    public static final String INCORRECT_BOOKING_DATES_ORDER = "Дата окончания бронирования должна быть " +
            "после даты начала бронирования";
    public static final String START_DATE_EQUALS_TO_END_DATE = "Дата окончания бронирования не может " +
            "совпадать с датой начала бронирования";
    public static final String START_DATE_IN_THE_PAST = "Дата начала бронирования не может быть в прошлом";
    public static final String END_DATE_IN_THE_PAST = "Дата окончания бронирования не может быть в прошлом";

    public static final String BOOKING_NOT_FOUND_BY_ID = "Бронирование с id = %d не найдено";

    public static final String INVALID_BOOKING_STATE = "Некорректное значение статуса для запроса бронирований";

    public static final String HAS_NO_COMPLETED_BOOKINGS = "Невозможно оставить комментарий (нет завершённой аренды)";

    public static final String ITEM_NOT_AVAILABLE = "Предмет недоступен для бронирования";
}
