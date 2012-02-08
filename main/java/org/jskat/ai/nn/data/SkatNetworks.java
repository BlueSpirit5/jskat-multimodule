/**
 * JSkat - A skat program written in Java
 * by Jan Schäfer and Markus J. Luzius
 *
 * Version: 0.10.0-SNAPSHOT
 * Build date: 2011-10-09
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
package org.jskat.ai.nn.data;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jskat.ai.nn.util.NetworkTopology;
import org.jskat.ai.nn.util.NeuralNetwork;
import org.jskat.util.GameType;

/**
 * Holds all neural networks for the NN player
 */
public class SkatNetworks {

	private static Log log = LogFactory.getLog(SkatNetworks.class);

	private final static SkatNetworks instance = new SkatNetworks();

	private static NeuralNetwork suitDeclarerOpening;
	private static NeuralNetwork suitDeclarerMiddleGame;
	private static NeuralNetwork suitDeclarerEndGame;
	private static NeuralNetwork suitOpponentOpening;
	private static NeuralNetwork suitOpponentMiddleGame;
	private static NeuralNetwork suitOpponentEndGame;
	private static NeuralNetwork nullDeclarerOpening;
	private static NeuralNetwork nullDeclarerMiddleGame;
	private static NeuralNetwork nullDeclarerEndGame;
	private static NeuralNetwork nullOpponentOpening;
	private static NeuralNetwork nullOpponentMiddleGame;
	private static NeuralNetwork nullOpponentEndGame;
	private static NeuralNetwork grandDeclarerOpening;
	private static NeuralNetwork grandDeclarerMiddleGame;
	private static NeuralNetwork grandDeclarerEndGame;
	private static NeuralNetwork grandOpponentOpening;
	private static NeuralNetwork grandOpponentMiddleGame;
	private static NeuralNetwork grandOpponentEndGame;
	private static NeuralNetwork ramschDeclarerOpening;
	private static NeuralNetwork ramschDeclarerMiddleGame;
	private static NeuralNetwork ramschDeclarerEndGame;

	/**
	 * Gets an instance of the SkatNetworks
	 * 
	 * @return Instance
	 */
	public static SkatNetworks instance() {

		return instance;
	}

	/**
	 * Constructor
	 */
	private SkatNetworks() {

		initNetworks();
	}

	private void initNetworks() {
		// 32 input nodes for every player
		// 1 output node for win/lost
		// 1 hidden layer
		// 2*n+1 nodes in hidden layer
		// n number of nodes in input layer
		int inputCount = 96;
		// int hiddenCount = 2 * inputCount + 1;
		int hiddenCount = 10;
		int outputCount = 1;

		int[] hiddenLayer = { hiddenCount };

		NetworkTopology topol = new NetworkTopology(inputCount, outputCount, 1, hiddenLayer);

		suitDeclarerOpening = new NeuralNetwork(topol);
		suitDeclarerMiddleGame = new NeuralNetwork(topol);
		suitDeclarerEndGame = new NeuralNetwork(topol);
		suitOpponentOpening = new NeuralNetwork(topol);
		suitOpponentMiddleGame = new NeuralNetwork(topol);
		suitOpponentEndGame = new NeuralNetwork(topol);
		nullDeclarerOpening = new NeuralNetwork(topol);
		nullDeclarerMiddleGame = new NeuralNetwork(topol);
		nullDeclarerEndGame = new NeuralNetwork(topol);
		nullOpponentOpening = new NeuralNetwork(topol);
		nullOpponentMiddleGame = new NeuralNetwork(topol);
		nullOpponentEndGame = new NeuralNetwork(topol);
		grandDeclarerOpening = new NeuralNetwork(topol);
		grandDeclarerMiddleGame = new NeuralNetwork(topol);
		grandDeclarerEndGame = new NeuralNetwork(topol);
		grandOpponentOpening = new NeuralNetwork(topol);
		grandOpponentMiddleGame = new NeuralNetwork(topol);
		grandOpponentEndGame = new NeuralNetwork(topol);
		ramschDeclarerOpening = new NeuralNetwork(topol);
		ramschDeclarerMiddleGame = new NeuralNetwork(topol);
		ramschDeclarerEndGame = new NeuralNetwork(topol);

		loadNetworks(ClassLoader.getSystemResource("org/jskat/ai/nn/data") //$NON-NLS-1$
				.getPath());
	}

