/*

@ShortLicense@

Authors: @JS@
         @MJL@

Released: @ReleaseDate@

 */

package jskat.control;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Observable;
import java.util.ResourceBundle;
import java.util.TimerTask;
import java.util.Vector;

import javax.swing.JOptionPane;

import jskat.data.BidStatus;
import jskat.data.GameAnnouncement;
import jskat.data.JSkatDataModel;
import jskat.data.JSkatOptions;
import jskat.data.SkatGameData;
import jskat.data.SkatTableOptions;
import jskat.data.Trick;
import jskat.gui.main.GameAnnounceDialog;
import jskat.gui.main.JSkatFrame;
import jskat.player.JSkatPlayer;
import jskat.share.Card;
import jskat.share.CardDeck;
import jskat.share.CardVector;
import jskat.share.SkatConstants;
import jskat.share.Tools;
import jskat.share.exception.SkatHandlingException;
import jskat.share.exception.WrongCardException;
import jskat.share.rules.SkatRules;
import jskat.share.rules.SkatRulesFactory;
import jskat.test.share.TestHelper;

import org.apache.log4j.Logger;

public class SkatGame extends Observable {

	private static final Logger log = Logger
			.getLogger(jskat.control.SkatGame.class);

	/**
	 * 
	 * @param dataModel
	 *            The data model of JSkat
	 * @param skatSeries
	 *            The skat series where the game is played
	 */
	public SkatGame(JSkatDataModel dataModel, SkatSeries skatSeries, int dealer) {

		this.jskatStrings = dataModel.getResourceBundle();
		this.jskatOptions = dataModel.getJSkatOptions();
		this.mainWindow = dataModel.getMainWindow();
		this.skatTableOptions = jskatOptions.getSkatTableOptions();
		this.skatSeries = skatSeries;

		gameData = new SkatGameData();

		gameData.setPlayers(skatSeries.getSkatSeriesData().getPlayers());

		// 20.05.07 mjl: set forehand correctly...
		gameData.setDealer(dealer);
		playerOrder[0] = (dealer + 1) % 3;
		playerOrder[1] = (playerOrder[0] + 1) % 3;
		playerOrder[2] = (playerOrder[0] + 2) % 3;

		// notify all players
		gameData.getPlayers()[0].newGame();
		gameData.getPlayers()[1].newGame();
		gameData.getPlayers()[2].newGame();

		cardDeck = new CardDeck();

		bidThread = BiddingThread.getInstance(dataModel, this, playerOrder);

		setState(GameState.CREATED);

		log.debug("Skat game created (Dealer=Player " + dealer + ").");
	}

