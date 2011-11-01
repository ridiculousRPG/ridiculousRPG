package com.madthrax.ridiculousRPG.ui;

import java.util.Locale;

public class TextLoader {
	public static String loadText(int textId, Locale locale, Object... args) {
		return textId==1 ? String.format(locale, "Pause\nbäh ÄFPß: \ntest Aläx bla blub\nZeile 3\nZeilö 4\nZeile 5 public static String loadText(int textId, Locale locale, Object... args) { public static String loadText(int textId, Locale locale, Object... args) { public static String loadText(int textId, Locale locale, Object... args) {", args)
				: String.format(locale, "ÄFPß: %d\ntest Aläx bla blub\nZeile 3\nZeilö 4\nZeile 5", args);
	}
}