	/**
	 * Gets a neural network
	 * 
	 * @param gameType
	 *            Game type
	 * @param isDeclarer
	 *            TRUE, if declarer network is desired
	 * @return Neural network
	 */
	public static List<NeuralNetwork> getNetwork(GameType gameType, boolean isDeclarer) {

		List<NeuralNetwork> result = new ArrayList<NeuralNetwork>();

		switch (gameType) {
		case CLUBS:
		case SPADES:
		case HEARTS:
		case DIAMONDS:
			if (isDeclarer) {
				log.debug("Returning suit declarer network"); //$NON-NLS-1$
				result = getSuitDeclarer();
			} else {
				log.debug("Returning suit opponent network"); //$NON-NLS-1$
				result = getSuitOpponent();
			}
			break;
		case NULL:
			if (isDeclarer) {
				log.debug("Returning null declarer network"); //$NON-NLS-1$
				result = getNullDeclarer();
			} else {
				log.debug("Returning null opponent network"); //$NON-NLS-1$
				result = getNullOpponent();
			}
			break;
		case GRAND:
			if (isDeclarer) {
				log.debug("Returning grand declarer network"); //$NON-NLS-1$
				result = getGrandDeclarer();
			} else {
				log.debug("Returning grand opponent network"); //$NON-NLS-1$
				result = getGrandOpponent();
			}
			break;
		case RAMSCH:
			log.debug("Returning ramsch declarer network"); //$NON-NLS-1$
			result = getRamschDeclarer();
			break;
		case PASSED_IN:
			break;
		}

		return result;
	}

	/**
	 * Gets the neural network for declarer in suit games
	 * 
	 * @return Neural network
	 */
	private static List<NeuralNetwork> getSuitDeclarer() {
		return createNetworkList(suitDeclarerOpening, suitDeclarerMiddleGame, suitDeclarerEndGame);
	}

	/**
	 * Gets the neural network for opponent in suit games
	 * 
	 * @return Neural network
	 */
	private static List<NeuralNetwork> getSuitOpponent() {
		return createNetworkList(suitOpponentOpening, suitOpponentMiddleGame, suitOpponentEndGame);
	}

	/**
	 * Gets the neural network for declarer in null games
	 * 
	 * @return Neural network
	 */
	private static List<NeuralNetwork> getNullDeclarer() {
		return createNetworkList(nullDeclarerOpening, nullDeclarerMiddleGame, nullDeclarerEndGame);
	}

	/**
	 * Gets the neural network for opponent in null games
	 * 
	 * @return Neural network
	 */
	private static List<NeuralNetwork> getNullOpponent() {
		return createNetworkList(nullOpponentOpening, nullOpponentMiddleGame, nullOpponentEndGame);
	}

	/**
	 * Gets the neural network for declarer in grand games
	 * 
	 * @return Neural network
	 */
	private static List<NeuralNetwork> getGrandDeclarer() {
		return createNetworkList(grandDeclarerOpening, grandDeclarerMiddleGame, grandDeclarerEndGame);
	}

	/**
	 * Gets the neural network for opponent in grand games
	 * 
	 * @return Neural network
	 */
	private static List<NeuralNetwork> getGrandOpponent() {
		return createNetworkList(grandOpponentOpening, grandOpponentMiddleGame, grandOpponentEndGame);
	}

	/**
	 * Gets the neural network for declarer in ramsch games
	 * 
	 * @return Neural network
	 */
	private static List<NeuralNetwork> getRamschDeclarer() {
		return createNetworkList(ramschDeclarerOpening, ramschDeclarerMiddleGame, ramschDeclarerEndGame);
	}

	private static List<NeuralNetwork> createNetworkList(NeuralNetwork openingNet, NeuralNetwork middleGameNet,
			NeuralNetwork endGameNet) {

		List<NeuralNetwork> result = new ArrayList<NeuralNetwork>();

		for (int i = 0; i < 10; i++) {
			switch (i) {
			case 0:
			case 1:
			case 2:
				result.add(openingNet);
				break;
			case 3:
			case 4:
			case 5:
			case 6:
				result.add(middleGameNet);
				break;
			case 7:
			case 8:
			case 9:
				result.add(endGameNet);
				break;
			}
		}

		return result;
	}