	/**
	 * Controls the playing of a game
	 * 
	 * @param newGame
	 */
	public void playing(GameAnnouncement newGame) {

		log
				.debug("=============================================================================");
		log.debug("playing()");

		if (newGame == null) {

			log.error("playing() newGame == null");

			// no player bid 18
			// actually, this code should never be executed
			// as passed in games are already considered in showSkat()

			if (skatTableOptions.getRules() == SkatTableOptions.PUB_RULES
					&& skatTableOptions.isPlayRamsch()
					&& skatTableOptions.isRamschEventNoBid()) {

				log.warn("This should never happen....");

				// TODO: ???????? Why a ROUND of ramsch games ??????
				skatSeries.getSkatSeriesData().addRoundOfRamschGames();
				prepareRamschGame();

			} else {

				setState(GameState.GAME_OVER);
				gameData.setGameType(SkatConstants.GameTypes.PASSED_IN);
				gameData.setGameLost(true);
				finishGame();
			}

			return;
		}

		gameData.setAnnouncement(newGame);
		gameData.setGameType(newGame.getGameType());
		rules = SkatRulesFactory.getSkatRules(newGame.getGameType());
		gameData.setTrump(newGame.getTrump());
		gameData.setOuvert(newGame.isOuvert());

		if (gameData.getGameType() != SkatConstants.GameTypes.PASSED_IN) {

			updateDealtCards(gameData.getSinglePlayer());
		}

		setState(GameState.PLAYING);

		// TODO (mjl) Make a better game announcement
		String trumpColorText = new String();

		int cardFace = jskatOptions.getCardFace();
		SkatConstants.Suits trump = gameData.getTrump();

		if (trump == SkatConstants.Suits.CLUBS) {

			if (cardFace == JSkatOptions.CARD_FACE_FRENCH) {

				trumpColorText = jskatStrings.getString("clubs");

			} else if (cardFace == JSkatOptions.CARD_FACE_GERMAN) {

				trumpColorText = jskatStrings.getString("acorns");
			}
		}
		else if (trump == SkatConstants.Suits.SPADES) {

			if (cardFace == JSkatOptions.CARD_FACE_FRENCH) {

				trumpColorText = jskatStrings.getString("spades");

			} else if (cardFace == JSkatOptions.CARD_FACE_GERMAN) {

				trumpColorText = jskatStrings.getString("greens");
			}
		}
		else if (trump == SkatConstants.Suits.HEARTS) {

			trumpColorText = jskatStrings.getString("hearts");
		}
		else if (trump == SkatConstants.Suits.DIAMONDS) {

			if (cardFace == JSkatOptions.CARD_FACE_FRENCH) {

				trumpColorText = jskatStrings.getString("diamonds");

			} else if (cardFace == JSkatOptions.CARD_FACE_GERMAN) {

				trumpColorText = jskatStrings.getString("bells");
			}
		}

		String gameTypeText = "";
		SkatConstants.GameTypes gameType = gameData.getGameType();
		
		if (gameType == SkatConstants.GameTypes.SUIT) {

			gameTypeText = jskatStrings.getString("suit_game");
		}
		else if (gameType == SkatConstants.GameTypes.GRAND) {

			gameTypeText = jskatStrings.getString("grand");
		}
		else if (gameType == SkatConstants.GameTypes.NULL) {

			gameTypeText = jskatStrings.getString("null");
		}
		else if (gameType == SkatConstants.GameTypes.RAMSCH) {

			gameTypeText = jskatStrings.getString("ramsch");
		}
		else if (gameType == SkatConstants.GameTypes.RAMSCHGRAND) {

			gameTypeText = jskatStrings.getString("ramsch_grand");
		}

		if (isJSkatPlayedByHuman()) {

			// show dialog only if a human player is involved

			String message = jskatStrings.getString("new_game") + ": "
					+ gameTypeText + " " + trumpColorText + "\n";

			// When playing ramsch, there is no single player
			if (gameData.getGameType() != SkatConstants.GameTypes.RAMSCH) {

				message = message
						+ jskatStrings.getString("player")
						+ ": "
						+ gameData.getPlayers()[gameData.getSinglePlayer()]
								.getPlayerName() + "\n";
			}

			message = message
					+ jskatStrings.getString("forehand")
					+ ": "
					+ gameData.getPlayers()[(gameData.getDealer() + 1) % 3]
							.getPlayerName();

			JOptionPane.showMessageDialog(mainWindow, message, jskatStrings
					.getString("game_announcement"),
					JOptionPane.INFORMATION_MESSAGE);

			// add all cards in the skat CardVector to the HumanPlayer
			gameData.getPlayers()[2].takeSkat(gameData.getSkat());
		}

		// Set the values to the game
		currPlayerID = 0;

		// remember the jacks of the single player for calculating the game
		// result

		CardVector singlePlayerCards = gameData.getDealtCards().get(
				gameData.getSinglePlayer());

		log.debug("SinglePlayerCards(" + gameData.getSinglePlayer() + "): "
				+ singlePlayerCards);

		if (singlePlayerCards.contains(SkatConstants.Suits.CLUBS, SkatConstants.Ranks.JACK)) {
			gameData.setClubJack(true);
		}
		if (singlePlayerCards
				.contains(SkatConstants.Suits.SPADES, SkatConstants.Ranks.JACK)) {
			gameData.setSpadeJack(true);
		}
		if (singlePlayerCards
				.contains(SkatConstants.Suits.HEARTS, SkatConstants.Ranks.JACK)) {
			gameData.setHeartJack(true);
		}
		if (singlePlayerCards.contains(SkatConstants.Suits.DIAMONDS,
				SkatConstants.Ranks.JACK)) {
			gameData.setDiamondJack(true);
		}

		log.debug("C-J:" + gameData.getClubJack() + " S-J:"
				+ gameData.getSpadeJack() + " H-J:" + gameData.getHeartJack()
				+ " D-J:" + gameData.getDiamondJack());
		log.debug("trying to start game...");

		startGame();

		log.debug("game started...");

		gameData.addTrick(playerOrder[0]);

		if (gameData.getPlayers()[playerOrder[currPlayerID]].isAIPlayer()) {

			log.debug("AI player has to move...");

			setState(GameState.AI_PLAYER_PLAYING);

			wait(jskatOptions.getTrickRemoveDelayTime());

		} else if (gameData.getPlayers()[playerOrder[currPlayerID]]
				.isHumanPlayer()) {

			log.debug("Human player has to move...");

			setState(GameState.WAIT_FOR_HUMAN_PLAYER_INPUT);
		}
	}

	/**
	 * Prepare the ramsch game
	 */
	private void prepareRamschGame() {

		gameData.setGameType(SkatConstants.GameTypes.RAMSCH);
		gameData.setTrump(null);

		// first ask if anyone wants to play a grand hand
		if (skatTableOptions.getRules() == SkatTableOptions.PUB_RULES
				&& skatTableOptions.isPlayRamsch() == true
				&& skatTableOptions.isRamschGrandHandPossible() == true) {

			for (int loop = 0; loop < 3; loop++) {

				int currPlayer = (playerOrder[0] + loop) % 3;

				log.debug("Asking player " + currPlayer + "("
						+ gameData.getPlayers()[currPlayer].getPlayerName()
						+ ") about grand hand.");

				if (gameData.getPlayers()[currPlayer].isHumanPlayer()) {

					int choice = JOptionPane.showConfirmDialog(mainWindow,
							jskatStrings.getString("ramsch_grand_question"),
							jskatStrings.getString("ramsch_game"),
							JOptionPane.YES_NO_OPTION);

					if (choice == JOptionPane.YES_OPTION) {

						log.debug("player "
								+ currPlayer
								+ "("
								+ gameData.getPlayers()[currPlayer]
										.getPlayerName()
								+ ") wants to play a grand hand");

						gameData.setHand(true);
						gameData.setSinglePlayer(currPlayer);

						GameAnnouncement newGame = new GameAnnouncement();
						newGame.setGameType(SkatConstants.GameTypes.RAMSCHGRAND);
						newGame.setTrump(null);

						playing(newGame);
						return;
					}

				} else {

					// TODO (mjl) ask AI players if they want to play a grand
					// hand
					// log.debug("player " + currPlayer +
					// "("+players[currPlayer].getPlayerName()+") wants to play
					// a grand hand");
					if (isJSkatPlayedByHuman()) {
						JOptionPane.showMessageDialog(mainWindow, gameData
								.getPlayers()[currPlayer].getPlayerName()
								+ " "
								+ jskatStrings.getString("ramsch_no_grand"),
								jskatStrings.getString("ramsch_game"),
								JOptionPane.INFORMATION_MESSAGE);
					}
				}
			}

			log.debug("No one wants to play a grand hand...");
		}

		if (skatTableOptions.getRules() == SkatTableOptions.PUB_RULES
				&& skatTableOptions.isPlayRamsch() == true
				&& skatTableOptions.isSchieberRamsch() == true) {

			schieberRamschThread = new SchieberRamschThread(this);
			schieberRamschThread.start();
		} else {

			// when no Schieberramsch is played
			// --> start the game immediatly
			gameData.setSinglePlayer(playerOrder[0]);

			// TODO (mjl) Test this bit
			GameAnnouncement newGame = new GameAnnouncement();
			newGame.setGameType(SkatConstants.GameTypes.RAMSCH);
			newGame.setTrump(null);

			playing(newGame);
		}
	}

