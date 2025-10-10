package ru.practicum.shareit.exception;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExceptionConstants {
    public static final String USER_NOT_FOUND_BY_ID = "Пользователь с id = %d не найден";
    public static final String EMAIL_CONFLICT = "Этот email уже используется";
    public static final String ITEM_NOT_FOUND_BY_ID = "Предмет с id = %d не найден";
    public static final String NO_ACCESS_FOR_EDIT = "Редактировать вещь может только её владелец";
}
