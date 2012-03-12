package org.jskat.gui.iss;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ActionMap;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jskat.data.iss.ChatMessage;
import org.jskat.gui.AbstractTabPanel;
import org.jskat.gui.LayoutFactory;
import org.jskat.gui.action.JSkatAction;
import org.jskat.util.JSkatResourceBundle;

/**
 * Chat panel for ISS
 */
class ChatPanel extends JPanel implements ChangeListener {

	private static final long serialVersionUID = 1L;
	static Log log = LogFactory.getLog(ChatPanel.class);

	JTextField inputLine;
	private Map<String, JTextArea> chats;
	private JTabbedPane chatTabs;

	String activeChatName;

	/**
	 * Constructor
	 */
	ChatPanel(AbstractTabPanel parent) {

		initPanel(parent.getActionMap());
	}

	private void initPanel(ActionMap actions) {

		JSkatResourceBundle strings = JSkatResourceBundle.instance();

		setLayout(LayoutFactory.getMigLayout("fill", "fill", "[grow][shrink]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		setMinimumSize(new Dimension(100, 100));
		setPreferredSize(new Dimension(100, 100));

		chats = new HashMap<String, JTextArea>();
		chatTabs = new JTabbedPane();
		chatTabs.setTabPlacement(SwingConstants.BOTTOM);
		chatTabs.setAutoscrolls(true);
		chatTabs.addChangeListener(this);
		add(chatTabs, "grow, wrap"); //$NON-NLS-1$

		addNewChat(strings.getString("lobby")); //$NON-NLS-1$

		inputLine = new JTextField(20);
		inputLine.setAction(actions.get(JSkatAction.SEND_CHAT_MESSAGE));
		inputLine.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				String message = ChatPanel.this.inputLine.getText();
				log.debug("Chat message: " + message); //$NON-NLS-1$

				ChatMessage chatMessage = new ChatMessage(ChatPanel.this.activeChatName, message);
				e.setSource(chatMessage);
				// fire event again
				ChatPanel.this.inputLine.dispatchEvent(e);

				ChatPanel.this.inputLine.setText(null);
			}
		});
		add(inputLine, "growx"); //$NON-NLS-1$
	}

	JTextArea addNewChat(String name) {

		JTextArea chat = getChat();
		chats.put(name, chat);
		JScrollPane scrollPane = new JScrollPane(chat);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setName(name);

		chatTabs.add(name, scrollPane);
		chatTabs.setSelectedIndex(chatTabs.getComponentCount() - 1);
		activeChatName = name;

		return chat;
	}

	private JTextArea getChat() {

		JTextArea chat = new JTextArea(7, 50);
		chat.setEditable(false);
		chat.setLineWrap(true);

		return chat;
	}

	void appendMessage(ChatMessage message) {

		log.debug("Appending chat message: " + message); //$NON-NLS-1$

		JTextArea chat = chats.get(message.getChatName());

		if (chat == null) {
			// new chat --> create chat text area first
			chat = addNewChat(message.getChatName());
		}

		chat.append(message.getMessage() + '\n');
	}

	@Override
	public void stateChanged(ChangeEvent e) {

		if (e.getSource() instanceof JTabbedPane) {

			JTabbedPane tabs = (JTabbedPane) e.getSource();
			Component tab = tabs.getSelectedComponent();

			activeChatName = tab.getName();
			log.debug("Chat " + activeChatName + " activated."); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	public void setFocus() {
		inputLine.requestFocus();
	}
}