	/**
	 * Finishes the game and does some cleanup before the next game
	 */
	private void finishGame() {

		if (gameData.getGameType() != SkatConstants.GameTypes.PASSED_IN) {

			if (gameData.getGameType() == SkatConstants.GameTypes.RAMSCH) {
				calculateRamschResult();
			} else {
				calculateResult();
			}
		}

		// TODO (mjl) Here the decision should be made, whether any events
		// should trigger a bockramsch round
		// - i.e. check ramsch options from option dialog
		// --> call dataModel.getCurrentRound().addRoundOfRamschGames();

		// if the game was a ramsch grand hand, the same player is forehand
		// again
		// TODO (js) This should be adjustable in the table options
		if (gameData.getGameType() != SkatConstants.GameTypes.RAMSCHGRAND) {

			dealer = (gameData.getDealer() + 1) % 3;

			log
					.debug("Rotating forehand player. Dealer for next game is player "
							+ dealer
							+ " ("
							+ gameData.getPlayers()[dealer].getPlayerName()
							+ ")");

		} else {

			log
					.debug("No rotation of dealer (ramsch grand hand). Dealer for next game is again player "
							+ dealer
							+ " ("
							+ gameData.getPlayers()[dealer].getPlayerName()
							+ ")");

			// TODO (js) comment about what does this mean
			// TODO (mjl) is this needed anymore?
			// if (!skatSeries.isRamschGamesRemaining()) {

			// skatSeries.addRamschGame();
			// }
		}

		// skatSeries.startNewGame();
		setState(GameState.WAIT_FOR_NEXT_GAME);
	}

	/**
	 * 
	 */
	private void calculateRamschResult() {
		// create a nice end of game message for the user
		StringBuffer scoreMessage = new StringBuffer();
		scoreMessage.append(gameData.getPlayers()[0].getPlayerName() + ": "
				+ gameData.getScore(0) + "\n");
		scoreMessage.append(gameData.getPlayers()[1].getPlayerName() + ": "
				+ gameData.getScore(1) + "\n");
		scoreMessage.append(gameData.getPlayers()[2].getPlayerName() + ": "
				+ gameData.getScore(2) + "\n");
		scoreMessage.append("(Skat: "
				+ (gameData.getSkat().getCard(0).getPoints() + gameData
						.getSkat().getCard(1).getPoints()) + ")\n");

		if (true) {

			if (skatTableOptions.getRamschSkat() == SkatTableOptions.SKAT_TO_LOSER) {

				scoreMessage.append(jskatStrings.getString("skat_to_loser"));

			} else if (skatTableOptions.getRamschSkat() == SkatTableOptions.SKAT_TO_LAST_TRICK) {

				scoreMessage.append(jskatStrings
						.getString("skat_to_last_trick"));

			} else {

				log
						.warn("Undefined skat winner in ramsch game. getRamschSkat()="
								+ skatTableOptions.getRamschSkat());
			}

			scoreMessage.append(" --> "
					+ gameData.getPlayers()[gameData.getSkatOwner()]
							.getPlayerName() + "\n\n");
		}

		// check if anyone is virgin (jungfrau) or if it's a walkthrough
		// (durschmarsch)
		int[] trickWins = { 0, 0, 0 };
		for (int i = 0; i < 10; i++) {

			trickWins[gameData.getTrickWinner(i)]++;
		}

		log.debug("Tricks: player 0: " + trickWins[0] + ", player 1: "
				+ trickWins[1] + ", player 2: " + trickWins[2]);

		// TODO re-implement this a.s.a.p.
		if (rules.isDurchMarsch(0, gameData) ||
				rules.isDurchMarsch(1, gameData) ||
				rules.isDurchMarsch(2, gameData)) {

			log.info("One player did a Durschmarsch!");

			gameData.setDurchMarsch(true);
			// TODO this is wrong
			gameData.setJungFrau(false);

		} else if (rules.isJungFrau(0, gameData) ||
					rules.isJungFrau(1, gameData) ||
					rules.isJungFrau(2, gameData)) {

			log.info("One or two players is/are Jungfrau!");
			// only one player can be jungfrau - otherwise it's a
			// durchmarsch
			// TODO wrong!
			gameData.setJungFrau(true);
			gameData.setDurchMarsch(false);
		}

		gameData.finishRamschGame();
		// calculate the game result
		gameData.calcResult();

		if (gameData.getGeschoben() > 1) {

			scoreMessage.append(gameData.getGeschoben() + " "
					+ jskatStrings.getString("skipped2") + "\n");

		} else if (gameData.getGeschoben() > 0) {

			scoreMessage.append(gameData.getGeschoben() + " "
					+ jskatStrings.getString("skipped1") + "\n");
		}

		if (gameData.isJungFrau()) {

			// TODO re-implement it!
//			scoreMessage.append(gameData.getPlayers()[AbstractSkatRules
//					.isJungfrau(trickWins)].getPlayerName()
//					+ " " + jskatStrings.getString("jungfrau") + "\n");
		}

		if (!gameData.isDurchMarsch()) {

			scoreMessage.append("\n"
					+ gameData.getPlayers()[gameData.getSinglePlayer()]
							.getPlayerName() + " "
					+ jskatStrings.getString("ramsch_loser") + " ("
					+ gameData.getResult() + ")");

		} else {

			scoreMessage.append("\n"
					+ gameData.getPlayers()[gameData.getSinglePlayer()]
							.getPlayerName() + " "
					+ jskatStrings.getString("ramsch_durchmarsch_winner")
					+ " (" + gameData.getResult() + ")");
		}

		if (isJSkatPlayedByHuman()) {

			JOptionPane.showMessageDialog(mainWindow, scoreMessage,
					jskatStrings.getString("game_result"),
					JOptionPane.INFORMATION_MESSAGE);
		}

		// skatSeries.ramschGameFinished();
		// TODO (mjl) is this needed anymore?
	}

