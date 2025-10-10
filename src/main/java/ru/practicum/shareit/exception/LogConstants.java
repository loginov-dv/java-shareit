package ru.practicum.shareit.exception;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LogConstants {
    public static final String USER_NOT_FOUND_BY_ID = "Пользователь с id = {} не найден";
    public static final String EMAIL_CONFLICT = "email = {} уже используется";
    public static final String ITEM_NOT_FOUND_BY_ID = "Предмет с id = {} не найден";
}
