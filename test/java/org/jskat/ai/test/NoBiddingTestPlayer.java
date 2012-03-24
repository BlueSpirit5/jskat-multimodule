/**
 * JSkat - A skat program written in Java
 * by Jan Schäfer and Markus J. Luzius
 *
 * Version 0.11.0-SNAPSHOT
 * Copyright (C) 2012-03-13
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
package org.jskat.ai.test;
import org.jskat.ai.rnd.AIPlayerRND;

/**
 * Test player that does not bid, it makes random moves during other game phases
 */
public class NoBiddingTestPlayer extends AIPlayerRND {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int bidMore(@SuppressWarnings("unused") int nextBidValue) {
		return -1;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean holdBid(@SuppressWarnings("unused") int currBidValue) {
		return false;
	}
}