	/**
	 * Calculates the result of the game
	 * 
	 * @param currGame
	 */
	private void calculateResult() {

		int playerScore = gameData.getSinglePlayerScore();
		int opponentScore = gameData.getOpponentScore();

		String theWinnerIs;
		
		// TODO Refactor it, because it hurts so much ;-)
		if (gameData.getGameType() == SkatConstants.GameTypes.NULL
				&& gameData.isGameLost()) {

			theWinnerIs = jskatStrings.getString("opponent_player_win_null");

		} else if (gameData.getGameType() == SkatConstants.GameTypes.NULL) {

			theWinnerIs = jskatStrings.getString("single_player_wins_null");
			gameData.setGameLost(false);

		} else if (playerScore > opponentScore) {

			theWinnerIs = jskatStrings.getString("single_player_wins");
			gameData.setGameLost(false);

		} else {

			theWinnerIs = jskatStrings.getString("opponent_player_win");
			gameData.setGameLost(true);
		}

		if (gameData.getGameType() != SkatConstants.GameTypes.NULL) {
			theWinnerIs = theWinnerIs + " " + playerScore + " "
					+ jskatStrings.getString("to") + " " + opponentScore + " "
					+ jskatStrings.getString("points") + ".";
		}

		if (playerScore <= 30 || opponentScore <= 30) {
			
			gameData.setSchneider(true);
		}

		// FIXME This is wrong, in schwarz games no trick can be made
		if (playerScore == 0 || opponentScore == 0) {
			
			gameData.setSchwarz(true);
		}

		gameData.calcResult();

		// if player bid too much --> change message
		if (gameData.getOverBidded()) {
			theWinnerIs = jskatStrings.getString("opponent_player_win_overbid");
		}

		if (isJSkatPlayedByHuman()) {

			// show this dialog only when a Human player is involved
			JOptionPane.showMessageDialog(mainWindow, theWinnerIs, jskatStrings
					.getString("game_result"), JOptionPane.INFORMATION_MESSAGE);
		}

	}

	/**
	 * Returns the data of the game
	 * 
	 * @return The data of the game
	 */
	public SkatGameData getSkatGameData() {

		return gameData;
	}

	/**
	 * Wait for a short time
	 * 
	 * @param delay
	 *            The time to wait in milliseconds
	 */
	private void wait(int delay) {

		// JSkatTimer skatTimer = new JSkatTimer(delay);
		new JSkatTimer(delay);
	}

	/**
	 * Helper class for waiting a short time
	 * 
	 * @author Jan Schäfer
	 */
	private class JSkatTimer extends java.util.Timer {

		/**
		 * Creates a new instance of JSkatTimer
		 * 
		 * @param delay
		 */
		public JSkatTimer(int delay) {

			this.schedule(new JSkatTimerTask(), delay);
		}

		/**
		 * Helper class for JSkatTimer
		 * 
		 * @author Jan Schäfer <j@nschaefer.net>
		 */
		private class JSkatTimerTask extends TimerTask {

			/**
			 * @see java.lang.Runnable#run()
			 */
			public void run() {

				if (getState() == GameState.AI_PLAYER_PLAYING) {

					try {

						playTrickCard();

					} catch (WrongCardException except) {

						/*
						 * JOptionPane.showMessageDialog(mainWindow, gameData
						 * .getPlayers()[playerOrder[currPlayerID]]
						 * .getPlayerName() + " (" + except.getMessage() + ")",
						 * "Wrong card", JOptionPane.ERROR_MESSAGE);
						 */
						log
								.debug(gameData.getPlayers()[playerOrder[currPlayerID]]
										.getPlayerName()
										+ " (" + except.getMessage() + ")");
						setState(GameState.GAME_OVER);
						finishGame();
					}

				} else if (getState() == GameState.TRICK_COMPLETED) {

					if (!jskatOptions.isTrickRemoveAfterClick()) {

						calculateTrickWinner();
					}
				}
			}
		}
	}

