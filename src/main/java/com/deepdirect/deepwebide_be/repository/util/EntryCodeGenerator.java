package com.deepdirect.deepwebide_be.repository.util;

import com.deepdirect.deepwebide_be.global.exception.ErrorCode;
import com.deepdirect.deepwebide_be.global.exception.GlobalException;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

public class EntryCodeGenerator {

    private static final int CODE_LENGTH = 8;
    private static final int MAX_TRY_COUNT = 10;
    private static final String CHAR_POOL = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    private EntryCodeGenerator() {
        throw new IllegalStateException("Utility class");
    }

    public static String generateUniqueCode(Function<String, Boolean> isDuplicate) {
        for (int i = 0; i < MAX_TRY_COUNT; i++) {
            String code = generateRandomCode(CODE_LENGTH);
            if (!isDuplicate.apply(code)) {
                return code;
            }
        }
        throw new GlobalException(ErrorCode.ENTRY_CODE_GENERATION_FAILED);
    }

    private static String generateRandomCode(int length) {
        StringBuilder sb = new StringBuilder(length);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < length; i++) {
            sb.append(CHAR_POOL.charAt(random.nextInt(CHAR_POOL.length())));
        }
        return sb.toString();
    }

}
