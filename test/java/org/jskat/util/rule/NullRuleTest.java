/**
 * JSkat - A skat program written in Java
 * by Jan Schäfer, Markus J. Luzius and Daniel Loreck
 *
 * Version 0.12.0
 * Copyright (C) 2013-05-09
 *
 * Licensed under the Apache License, Version 2.0. You may
 * obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jskat.util.rule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.jskat.AbstractJSkatTest;
import org.jskat.data.GameAnnouncement;
import org.jskat.data.GameAnnouncement.GameAnnouncementFactory;
import org.jskat.data.SkatGameData;
import org.jskat.data.Trick;
import org.jskat.util.Card;
import org.jskat.util.GameType;
import org.jskat.util.Player;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for NullRule
 */
public class NullRuleTest extends AbstractJSkatTest {

	private SkatGameData data;
	private GameAnnouncementFactory factory;

	private static SkatRule nullRules = SkatRuleFactory.getSkatRules(GameType.NULL);

	/**
	 * @see Before
	 */
	@Before
	public void initialize() {

		data = new SkatGameData();
		factory = GameAnnouncement.getFactory();
		factory.setGameType(GameType.NULL);
		data.setDeclarer(Player.FOREHAND);
	}

	private void playWinningTricks() {
		data.addTrick(new Trick(0, Player.FOREHAND));
		data.setTrickCard(Player.FOREHAND, Card.C7);
		data.setTrickCard(Player.MIDDLEHAND, Card.C8);
		data.setTrickCard(Player.REARHAND, Card.C9);
		data.setTrickWinner(0, Player.REARHAND);
		data.addTrick(new Trick(1, Player.REARHAND));
		data.setTrickCard(Player.REARHAND, Card.S8);
		data.setTrickCard(Player.FOREHAND, Card.S7);
		data.setTrickCard(Player.MIDDLEHAND, Card.S9);
		data.setTrickWinner(1, Player.MIDDLEHAND);
	}

	/**
	 * Checks @see NullRule#calcGameWon()
	 */
	@Test
	public void calcGameWon() {

		data.setAnnouncement(factory.getAnnouncement());
		playWinningTricks();
		data.calcResult();
		assertTrue(data.getResult().isWon());
	}

	/**
	 * Checks @see NullRule#calcGameWon()
	 */
	@Test
	public void calcGameLost() {

		data.setAnnouncement(factory.getAnnouncement());
		playWinningTricks();
		playLoosingTrick();
		data.calcResult();
		assertFalse(data.getResult().isWon());
	}

	/**
	 * Checks @see NullRule#calcGameResult()
	 */
	@Test
	public void calcGameResultGameWon() {

		data.setDeclarerPickedUpSkat(true);
		data.setAnnouncement(factory.getAnnouncement());
		playWinningTricks();
		data.calcResult();
		assertEquals(23, data.getResult().getGameValue());
	}

	/**
	 * Checks @see NullRule#calcGameResult()
	 */
	@Test
	public void calcGameResultGameWonHand() {

		factory.setHand(true);
		data.setAnnouncement(factory.getAnnouncement());
		playWinningTricks();
		data.calcResult();
		assertEquals(35, data.getResult().getGameValue());
	}

	/**
	 * Checks @see NullRule#calcGameResult()
	 */
	@Test
	public void calcGameResultGameWonOuvert() {

		data.setDeclarerPickedUpSkat(true);
		factory.setOuvert(Boolean.TRUE);
		data.setAnnouncement(factory.getAnnouncement());
		playWinningTricks();
		data.calcResult();
		assertEquals(46, data.getResult().getGameValue());
	}

	/**
	 * Checks @see NullRule#calcGameResult()
	 */
	@Test
	public void calcGameResultGameWonHandOuvert() {
		factory.setHand(true);
		factory.setOuvert(true);
		data.setAnnouncement(factory.getAnnouncement());
		playWinningTricks();
		data.calcResult();
		assertEquals(59, data.getResult().getGameValue());
	}

	/**
	 * Checks @see NullRule#calcGameResult()
	 */
	@Test
	public void calcGameResultGameLost() {

		data.setDeclarerPickedUpSkat(true);
		data.setAnnouncement(factory.getAnnouncement());
		playWinningTricks();
		playLoosingTrick();
		data.calcResult();
		assertEquals(-46, data.getResult().getGameValue());
	}

	private void playLoosingTrick() {
		data.addTrick(new Trick(2, Player.MIDDLEHAND));
		data.setTrickCard(Player.MIDDLEHAND, Card.H7);
		data.setTrickCard(Player.REARHAND, Card.H8);
		data.setTrickCard(Player.FOREHAND, Card.H9);
		data.setTrickWinner(2, Player.FOREHAND);
		data.getGameResult().setWon(nullRules.isGameWon(data));
	}

	/**
	 * Checks @see NullRule#calcGameResult()
	 */
	@Test
	public void calcGameResultGameLostHand() {
		factory.setHand(true);
		data.setAnnouncement(factory.getAnnouncement());
		playWinningTricks();
		playLoosingTrick();
		data.calcResult();
		assertEquals(-70, nullRules.calcGameResult(data));
	}

	/**
	 * Checks @see NullRule#calcGameResult()
	 */
	@Test
	public void calcGameResultGameLostOuvert() {

		data.setDeclarerPickedUpSkat(true);
		factory.setOuvert(Boolean.TRUE);
		data.setAnnouncement(factory.getAnnouncement());
		playWinningTricks();
		playLoosingTrick();
		data.calcResult();
		assertEquals(-92, nullRules.calcGameResult(data));
	}

	/**
	 * Checks @see NullRule#calcGameResult()
	 */
	@Test
	public void calcGameResultGameLostHandOuvert() {
		factory.setHand(true);
		factory.setOuvert(true);
		data.setAnnouncement(factory.getAnnouncement());
		playWinningTricks();
		playLoosingTrick();
		data.calcResult();
		assertEquals(-118, nullRules.calcGameResult(data));
	}

	/**
	 * Test for overbidding in null games
	 */
	@Test
	public void testOverbid() {

		data.setBidValue(24);
		data.setDeclarerPickedUpSkat(true);
		data.setAnnouncement(factory.getAnnouncement());
		playWinningTricks();
		data.calcResult();
		assertEquals(-46, data.getResult().getGameValue());
	}
}
