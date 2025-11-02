package ru.practicum.shareit.gateway.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RandomUtils {
    public static String createName() {
        return createName(10);
    }

    public static String createName(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        int charsLength = chars.length();
        int counter = 0;
        String result = "";

        while (counter < length) {
            result += chars.charAt((int)Math.round(Math.random() * (charsLength - 1)));
            counter++;
        }

        return result;
    }
}
