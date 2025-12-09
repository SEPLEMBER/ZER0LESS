package com.aidinhut.simpletextcrypt;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.LinkedHashSet;

/**
 * Crypter2 — простая обфускация: заменяет печатные ASCII (32..126) на заранее заданные
 * кандзи (95 символов). Обратимая операция.
 *
 * Поддерживает интерфейс:
 *   String encrypt(String key, String input)
 *   String decrypt(String key, String input)
 *
 * Параметр key не используется (оставлен для совместимости с Crypter).
 *
 * Бросает IllegalArgumentException при обнаружении неподдерживаемых символов.
 */
public final class Crypter2 {

    private Crypter2() { /* static only */ }

    // 95 кандзи, соответствуют ASCII 32..126 (space .. ~)
    private static final String[] KANJI = {
            "日","本","人","大","小","中","山","川","田","目",
            "耳","口","手","足","力","水","火","土","風","空",
            "海","天","心","愛","学","校","言","語","文","書",
            "話","行","来","見","食","飲","車","電","駅","家",
            "男","女","子","年","時","分","秒","新","古","長",
            "短","高","低","明","暗","赤","青","白","黒","金",
            "銀","銅","王","魚","鳥","犬","猫","虫","花","草",
            "林","森","石","走","立","座","起","泳","歩","歌",
            "泣","笑","喜","怒","怖","旅","宿","室","庭","店",
            "村","町","都","市","県"
    };

    // Immutable maps for fast lookup
    private static final Map<Character, String> CHAR_TO_KANJI;
    private static final Map<String, Character> KANJI_TO_CHAR;

    static {
        Map<Character, String> forward = new LinkedHashMap<>(95);
        Map<String, Character> backward = new HashMap<>(95);

        int ascii = 32; // space
        for (int i = 0; i < KANJI.length; i++, ascii++) {
            char ch = (char) ascii;
            String kanji = KANJI[i];
            forward.put(ch, kanji);
            backward.put(kanji, ch);
        }

        CHAR_TO_KANJI = Collections.unmodifiableMap(forward);
        KANJI_TO_CHAR = Collections.unmodifiableMap(backward);
    }

    /**
     * Зашифровать / запаковать строку, заменяя поддерживаемые ASCII символы на кандзи.
     * @param key параметр для совместимости (не используется)
     * @param input входящая строка (ожидаются символы с кодами 32..126)
     * @return строка, содержащая кандзи
     * @throws IllegalArgumentException если найдены неподдерживаемые символы
     */
    public static String encrypt(String key, String input) {
        if (input == null || input.isEmpty()) return input == null ? null : "";

        StringBuilder sb = new StringBuilder(input.length() * 2); // примерный размер
        Set<Character> unsupported = new LinkedHashSet<>();

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            String mapped = CHAR_TO_KANJI.get(c);
            if (mapped != null) {
                sb.append(mapped);
            } else {
                unsupported.add(c);
            }
        }

        if (!unsupported.isEmpty()) {
            // Ограничиваем вывод списка символов (во избежание слишком длинных сообщений)
            StringBuilder sample = new StringBuilder();
            int count = 0;
            for (Character uc : unsupported) {
                if (count++ > 20) { sample.append("…"); break; }
                if (sample.length() > 0) sample.append(' ');
                sample.append(uc);
            }
            throw new IllegalArgumentException("Crypter2.encrypt: обнаружены неподдерживаемые символы: " + sample.toString());
        }

        return sb.toString();
    }

    /**
     * Расшифровать / распаковать строку с кандзи обратно в ASCII.
     * @param key параметр для совместимости (не используется)
     * @param input входящая строка, содержащая кандзи
     * @return восстановленная ASCII-строка
     * @throws IllegalArgumentException если найдены неподдерживаемые иероглифы
     */
    public static String decrypt(String key, String input) {
        if (input == null || input.isEmpty()) return input == null ? null : "";

        StringBuilder sb = new StringBuilder(input.length()); // минимальный размер
        Set<String> unsupported = new LinkedHashSet<>();

        for (int i = 0; i < input.length(); i++) {
            String ch = String.valueOf(input.charAt(i)); // каждый кандзи — один UTF-16 code unit здесь
            Character mapped = KANJI_TO_CHAR.get(ch);
            if (mapped != null) {
                sb.append(mapped.charValue());
            } else {
                unsupported.add(ch);
            }
        }

        if (!unsupported.isEmpty()) {
            StringBuilder sample = new StringBuilder();
            int count = 0;
            for (String uc : unsupported) {
                if (count++ > 20) { sample.append("…"); break; }
                if (sample.length() > 0) sample.append(' ');
                sample.append(uc);
            }
            throw new IllegalArgumentException("Crypter2.decrypt: обнаружены неподдерживаемые иероглифы: " + sample.toString());
        }

        return sb.toString();
    }

    /**
     * Возвращает readonly-отображение ASCII->канзи (для отладки).
     */
    public static Map<Character, String> getCharToKanjiMap() {
        return CHAR_TO_KANJI;
    }

    /**
     * Возвращает readonly-отображение канзи->ASCII (для отладки).
     */
    public static Map<String, Character> getKanjiToCharMap() {
        return KANJI_TO_CHAR;
    }
}