	/**
	 * Calculates the winner of the current trick
	 * 
	 */
	protected void calculateTrickWinner() {

		int trickWinner = 0;

		Trick trick = gameData.getTricks().get(trickNumber);

		if (trick.getSecondCard().beats(trick.getFirstCard(),
				gameData.getAnnoucement(), trick.getFirstCard())) {

			trickWinner = 1;
		}
		if (trick.getThirdCard().beats(trick.getCard(trickWinner),
				gameData.getAnnoucement(), trick.getFirstCard())) {
			trickWinner = 2;
		}

		gameData.setTrickWinner(trickNumber, playerOrder[trickWinner]);

		log.debug("And the trick winner is: " + playerOrder[trickWinner]);

		CardVector trickVector = trick.getCardVector();

		// show the completed trick to all the players
		for (int i = 0; i < 3; i++) {

			gameData.getPlayers()[i].showTrick(trickVector,
					playerOrder[trickWinner]);
		}

		int trickValue = 0;

		for (int i = 0; i < trickVector.size(); i++) {

			trickValue = trickValue + trick.getCard(i).getPoints();
		}

		log.debug("TrickValue: " + trickValue);

		// add the trick value to the trick winner's score
		gameData.addToPlayerPoints(playerOrder[trickWinner], trickValue);

		trickNumber++;
		currPlayerID = 0;

		if (gameData.getGameType() == SkatConstants.GameTypes.NULL
				&& playerOrder[trickWinner] == gameData.getSinglePlayer()) {

			// null game is lost because single player made a trick
			log.debug("Null game is lost!");

			setState(GameState.GAME_OVER);
			gameData.setGameLost(true);
			finishGame();

		} else if (trickNumber < 10) {

			dealer = playerOrder[trickWinner];
			// remember the player order of the next trick for showing the last
			// tricks later on
			gameData.addTrick(dealer);

			setState(GameState.NEW_TRICK_STARTED);

			// 18.05.07 mjl: added new player order
			playerOrder[0] = dealer;
			playerOrder[1] = (dealer + 1) % 3;
			playerOrder[2] = (dealer + 2) % 3;

			if (gameData.getPlayers()[dealer].isAIPlayer()) {

				setState(GameState.AI_PLAYER_PLAYING);
				wait(jskatOptions.getTrickRemoveDelayTime());

			} else {

				setState(GameState.WAIT_FOR_HUMAN_PLAYER_INPUT);
			}

		} else {

			// at the end of the game: wait for last trick to be removed
			wait(jskatOptions.getTrickRemoveDelayTime());

			// regular end of game
			setState(GameState.GAME_OVER);

			// set the game to won, just to make sure that a null game is
			// evaluated properly
			gameData.setGameLost(false);

			if (gameData.getGameType() == SkatConstants.GameTypes.RAMSCH) {

				log.debug("Ramsch Skat option: "
						+ skatTableOptions.getRamschSkat());
				// check options, if ramsch skat goes to the winner of the final
				// trick
				if (skatTableOptions.getRamschSkat() == SkatTableOptions.SKAT_TO_LAST_TRICK) {

					log
							.debug("Ramsch game - skat goes to winner of the last trick: "
									+ playerOrder[trickWinner]
									+ " ("
									+ gameData.getPlayers()[playerOrder[trickWinner]]
											.getPlayerName() + ")");

					gameData.setSkatOwner(playerOrder[trickWinner]);
				} else {
					if (gameData.getScore(0) > gameData.getScore(1)) {
						if (gameData.getScore(0) > gameData.getScore(2)) {
							gameData.setSkatOwner(0);
						} else {
							gameData.setSkatOwner(2);
						}
					} else {
						if (gameData.getScore(1) > gameData.getScore(2)) {
							gameData.setSkatOwner(1);
						} else {
							gameData.setSkatOwner(2);
						}
					}
					log
							.debug("Ramsch game - skat goes to player with the most points: "
									+ gameData.getSkatOwner()
									+ " ("
									+ gameData.getPlayers()[gameData
											.getSkatOwner()].getPlayerName()
									+ ")");
				}

			} else {

				gameData.setSkatOwner(gameData.getSinglePlayer());
			}

			gameData.addToPlayerPoints(gameData.getSkatOwner(), gameData.getSkat()
					.getCard(0).getPoints()
					+ gameData.getSkat().getCard(1).getPoints());

			finishGame();
		}
	}

	/**
	 * Deals the cards to the skat players
	 * 
	 */
	public void dealCards() {

		// TODO please do these thing in the unit testing parts

		if (PRESET_CARDSET >= 0) {
			ArrayList<HashSet<Card>> presetCardset = TestHelper.dealCardset(PRESET_CARDSET);

			for (int i = 0; i < 3; i++) {
				Iterator<Card> iter = presetCardset.get(i).iterator();
				while (iter.hasNext()) {
					Card toDeal = iter.next();
					gameData.setDealtCard(i, toDeal);
					gameData.getPlayers()[i].takeCard(cardDeck.remove(toDeal));
				}
			}
			Iterator<Card> iter = presetCardset.get(3).iterator();
			while (iter.hasNext()) {
				Card toDeal = iter.next();
				gameData.setDealtCard(3, cardDeck.remove(toDeal));
			}
		} else {

			int cardsToDeal = 0;
			cardDeck.shuffle();

			// Deal in three steps
			for (int step = 0; step < 3; step++) {
				for (int player = 0; player < 3; player++) {

					int receivingPlayer = (playerOrder[0] + player) % 3;

					switch (step) {

					case (0):
					case (2):
						cardsToDeal = 3;
						break;
					case (1):
						cardsToDeal = 4;
						break;
					}

					// Deal cards to Skat player
					for (int k = 0; k < cardsToDeal; k++) {

						// recall card
						gameData.setDealtCard(receivingPlayer, cardDeck
								.getCard(0));
						// inform player
						gameData.getPlayers()[receivingPlayer]
								.takeCard(cardDeck.remove(0));
					}
				}

				// After dealing the first round
				if (step == 0) {

					// Put two cards into the Skat
					gameData.setDealtCard(3, cardDeck.remove(0));
					gameData.setDealtCard(3, cardDeck.remove(0));
				}
			}
		}

		log.debug("Dealt cards: " + Tools.dumpCards(gameData.getDealtCards()));

		setState(GameState.BIDDING);

		mainWindow.getPlayArea().getBiddingPanel().setSkatGame(this);
		bidThread.start();
	}

