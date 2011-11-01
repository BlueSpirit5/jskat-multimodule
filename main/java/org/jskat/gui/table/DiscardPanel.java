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
package org.jskat.gui.table;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JButton;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jskat.gui.action.JSkatAction;
import org.jskat.gui.img.JSkatGraphicRepository;
import org.jskat.util.Card;
import org.jskat.util.CardList;
import org.jskat.util.JSkatResourceBundle;

/**
 * Holds widgets for deciding of looking into skat or playing hand game
 */
class DiscardPanel extends JPanel {

	private static Log log = LogFactory.getLog(DiscardPanel.class);
	private static final String PICK_UP_SKAT_BUTTON = "PICK_UP_SKAT_BUTTON"; //$NON-NLS-1$

	private static final String CARD_PANEL = "CARD_PANEL"; //$NON-NLS-1$

	private static final long serialVersionUID = 1L;

	private JSkatGraphicRepository bitmaps;
	private JSkatResourceBundle strings;

	private Action pickUpSkatAction;
	private JButton pickUpSkatButton;
	private boolean userPickedUpSkat = false;

	private int maxCardCount = 0;

	private CardPanel cardPanel;

	private GameAnnouncePanel announcePanel;
	private SchieberamschContextPanel ramschPanel;

	/**
	 * Constructor
	 * 
	 * @param actions
	 *            Actions for discarding panel
	 * @param newMaxCardCount
	 *            Maximum number of cards
	 */
	public DiscardPanel(ActionMap actions, int newMaxCardCount) {

		strings = JSkatResourceBundle.instance();
		bitmaps = JSkatGraphicRepository.instance();

		setActionMap(actions);
		maxCardCount = newMaxCardCount;

		initPanel();
	}

	void initPanel() {

		setBackground(Color.WHITE);

		setLayout(new CardLayout());

		cardPanel = new CardPanel(this, 0.75, false);
		add(cardPanel, CARD_PANEL);

		pickUpSkatAction = getActionMap().get(JSkatAction.PICK_UP_SKAT);
		pickUpSkatButton = new JButton(pickUpSkatAction);
		pickUpSkatButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				log.debug("user picked up skat");
				userPickedUpSkat = true;
				if(announcePanel!=null) {
					log.debug("informing announce panel");
					announcePanel.setUserPickedUpSkat(true);
				}
				else if(ramschPanel!=null) {
					ramschPanel.refresh();
				}
				DiscardPanel.this.showPanel(CARD_PANEL);

				// fire event again
				pickUpSkatButton.dispatchEvent(e);
			}
		});
		JPanel lookIntoSkatPanel = new JPanel(new MigLayout("fill")); //$NON-NLS-1$
		lookIntoSkatPanel.add(pickUpSkatButton, "center, shrink"); //$NON-NLS-1$
		lookIntoSkatPanel.setOpaque(false);
		add(lookIntoSkatPanel, PICK_UP_SKAT_BUTTON);

		setOpaque(false);
		cardPanel.showCards();
	}

	protected void setSkat(CardList skat) {

		log.debug("setting skat: "+skat);
		
		addCard(skat.get(0));
		addCard(skat.get(1));

		userPickedUpSkat = true;
	}

	void addCard(Card card) {

		log.debug("adding card");
		cardPanel.addCard(card);
	}

	void removeCard(Card card) {

		log.debug("removing card");
		cardPanel.removeCard(card);
	}

	public void resetPanel() {

		cardPanel.clearCards();
		userPickedUpSkat = false;
		showPanel(PICK_UP_SKAT_BUTTON);
	}

	protected void showPanel(String panelType) {
		log.debug("Showing panel "+panelType);

		((CardLayout) getLayout()).show(DiscardPanel.this, panelType);
	}

	public CardList getDiscardedCards() {

		return (CardList) cardPanel.cards.clone();
	}

	public boolean isUserLookedIntoSkat() {

		return userPickedUpSkat;
	}

	public boolean isHandFull() {

		return cardPanel.getCardCount() == maxCardCount;
	}

	boolean isUserPickedUpSkat() {
		return userPickedUpSkat;
	}

	void setAnnouncePanel(GameAnnouncePanel announcePanel) {

		this.announcePanel = announcePanel;
	}

	void setRamschPanel(SchieberamschContextPanel ramschPanel) {

		this.ramschPanel = ramschPanel;
	}

}
