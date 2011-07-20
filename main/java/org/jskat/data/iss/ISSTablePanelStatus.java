/**
 * JSkat - A skat program written in Java
 * Copyright by Jan Schäfer and Markus J. Luzius
 *
 * Version: 0.8.0
 * Build date: 2011-07-20 21:16:11
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

package org.jskat.data.iss;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds all data for a ISS table
 */
public class ISSTablePanelStatus {

	private int maxPlayers;

	Map<String, ISSPlayerStatus> playerInfos = new HashMap<String, ISSPlayerStatus>();

	private String loginName;

	/**
	 * Adds a player to the status<br>
	 * If there is a player with the same name, already in the map it's status
	 * is updated instead
	 * 
	 * @param player
	 *            Player position
	 * @param status
	 *            Player status
	 */
	public void addPlayer(String playerName, ISSPlayerStatus status) {

		this.playerInfos.put(playerName, status);
	}

	public int getNumberOfPlayers() {

		return this.playerInfos.size();
	}

	public Map<String, ISSPlayerStatus> getPlayerInformations() {

		return playerInfos;
	}

	public ISSPlayerStatus getPlayerInformation(String playerName) {

		return playerInfos.get(playerName);
	}

	public int getMaxPlayers() {

		return this.maxPlayers;
	}

	public void setMaxPlayers(int maxPlayers) {

		this.maxPlayers = maxPlayers;
	}

	public void setLoginName(String newLoginName) {

		loginName = newLoginName;
	}

	public String getLoginName() {

		return loginName;
	}
}