	/**
	 * Updates the dealt cards vector
	 * 
	 * @param playerID
	 *            ID of the player
	 */
	public void updateDealtCards(int playerID) {

		Card helperCard;
		CardVector skat = gameData.getSkat();
		Vector<CardVector> dealtCards = gameData.getDealtCards();

		log.debug("Updating dealt cards (old):" + dealtCards.get(playerID)
				+ ", old Skat=" + dealtCards.get(3) + ", new Skat=" + skat);

		helperCard = skat.getCard(0);

		if (!dealtCards.get(3).contains(helperCard)) {

			// player has put a different card into skat, so remove it and put
			// it in the new skat
			dealtCards.get(playerID).remove(helperCard);
			dealtCards.get(3).add(helperCard);
		}

		helperCard = skat.getCard(1);

		if (!dealtCards.get(3).contains(helperCard)) {

			// player has put a different card into skat
			dealtCards.get(playerID).remove(helperCard);
			dealtCards.get(3).add(helperCard);
		}

		log.debug("Updating dealt cards (tmp):" + dealtCards.get(playerID)
				+ ", Skat=" + dealtCards.get(3));

		Iterator<Card> iter = dealtCards.get(3).iterator();

		while (iter.hasNext()) {

			helperCard = iter.next();

			// fixed: NEVER use object comparison (== or !=) - always use
			// .equals() !!!!!
			if (!helperCard.equals(skat.getCard(0))
					&& !helperCard.equals(skat.getCard(1))) {

				// player has taken card from skat
				iter.remove();
				dealtCards.get(playerID).add(helperCard);
			}
		}

		log.debug("Updating dealt cards (new):" + dealtCards.get(playerID)
				+ ", Skat=" + dealtCards.get(3));

	}

	/**
	 * Promotes user input to the bidding thread
	 * 
	 * @param userBidsMore
	 *            TRUE if the user wants to bid more otherwise FALSE
	 */
	public void notifyBiddingThread(boolean userBidsMore) {

		bidThread.notifyMe(userBidsMore);
	}

	/**
	 * Promotes user input to the SchieberRamschThread
	 */
	public void notifySchieberRamschThread() {

		schieberRamschThread.notifyMe();
	}

	/**
	 * Shows the skat to the single player (or asks a human player whether he
	 * wants to look into the skat) and announces the game.
	 */
	public void showSkat() {

		log.debug("showSkat()");

		if (gameData.getGameType() == SkatConstants.GameTypes.RAMSCH) {

			// we play a ramsch game
			setState(GameState.SHOWING_SKAT);

		} else {

			// This isn't a ramsch game (yet...)

			// Was any bidding done?
			if (gameData.getBidValue() < 18) {
				// check if ramsch should be played
				if (jskatOptions.getRules() == SkatTableOptions.PUB_RULES
						&& jskatOptions.isPlayRamsch()
						&& jskatOptions.isRamschEventNoBid()) {

					log
							.debug("showSkat(): No player bid 18 - so we will play ramsch!");

					if (isJSkatPlayedByHuman()) {
						// show this dialog only when a Human player is involved
						JOptionPane.showMessageDialog(mainWindow, jskatStrings
								.getString("passed_in_ramsch"), jskatStrings
								.getString("ramsch"),
								JOptionPane.INFORMATION_MESSAGE);
					}

					setState(GameState.DOING_SCHIEBERRAMSCH);
					prepareRamschGame();

				} else {

					log
							.debug("showSkat(): No player bid 18 - no game to be played!");

					if (isJSkatPlayedByHuman()) {
						// show dialog only if a human player is involved
						JOptionPane.showMessageDialog(mainWindow, jskatStrings
								.getString("no_bid"), jskatStrings
								.getString("game_announcement"),
								JOptionPane.INFORMATION_MESSAGE);
					}

					playing(null);
				}
			}
			// Ask Human player for looking into skat if he is the single player
			else if (gameData.getPlayers()[gameData.getSinglePlayer()]
					.isHumanPlayer()) {

				int choice;

				choice = JOptionPane
						.showConfirmDialog(mainWindow, jskatStrings
								.getString("look_into_skat"), jskatStrings
								.getString("look_into_skat"),
								JOptionPane.YES_NO_OPTION);

				if (choice == JOptionPane.YES_OPTION) {

					gameData.setHand(false);
					setState(GameState.SHOWING_SKAT);

				} else {

					gameData.setHand(true);
					new GameAnnounceDialog(mainWindow, true, jskatStrings, this)
							.setVisible(true);
				}

				// Ask the AIPlayer who is the single player about looking into
				// skat
			} else {

				if (gameData.getPlayers()[gameData.getSinglePlayer()]
						.lookIntoSkat(false)) {

					CardVector oldSkat = new CardVector();
					oldSkat.addNew(gameData.getSkat().getCard(0));
					oldSkat.addNew(gameData.getSkat().getCard(1));

					gameData.setHand(false);
					gameData.getPlayers()[gameData.getSinglePlayer()]
							.takeSkat(gameData.getSkat());

					if (gameData.getSkat().size() != 2) {
						throw new SkatHandlingException(
								"AI player did not handle skat properly (size of skat is "
										+ gameData.getSkat().size() + ")");
					} else {
						// update dealtCards[]
						gameData.updatePlayerCards(gameData.getSinglePlayer(),
								oldSkat);
						updateDealtCards(gameData.getSinglePlayer());
					}

				} else {

					gameData.setHand(true);
					
					if (isJSkatPlayedByHuman()) {
						// show dialog only when a human being is playing in the series
						JOptionPane.showMessageDialog(mainWindow, gameData
								.getPlayers()[gameData.getSinglePlayer()]
								.getPlayerName()
								+ " "
								+ jskatStrings.getString("aiplayer_handgame"),
								jskatStrings.getString("game_announcement"),
								JOptionPane.INFORMATION_MESSAGE);
					}
				}

				playing(gameData.getPlayers()[gameData.getSinglePlayer()]
						.announceGame());

				// TODO (mjl) Show a game announce dialog when the AIPlayer is
				// single player, too

			}
		}
	}

