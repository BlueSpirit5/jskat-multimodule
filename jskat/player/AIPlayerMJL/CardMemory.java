/*

@ShortLicense@

Author: @MJL@

Released: @ReleaseDate@

*/

package jskat.player.AIPlayerMJL;

import org.apache.log4j.Logger;

import jskat.share.CardVector;
import jskat.share.SkatConstants;
//import jskat.share.Card;
/**
 * CardMemory builds the memory of all the cards that have been played during a certain game. 
 * It remembers all the initial cards as well as the individual hands of all players. 
 * @author Markus J. Luzius <markus@luzius.de>
 *
 */
public class CardMemory {

    private static final Logger log = Logger.getLogger(CardMemory.class);

    /**
	 * Default constructor
	 */
	public CardMemory() {
		cardsPlayed = new CardVector[3];
		cardsPlayed[0] = new CardVector();	       
		cardsPlayed[1] = new CardVector();	       
		cardsPlayed[2] = new CardVector();	       
		initialCardsPlayed = new CardVector();
		allCardsPlayed = new CardVector();
		tricksPlayed = 0;
	}
	
	/**
	 * Extended constructor with the values of the current game
	 * @param gameType
	 * @param trump
	 */
	public CardMemory(SkatConstants.GameTypes gameType, SkatConstants.Suits trump) {
		this.gameType = gameType;
		this.trump = trump;
		cardsPlayed = new CardVector[3];
		cardsPlayed[0] = new CardVector();	       
		cardsPlayed[1] = new CardVector();	       
		cardsPlayed[2] = new CardVector();	       
		initialCardsPlayed = new CardVector();
		allCardsPlayed = new CardVector();
		tricksPlayed = 0;
	}

	/**
	 * Adds the cards of the current trick to the memory
	 * @param trick current trick
	 * @param forehandPlayer player of the first card in the trick 
	 */
	public void addTrick(CardVector trick, int forehandPlayer) {
		tricksPlayed++;
		initialCardsPlayed.add(trick.getCard(0));
		allCardsPlayed.add(trick.getCard(0));
		allCardsPlayed.add(trick.getCard(1));
		allCardsPlayed.add(trick.getCard(2));
		cardsPlayed[forehandPlayer].add(trick.getCard(0));
		cardsPlayed[(forehandPlayer+1)%3].add(trick.getCard(1));
		cardsPlayed[(forehandPlayer+2)%3].add(trick.getCard(2));
		log.debug("Cards played so far:");
		log.debug("Player 0: "+cardsPlayed[0]);
		log.debug("Player 1: "+cardsPlayed[1]);
		log.debug("Player 2: "+cardsPlayed[2]);
		log.debug("Initial cards: "+initialCardsPlayed);
	}

	/**
	 * Counts, how many cards of a given suit a player has already played.<br>
	 * In a suit game, for a normal suit the maximum can be 7, of trump it can be 11.
	 * In a null game, the maximum is 8, in a grand game 7 for all suits.
	 * @param player Player who is evaluated
	 * @param suit Suit which is evaluated
	 * @return Number of times the given player has played a card of that suit
	 */
	public int timesPlayedByPlayer(int player, int suit) {
		int count = 0;
		return count;
	}

	/**
	 * Counts, how many times the given suit has been demanded as initial card.
	 * @param suit Suit which is evaluated
	 * @return Number of times the suit has been demanded
	 */
	public int timesDemanded(int suit) {
		int count = 0;
		return count;
	}

	/**
	 * Calculates how many cards of that suit are still out.
	 * @param suit Suit which is evaluated
	 * @param myCards Hand of the player
	 * @return Number of cards still out for that suit
	 */
	public int remainingCards(int suit, CardVector myCards) {
		int count = 0;
		return count;
	}

	/**	 number of tricks that have been played so far */
	private int tricksPlayed;
	/**	 type of the game
	 * @see jskat.share.SkatConstants */
	private SkatConstants.GameTypes gameType;
	/**	 trump suit in the current game
	 * @see jskat.share.SkatConstants */
	private SkatConstants.Suits trump;
	/**	 a vector of all the cards that have been played in this game so far */
	private CardVector allCardsPlayed;
	/**	 reflects all the cards that have been played by each player in this game so far */
	private CardVector[] cardsPlayed;
	/**	 reflects all the initial cards that have been played 
	 *  (i.e. what has been demanded already?) */
	private CardVector initialCardsPlayed;
}