	/**
	 * Loads all neural networks from files
	 * 
	 * @param filePath
	 *            Path to files
	 */
	public static void loadNetworks(String filePath) {

		String pathSep = System.getProperty("file.separator"); //$NON-NLS-1$

		suitDeclarerOpening = new NeuralNetwork();
		suitDeclarerOpening.loadNetwork("/org/jskat/ai/nn/data/jskat.suit.declarer.opening.nnet"); //$NON-NLS-1$
		suitDeclarerMiddleGame = new NeuralNetwork();
		suitDeclarerMiddleGame.loadNetwork("/org/jskat/ai/nn/data/jskat.suit.declarer.middlegame.nnet"); //$NON-NLS-1$
		suitDeclarerEndGame = new NeuralNetwork();
		suitDeclarerEndGame.loadNetwork("/org/jskat/ai/nn/data/jskat.suit.declarer.endgame.nnet"); //$NON-NLS-1$
		suitOpponentOpening = new NeuralNetwork();
		suitOpponentOpening.loadNetwork("/org/jskat/ai/nn/data/jskat.suit.opponent.opening.nnet"); //$NON-NLS-1$
		suitOpponentMiddleGame = new NeuralNetwork();
		suitOpponentMiddleGame.loadNetwork("/org/jskat/ai/nn/data/jskat.suit.opponent.middlegame.nnet"); //$NON-NLS-1$
		suitOpponentEndGame = new NeuralNetwork();
		suitOpponentEndGame.loadNetwork("/org/jskat/ai/nn/data/jskat.suit.opponent.endgame.nnet"); //$NON-NLS-1$
		nullDeclarerOpening = new NeuralNetwork();
		nullDeclarerOpening.loadNetwork("/org/jskat/ai/nn/data/jskat.null.declarer.opening.nnet"); //$NON-NLS-1$
		nullDeclarerMiddleGame = new NeuralNetwork();
		nullDeclarerMiddleGame.loadNetwork("/org/jskat/ai/nn/data/jskat.null.declarer.middlegame.nnet"); //$NON-NLS-1$
		nullDeclarerEndGame = new NeuralNetwork();
		nullDeclarerEndGame.loadNetwork("/org/jskat/ai/nn/data/jskat.null.declarer.endgame.nnet"); //$NON-NLS-1$
		nullOpponentOpening = new NeuralNetwork();
		nullOpponentOpening.loadNetwork("/org/jskat/ai/nn/data/jskat.null.opponent.opening.nnet"); //$NON-NLS-1$
		nullOpponentMiddleGame = new NeuralNetwork();
		nullOpponentMiddleGame.loadNetwork("/org/jskat/ai/nn/data/jskat.null.opponent.middlegame.nnet"); //$NON-NLS-1$
		nullOpponentEndGame = new NeuralNetwork();
		nullOpponentEndGame.loadNetwork("/org/jskat/ai/nn/data/jskat.null.opponent.endgame.nnet"); //$NON-NLS-1$
		grandDeclarerOpening = new NeuralNetwork();
		grandDeclarerOpening.loadNetwork("/org/jskat/ai/nn/data/jskat.grand.declarer.opening.nnet"); //$NON-NLS-1$
		grandDeclarerMiddleGame = new NeuralNetwork();
		grandDeclarerMiddleGame.loadNetwork("/org/jskat/ai/nn/data/jskat.grand.declarer.middlegame.nnet"); //$NON-NLS-1$
		grandDeclarerEndGame = new NeuralNetwork();
		grandDeclarerEndGame.loadNetwork("/org/jskat/ai/nn/data/jskat.grand.declarer.endgame.nnet"); //$NON-NLS-1$
		grandOpponentOpening = new NeuralNetwork();
		grandOpponentOpening.loadNetwork("/org/jskat/ai/nn/data/jskat.grand.opponent.opening.nnet"); //$NON-NLS-1$
		grandOpponentMiddleGame = new NeuralNetwork();
		grandOpponentMiddleGame.loadNetwork("/org/jskat/ai/nn/data/jskat.grand.opponent.middlegame.nnet"); //$NON-NLS-1$
		grandOpponentEndGame = new NeuralNetwork();
		grandOpponentEndGame.loadNetwork("/org/jskat/ai/nn/data/jskat.grand.opponent.endgame.nnet"); //$NON-NLS-1$
		ramschDeclarerOpening = new NeuralNetwork();
		ramschDeclarerOpening.loadNetwork("/org/jskat/ai/nn/data/jskat.ramsch.declarer.opening.nnet"); //$NON-NLS-1$
		ramschDeclarerMiddleGame = new NeuralNetwork();
		ramschDeclarerMiddleGame.loadNetwork("/org/jskat/ai/nn/data/jskat.ramsch.declarer.middlegame.nnet"); //$NON-NLS-1$
		ramschDeclarerEndGame = new NeuralNetwork();
		ramschDeclarerEndGame.loadNetwork("/org/jskat/ai/nn/data/jskat.ramsch.declarer.endgame.nnet"); //$NON-NLS-1$
	}