	/**
	 * Play next card in a trick
	 * 
	 * @throws WrongCardException
	 */
	private void playTrickCard() throws WrongCardException {

		// TODO bad design, this method is only valid for AIPlayer
		log.debug("playTrickCard() for trick " + trickNumber);

		JSkatPlayer currPlayer = gameData.getPlayers()[playerOrder[currPlayerID]];
		CardVector playerCards = gameData
				.getPlayerCards(playerOrder[currPlayerID]);

		if (getState() == GameState.AI_PLAYER_PLAYING) {

			CardVector trickVector = ((Trick) gameData.getTricks().get(
					trickNumber)).getCardVector();

			// Asks current computer player for the next card
			Card cardPlayed = currPlayer.playCard(trickVector);

			if (playerCards.contains(cardPlayed)) {

				// Player has the card
				if ((trickVector.size() == 0)
						|| (trickVector.size() > 0 && rules.isCardAllowed(
								cardPlayed, playerCards,
								trickVector.getCard(0), gameData.getTrump()))) {

					// Card is allowed because it's the first card in the trick
					// or the card is allowed to be played after the first card
					// of the trick
					log.debug("PLAYER IS GOOD");

					playerCards.remove(cardPlayed);

				} else {

					// Card is not allowed
					log.error("PLAYER IS EVIL");
					log.debug("1st card    : " + trickVector.getCard(0));
					log.debug("Player cards: " + playerCards);
					log.debug("Card played : " + cardPlayed);
					log.debug("GameType    : " + gameData.getGameType());
					log.debug("Trump       : " + gameData.getTrump());

					if (currPlayer instanceof jskat.player.AIPlayerMJL.AIPlayerMJL) {

						throw new WrongCardException(
								"AIPlayerMJL played a card that is not allowed");

					} else if (currPlayer instanceof jskat.player.AIPlayerRND.AIPlayerRND) {

						throw new WrongCardException(
								"AIPlayerRND played a card that is not allowed");
					}
				}

			} else {

				// Player doesn't have the card
				log.error("PLAYER IS EVIL");
				log.debug("1st card    : " + trickVector.getCard(0));
				log.debug("Player cards: " + playerCards);
				log.debug("Card played : " + cardPlayed);
				log.debug("GameType    : " + gameData.getGameType());
				log.debug("Trump       : " + gameData.getTrump());

				throw new WrongCardException(
						"AI player played a card he doesn't own");
			}

			gameData.setTrickCard(trickNumber, currPlayerID, cardPlayed);

			currPlayerID++;

			log.debug("currPlayerID: " + currPlayerID);

			if (currPlayerID < 3) {

				if (gameData.getPlayers()[playerOrder[currPlayerID]]
						.isAIPlayer()) {

					wait(jskatOptions.getTrickRemoveDelayTime());

				} else {

					setState(GameState.WAIT_FOR_HUMAN_PLAYER_INPUT);
				}

			} else {

				setState(GameState.TRICK_COMPLETED);

				if (!jskatOptions.isTrickRemoveAfterClick()) {

					wait(jskatOptions.getTrickRemoveDelayTime());
				}
			}
		}
	}

	/**
	 * Check if the human card is valid and then ask all AI players that come
	 * after the human player
	 * 
	 * @param suit
	 *            suit for human card
	 * @param value
	 *            value for human card
	 */
	protected void playTrickCard(SkatConstants.Suits suit, SkatConstants.Ranks rank) {

		log.debug("Human player plays card as player #" + currPlayerID);

		// TODO bad design, this method is only valid for human players

		log.debug("playTrickCard() for trick " + trickNumber + ": suit: "
				+ suit + ", value:" + rank);

		boolean cardAllowed = false;

		CardVector trickVector = ((Trick) gameData.getTricks().get(trickNumber))
				.getCardVector();

		// only if the user has clicked on a card not an empty panel
		Card cardToBePlayed = new Card(suit, rank);

		if (trickVector.size() == 0) {

			// First card in the trick is always allowed
			cardAllowed = true;

		} else if (rules.isCardAllowed(cardToBePlayed, gameData
				.getPlayerCards(playerOrder[currPlayerID]), trickVector
				.getCard(0), gameData.getTrump())) {

			cardAllowed = true;
		}

		if (cardAllowed) {

			log.debug("card count trick #" + trickNumber + "="
					+ trickVector.size());

			Card cardPlayed = new Card(suit, rank);

			gameData.setTrickCard(trickNumber, currPlayerID, cardPlayed);

			gameData.getPlayerCards(playerOrder[currPlayerID]).remove(
					cardPlayed);

			currPlayerID++;

			if (currPlayerID < 3) {

				setState(GameState.AI_PLAYER_PLAYING);

				// Ask all computer players for their cards
				wait(jskatOptions.getTrickRemoveDelayTime());

			} else {

				setState(GameState.TRICK_COMPLETED);

				if (!jskatOptions.isTrickRemoveAfterClick())
					wait(jskatOptions.getTrickRemoveDelayTime());
			}
		}
	}

