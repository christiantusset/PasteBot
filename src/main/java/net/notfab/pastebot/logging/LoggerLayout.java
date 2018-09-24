package net.notfab.pastebot.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.LayoutBase;

import java.util.Calendar;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Copyright (C) Fabricio20 (https://github.com/Fabricio20)
 * Created by Fabricio20 on 2018-01-07.
 */
public class LoggerLayout extends LayoutBase<ILoggingEvent> {

    private final Map<ConsoleColor, String> replacements = new EnumMap<>(ConsoleColor.class);
    private final Calendar calendar;

    public LoggerLayout() {
        replacements.put(ConsoleColor.BLACK, "\u001B[0;30;22m");
        replacements.put(ConsoleColor.DARK_BLUE, "\u001B[0;34;22m");
        replacements.put(ConsoleColor.DARK_GREEN, "\u001B[0;32;22m");
        replacements.put(ConsoleColor.DARK_AQUA, "\u001B[0;36;22m");
        replacements.put(ConsoleColor.DARK_RED, "\u001B[0;31;22m");
        replacements.put(ConsoleColor.DARK_PURPLE, "\u001B[0;35;22m");
        replacements.put(ConsoleColor.GOLD, "\u001B[0;33;22m");
        replacements.put(ConsoleColor.GRAY, "\u001B[0;37;22m");
        replacements.put(ConsoleColor.DARK_GRAY, "\u001B[0;30;1m");
        replacements.put(ConsoleColor.BLUE, "\u001B[0;34;1m");
        replacements.put(ConsoleColor.GREEN, "\u001B[0;32;1m");
        replacements.put(ConsoleColor.AQUA, "\u001B[0;36;1m");
        replacements.put(ConsoleColor.RED, "\u001B[0;31;1m");
        replacements.put(ConsoleColor.LIGHT_PURPLE, "\u001B[0;35;1m");
        replacements.put(ConsoleColor.YELLOW, "\u001B[0;33;1m");
        replacements.put(ConsoleColor.WHITE, "\u001B[0;37;1m");
        replacements.put(ConsoleColor.MAGIC, "\u001B[5m");
        replacements.put(ConsoleColor.BOLD, "\u001B[21m");
        replacements.put(ConsoleColor.STRIKETHROUGH, "\u001B[9m");
        replacements.put(ConsoleColor.UNDERLINE, "\u001B[4m");
        replacements.put(ConsoleColor.ITALIC, "\u001B[3m");
        replacements.put(ConsoleColor.RESET, "\u001B[m");
        calendar = Calendar.getInstance();
    }

    @Override
    public String doLayout(ILoggingEvent event) {
        ConsoleColor color;
        String prefix;
        if (event.getLevel() == Level.TRACE) {
            prefix = "&a[&fTRACE&a] ";
            color = ConsoleColor.GREEN;
        } else if (event.getLevel() == Level.DEBUG) {
            prefix = "&d[&fDEBUG&d] ";
            color = ConsoleColor.LIGHT_PURPLE;
        } else if (event.getLevel() == Level.INFO) {
            prefix = "&b[&fINFO&b] ";
            color = ConsoleColor.AQUA;
        } else if (event.getLevel() == Level.WARN) {
            prefix = "&6[&fWARN&6] ";
            color = ConsoleColor.GOLD;
        } else if (event.getLevel() == Level.ERROR) {
            prefix = "&c[&fERROR&c] ";
            color = ConsoleColor.RED;
        } else {
            prefix = "&7[" + event.getLevel().toString() + "] ";
            color = ConsoleColor.WHITE;
        }

        String className = event.getLoggerName();
        className = className.substring(className.lastIndexOf(".") + 1, className.length());

        prefix += "&b[&f" + className + "&b] &f{&a" + event.getThreadName() + "&f} ";

        calendar.setTimeInMillis(System.currentTimeMillis());
        String time = "&7" + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + "&r ";

        String throwable = null;
        if (event.getThrowableProxy() != null && event.getThrowableProxy().getMessage() != null) {
            throwable = ThrowableProxyUtil.asString(event.getThrowableProxy());
        }

        String message = time + prefix + ("&" + color.getChar()) + event.getFormattedMessage() + "\n";
        if (throwable != null) {
            message += throwable;
        }
        return translateAlternateColorCodes(ConsoleColor.translateAlternateColorCodes('&', message));
    }

    private String translateAlternateColorCodes(String message) {
        for (ConsoleColor color : ConsoleColor.values()) {
            if (replacements.containsKey(color)) {
                message = message.replaceAll("(?i)" + color.toString(), replacements.get(color));
            } else {
                message = message.replaceAll("(?i)" + color.toString(), "");
            }
        }
        return message + replacements.get(ConsoleColor.RESET);
    }

    // This is just ChatColor from Bukkit/Spigot
    public enum ConsoleColor {
        /**
         * Represents black
         */
        BLACK('0', 0x00),
        /**
         * Represents dark blue
         */
        DARK_BLUE('1', 0x1),
        /**
         * Represents dark green
         */
        DARK_GREEN('2', 0x2),
        /**
         * Represents dark blue (aqua)
         */
        DARK_AQUA('3', 0x3),
        /**
         * Represents dark red
         */
        DARK_RED('4', 0x4),
        /**
         * Represents dark purple
         */
        DARK_PURPLE('5', 0x5),
        /**
         * Represents gold
         */
        GOLD('6', 0x6),
        /**
         * Represents gray
         */
        GRAY('7', 0x7),
        /**
         * Represents dark gray
         */
        DARK_GRAY('8', 0x8),
        /**
         * Represents blue
         */
        BLUE('9', 0x9),
        /**
         * Represents green
         */
        GREEN('a', 0xA),
        /**
         * Represents aqua
         */
        AQUA('b', 0xB),
        /**
         * Represents red
         */
        RED('c', 0xC),
        /**
         * Represents light purple
         */
        LIGHT_PURPLE('d', 0xD),
        /**
         * Represents yellow
         */
        YELLOW('e', 0xE),
        /**
         * Represents white
         */
        WHITE('f', 0xF),
        /**
         * Represents magical characters that change around randomly
         */
        MAGIC('k', 0x10, true),
        /**
         * Makes the text bold.
         */
        BOLD('l', 0x11, true),
        /**
         * Makes a line appear through the text.
         */
        STRIKETHROUGH('m', 0x12, true),
        /**
         * Makes the text appear underlined.
         */
        UNDERLINE('n', 0x13, true),
        /**
         * Makes the text italic.
         */
        ITALIC('o', 0x14, true),
        /**
         * Resets all previous chat colors or formats.
         */
        RESET('r', 0x15);

        /**
         * The special character which prefixes all chat colour codes. Use this if
         * you need to dynamically convert colour codes from your custom format.
         */
        public static final char COLOR_CHAR = '\u00A7';
        private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)" + String.valueOf(COLOR_CHAR) + "[0-9A-FK-OR]");

        private final int intCode;
        private final char code;
        private final boolean isFormat;
        private final String toString;
        private final static Map<Integer, ConsoleColor> BY_ID = new HashMap<>();
        private final static Map<Character, ConsoleColor> BY_CHAR = new HashMap<>();

        ConsoleColor(char code, int intCode) {
            this(code, intCode, false);
        }

        ConsoleColor(char code, int intCode, boolean isFormat) {
            this.code = code;
            this.intCode = intCode;
            this.isFormat = isFormat;
            this.toString = new String(new char[]{COLOR_CHAR, code});
        }

        /**
         * Gets the char value associated with this color
         *
         * @return A char value of this color code
         */
        public char getChar() {
            return code;
        }

        @Override
        public String toString() {
            return toString;
        }

        /**
         * Checks if this code is a format code as opposed to a color code.
         */
        public boolean isFormat() {
            return isFormat;
        }

        /**
         * Checks if this code is a color code as opposed to a format code.
         */
        public boolean isColor() {
            return !isFormat && this != RESET;
        }

        /**
         * Gets the color represented by the specified color code
         *
         * @param code Code to check
         * @return Associative ConsoleColor with the given code,
         * or null if it doesn't exist
         */
        public static ConsoleColor getByChar(char code) {
            return BY_CHAR.get(code);
        }

        /**
         * Gets the color represented by the specified color code
         *
         * @param code Code to check
         * @return Associative ConsoleColor with the given code,
         * or null if it doesn't exist
         */
        public static ConsoleColor getByChar(String code) {
            //Validate.notNull(code, "Code cannot be null");
            //Validate.isTrue(code.length() > 0, "Code must have at least one char");

            return BY_CHAR.get(code.charAt(0));
        }

        /**
         * Strips the given message of all color codes
         *
         * @param input String to strip of color
         * @return A copy of the input string, without any coloring
         */
        public static String stripColor(final String input) {
            if (input == null) {
                return null;
            }

            return STRIP_COLOR_PATTERN.matcher(input).replaceAll("");
        }

        /**
         * Translates a string using an alternate color code character into a
         * string that uses the internal ChatColor.COLOR_CODE color code
         * character. The alternate color code character will only be replaced if
         * it is immediately followed by 0-9, A-F, a-f, K-O, k-o, R or r.
         *
         * @param altColorChar    The alternate color code character to replace. Ex: &amp;
         * @param textToTranslate Text containing the alternate color code character.
         * @return Text containing the ChatColor.COLOR_CODE color code character.
         */
        public static String translateAlternateColorCodes(char altColorChar, String textToTranslate) {
            char[] b = textToTranslate.toCharArray();
            for (int i = 0; i < b.length - 1; i++) {
                if (b[i] == altColorChar && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(b[i + 1]) > -1) {
                    b[i] = ConsoleColor.COLOR_CHAR;
                    b[i + 1] = Character.toLowerCase(b[i + 1]);
                }
            }
            return new String(b);
        }

        /**
         * Gets the ChatColors used at the end of the given input string.
         *
         * @param input Input string to retrieve the colors from.
         * @return Any remaining ChatColors to pass onto the next line.
         */
        public static String getLastColors(String input) {
            StringBuilder result = new StringBuilder();
            int length = input.length();

            // Search backwards from the end as it is faster
            for (int index = length - 1; index > -1; index--) {
                char section = input.charAt(index);
                if (section == COLOR_CHAR && index < length - 1) {
                    char c = input.charAt(index + 1);
                    ConsoleColor color = getByChar(c);

                    if (color != null) {
                        result.insert(0, color.toString());

                        // Once we find a color or reset we can stop searching
                        if (color.isColor() || color.equals(RESET)) {
                            break;
                        }
                    }
                }
            }

            return result.toString();
        }

        static {
            for (ConsoleColor color : values()) {
                BY_ID.put(color.intCode, color);
                BY_CHAR.put(color.code, color);
            }
        }

    }

}