	/**
	 * Saves all networks to files
	 * 
	 * @param path
	 *            Path to files
	 */
	public static void saveNetworks(String path) {

		String pathSep = System.getProperty("file.separator"); //$NON-NLS-1$

		suitDeclarerOpening.saveNetwork(path.concat(pathSep).concat("jskat.suit.declarer.opening.nnet")); //$NON-NLS-1$
		suitDeclarerMiddleGame.saveNetwork(path.concat(pathSep).concat("jskat.suit.declarer.middlegame.nnet")); //$NON-NLS-1$
		suitDeclarerEndGame.saveNetwork(path.concat(pathSep).concat("jskat.suit.declarer.endgame.nnet")); //$NON-NLS-1$
		suitOpponentOpening.saveNetwork(path.concat(pathSep).concat("jskat.suit.opponent.opening.nnet")); //$NON-NLS-1$
		suitOpponentMiddleGame.saveNetwork(path.concat(pathSep).concat("jskat.suit.opponent.middlegame.nnet")); //$NON-NLS-1$
		suitOpponentEndGame.saveNetwork(path.concat(pathSep).concat("jskat.suit.opponent.endgame.nnet")); //$NON-NLS-1$
		nullDeclarerOpening.saveNetwork(path.concat(pathSep).concat("jskat.null.declarer.opening.nnet")); //$NON-NLS-1$
		nullDeclarerMiddleGame.saveNetwork(path.concat(pathSep).concat("jskat.null.declarer.middlegame.nnet")); //$NON-NLS-1$
		nullDeclarerEndGame.saveNetwork(path.concat(pathSep).concat("jskat.null.declarer.endgame.nnet")); //$NON-NLS-1$
		nullOpponentOpening.saveNetwork(path.concat(pathSep).concat("jskat.null.opponent.opening.nnet")); //$NON-NLS-1$
		nullOpponentMiddleGame.saveNetwork(path.concat(pathSep).concat("jskat.null.opponent.middlegame.nnet")); //$NON-NLS-1$
		nullOpponentEndGame.saveNetwork(path.concat(pathSep).concat("jskat.null.opponent.endgame.nnet")); //$NON-NLS-1$
		grandDeclarerOpening.saveNetwork(path.concat(pathSep).concat("jskat.grand.declarer.opening.nnet")); //$NON-NLS-1$
		grandDeclarerMiddleGame.saveNetwork(path.concat(pathSep).concat("jskat.grand.declarer.middlegame.nnet")); //$NON-NLS-1$
		grandDeclarerEndGame.saveNetwork(path.concat(pathSep).concat("jskat.grand.declarer.endgame.nnet")); //$NON-NLS-1$
		grandOpponentOpening.saveNetwork(path.concat(pathSep).concat("jskat.grand.opponent.opening.nnet")); //$NON-NLS-1$
		grandOpponentMiddleGame.saveNetwork(path.concat(pathSep).concat("jskat.grand.opponent.middlegame.nnet")); //$NON-NLS-1$
		grandOpponentEndGame.saveNetwork(path.concat(pathSep).concat("jskat.grand.opponent.endgame.nnet")); //$NON-NLS-1$
		ramschDeclarerOpening.saveNetwork(path.concat(pathSep).concat("jskat.ramsch.declarer.opening.nnet")); //$NON-NLS-1$
		ramschDeclarerMiddleGame.saveNetwork(path.concat(pathSep).concat("jskat.ramsch.declarer.middlegame.nnet")); //$NON-NLS-1$
		ramschDeclarerEndGame.saveNetwork(path.concat(pathSep).concat("jskat.ramsch.declarer.endgame.nnet")); //$NON-NLS-1$
	}

	/**
	 * Resets neural networks
	 */
	public static void resetNeuralNetworks() {
		suitDeclarerOpening.resetNetwork();
		suitDeclarerMiddleGame.resetNetwork();
		suitDeclarerEndGame.resetNetwork();
		suitOpponentOpening.resetNetwork();
		suitOpponentMiddleGame.resetNetwork();
		suitOpponentEndGame.resetNetwork();
		nullDeclarerOpening.resetNetwork();
		nullDeclarerMiddleGame.resetNetwork();
		nullDeclarerEndGame.resetNetwork();
		nullOpponentOpening.resetNetwork();
		nullOpponentMiddleGame.resetNetwork();
		nullOpponentEndGame.resetNetwork();
		grandDeclarerOpening.resetNetwork();
		grandDeclarerMiddleGame.resetNetwork();
		grandDeclarerEndGame.resetNetwork();
		grandOpponentOpening.resetNetwork();
		grandOpponentMiddleGame.resetNetwork();
		grandOpponentEndGame.resetNetwork();
		ramschDeclarerOpening.resetNetwork();
		ramschDeclarerMiddleGame.resetNetwork();
		ramschDeclarerEndGame.resetNetwork();
	}
}