	/**
	 * Starts the game
	 * 
	 * @param gameType
	 *            ID of the gameType that will be played
	 * @param trump
	 *            the ID of the trump suit
	 * @param handGame
	 *            true if a hand game will be played
	 * @param ouvertGame
	 *            true if a ouvert game will be played
	 */
	private void startGame() {

		log.debug("startGame() begin");

		// Tell everyone what game is played
		for (int i = 0; i < 3; i++) {

			log.debug("tell player " + i);

			gameData.getPlayers()[i]
					.startGame(gameData.getSinglePlayer(), playerOrder[0],
							gameData.getGameType(), gameData.getTrump(),
							gameData.isHand(), gameData.isOuvert());

			if (gameData.isOuvert()) {
				// in ouvert games disclose the single player cards to all
				// players
				gameData.getPlayers()[i].discloseOuvertCards(gameData
						.getPlayerCards(gameData.getSinglePlayer()));
			}
		}

		trickNumber = 0;

		setState(GameState.STARTED);

		log.debug("startGame() end");
	}

	/**
	 * Puts a card into the skat
	 * 
	 * @param suit
	 * @param value
	 */
	protected void putCardIntoSkat(SkatConstants.Suits suit, SkatConstants.Ranks rank) {

		CardVector playerCards = gameData.getPlayerCards(2);
		CardVector skatCards = gameData.getSkat();

		if (skatCards.size() < 4 && playerCards.contains(suit, rank)) {

			skatCards.add(playerCards.remove(suit, rank));
		}
	}

	/**
	 * Gives a card from the skat to a player
	 * 
	 * @param suit
	 * @param value
	 */
	protected void takeCardFromSkat(SkatConstants.Suits suit, SkatConstants.Ranks rank) {

		CardVector playerCards = gameData.getPlayerCards(2);
		CardVector skatCards = gameData.getSkat();

		if (playerCards.size() < 10 && skatCards.contains(suit, rank)) {

			playerCards.add(skatCards.remove(suit, rank));
		}
	}

	/**
	 * Gets the current bid status
	 * 
	 * @return The BidStatus
	 */
	public BidStatus getBidStatus() {

		return bidThread.getBidStatus();
	}

	/**
	 * Get the information whether JSkat is played by a human or not
	 * 
	 * @return TRUE when JSkat is played by a human
	 */
	public boolean isJSkatPlayedByHuman() {

		return (gameData.getPlayers()[2].isHumanPlayer());
	}

	public int[] getPlayerOrder() {

		return playerOrder;
	}

	/**
	 * Get the state of the game
	 * 
	 * @return The state of the game
	 */
	public GameState getState() {

		return gameState;
	}

	/**
	 * Set the state of the game
	 * 
	 * @param newGameState
	 *            The new state of the game to be set
	 */
	private void setState(GameState newGameState) {

		gameState = newGameState;

		setChanged();
		notifyObservers(newGameState);
	}

	/**
	 * After a human player has processed the skat the game announcing dialog is
	 * called
	 * 
	 */
	public void skatProcessed() {

		// if this is not a ramsch game, ask the player to
		// announce his game
		if (gameData.getGameType() != SkatConstants.GameTypes.RAMSCH &&
				gameData.getGameType() != SkatConstants.GameTypes.RAMSCHGRAND) {

			new GameAnnounceDialog(mainWindow, true, jskatStrings, skatSeries
					.getCurrSkatGame()).setVisible(true);

		} else {

			skatSeries.getCurrSkatGame().notifySchieberRamschThread();
		}
	}

	public int getDealer() {
		return dealer;
	}

	public SkatTableOptions getSkatTableOptions() {
		return skatTableOptions;
	}

	/**
	 * Status of the game
	 * 
	 */
	public enum GameState {
		CREATED, INITIALISED, STARTED, DEALING, BIDDING, SHOWING_SKAT,
		DOING_SCHIEBERRAMSCH, PLAYING, AI_PLAYER_PLAYING, 
		WAIT_FOR_HUMAN_PLAYER_INPUT, TRICK_COMPLETED, NEW_TRICK_STARTED,
		GAME_OVER, WAIT_FOR_NEXT_GAME
	}

	private GameState gameState;

	private SkatRules rules;
	
	/**
	 * The skat series where the game is played
	 */
	private SkatSeries skatSeries;

	/**
	 * All data of the game
	 */
	private SkatGameData gameData;

	private JSkatOptions jskatOptions;

	private JSkatFrame mainWindow;

	/**
	 * The table options for the game rules
	 */
	private SkatTableOptions skatTableOptions;

	private ResourceBundle jskatStrings;

	private int currPlayerID;

	/** Does the bidding */
	private BiddingThread bidThread;

	private SchieberRamschThread schieberRamschThread;

	private int[] playerOrder = { 0, 1, 2 };

	private int trickNumber;

	private int dealer;

	private CardDeck cardDeck;

	// mjl 2007/09/11: set to -1 for random distribution
	// i.e. -1 ==> no preset !!!
	private static final int PRESET_CARDSET = -1;
}
