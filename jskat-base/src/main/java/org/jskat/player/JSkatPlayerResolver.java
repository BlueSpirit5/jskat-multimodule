/**
 * JSkat - A skat program written in Java
 * by Jan Schäfer, Markus J. Luzius, Daniel Loreck and Sascha Laurien
 *
 * Version 0.14.0-SNAPSHOT
 * Copyright (C) 2013-11-02
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.jskat.player;

import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import org.reflections.Reflections;

/**
 * Resolves all implementations of {@link JSkatPlayer} that inherit from
 * {@link AbstractJSkatPlayer} and are not abstract
 */
public class JSkatPlayerResolver {

	public static final String HUMAN_PLAYER_CLASS = "org.jskat.gui.human.SwingHumanPlayer";

	public static final Set<String> EXCLUDED_PLAYER_CLASSES;

	public static final Set<String> UNIT_TEST_PLAYER_CLASSES;

	static {
		EXCLUDED_PLAYER_CLASSES = new HashSet<String>();
		EXCLUDED_PLAYER_CLASSES.add("org.jskat.ai.mjl.AIPlayerMJL");
		EXCLUDED_PLAYER_CLASSES
				.add("org.jskat.ai.algorithmic.AlgorithmicAIPlayer");

		UNIT_TEST_PLAYER_CLASSES = new HashSet<String>();
		UNIT_TEST_PLAYER_CLASSES.add("org.jskat.ai.test.UnitTestPlayer");
		UNIT_TEST_PLAYER_CLASSES.add("org.jskat.ai.test.RamschTestPlayer");
		UNIT_TEST_PLAYER_CLASSES.add("org.jskat.ai.test.NoBiddingTestPlayer");
		UNIT_TEST_PLAYER_CLASSES.add("org.jskat.ai.test.ExceptionTestPlayer");
		UNIT_TEST_PLAYER_CLASSES
				.add("org.jskat.ai.test.PlayNonPossessingCardTestPlayer");
		UNIT_TEST_PLAYER_CLASSES
				.add("org.jskat.ai.test.PlayNotAllowedCardTestPlayer");
	}

	/**
	 * Gets all class names including package names of AI player implementations
	 */
	public static Set<String> getAllAIPlayerImplementations() {

		Set<String> result = getAllImplementations();

		result.removeAll(EXCLUDED_PLAYER_CLASSES);
		result.removeAll(UNIT_TEST_PLAYER_CLASSES);

		return result;
	}

	private static Set<String> getAllImplementations() {
		Set<String> result = new HashSet<String>();
		Reflections reflections = new Reflections("org.jskat");

		Set<Class<? extends AbstractJSkatPlayer>> subTypes = reflections
				.getSubTypesOf(AbstractJSkatPlayer.class);
		for (Class<? extends AbstractJSkatPlayer> jskatPlayer : subTypes) {
			if (isNotAbstract(jskatPlayer) && isNotHumanPlayer(jskatPlayer)) {
				result.add(jskatPlayer.getName());
			}
		}
		return result;
	}

	private static boolean isNotHumanPlayer(
			Class<? extends AbstractJSkatPlayer> jskatPlayer) {
		return !HUMAN_PLAYER_CLASS.equals(jskatPlayer.getName());
	}

	private static boolean isNotAbstract(
			Class<? extends AbstractJSkatPlayer> jskatPlayer) {
		return !Modifier.isAbstract(jskatPlayer.getModifiers());
	}
}
