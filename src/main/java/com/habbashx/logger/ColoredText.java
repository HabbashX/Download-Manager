package com.habbashx.logger;

/**
 * The ColoredText class provides ANSI color codes as constants to style text output on supported terminals.
 * These constants can be used to add color to console or terminal text.
 *
 * The class contains pre-defined colors, such as red, bright red, bright yellow, lime green, and gray.
 * It also includes a reset code to revert the text formatting back to default.
 */
class ColoredText {

    public static final String RESET = "\u001b[0m";

    public static final String RED = "\u001b[31m";

    public static final String BRIGHT_RED = "\u001b[31;1m";

    public static final String BRIGHT_YELLOW = "\u001b[33;1m";

    public static final String LIME_GREEN = "\u001b[38;5;154m";

    public static final String GRAY = "\u001b[90m";

}