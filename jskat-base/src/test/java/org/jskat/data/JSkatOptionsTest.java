/**
 * JSkat - A skat program written in Java
 * by Jan Schäfer, Markus J. Luzius and Daniel Loreck
 *
 * Version 0.13.0-SNAPSHOT
 * Copyright (C) 2013-05-10
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
package org.jskat.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Locale;

import org.jskat.AbstractJSkatTest;
import org.jskat.data.JSkatOptions.SupportedLanguage;
import org.jskat.data.SkatTableOptions.RuleSet;
import org.jskat.gui.img.CardFace;
import org.jskat.gui.img.CardSet;
import org.junit.Test;

/**
 * Tests for JSkat options
 */
public class JSkatOptionsTest extends AbstractJSkatTest {

	/**
	 * Tests the default values
	 */
	@Test
	public void testDefaultValues() {

		JSkatOptions options = JSkatOptions.instance();
		options.setDefaultProperties(new DesktopSavePathResolver());

		assertTrue(options.isShowTipsAtStartUp().booleanValue());
		assertFalse(options.isCheckForNewVersionAtStartUp().booleanValue());

		if (Locale.getDefault().getLanguage()
				.equals(Locale.GERMAN.getLanguage())) {
			assertEquals(SupportedLanguage.GERMAN, options.getLanguage());
		} else {
			assertEquals(SupportedLanguage.ENGLISH, options.getLanguage());
		}
		assertEquals(CardSet.ISS_TOURNAMENT, options.getCardSet());
		assertEquals(CardFace.TOURNAMENT, options.getCardSet().getCardFace());
		assertEquals(options.getDefaultSaveDir(), options.getSavePath());
		assertEquals(Integer.valueOf(2000), options.getTrickRemoveDelayTime());
		assertFalse(options.isTrickRemoveAfterClick().booleanValue());
		assertFalse(options.isGameShortCut().booleanValue());

		// rule defaults
		assertEquals(RuleSet.ISPA, options.getRules());

		assertFalse(options.isPlayContra());
		assertFalse(options.isPlayContra(true));
		assertTrue(options.isPlayContra(false));

		assertFalse(options.isContraAfterBid18());
		assertFalse(options.isContraAfterBid18(true));
		assertTrue(options.isContraAfterBid18(false));

		assertFalse(options.isPlayBock());
		assertFalse(options.isPlayBock(true));
		assertTrue(options.isPlayBock(false));

		assertFalse(options.isPlayRamsch());
		assertFalse(options.isPlayRamsch(true));
		assertTrue(options.isPlayRamsch(false));

		assertFalse(options.isPlayRevolution());
		assertFalse(options.isPlayRevolution(true));
		assertFalse(options.isPlayRevolution(false));

		// ISS defaults
		assertEquals("skatgame.net", options.getIssAddress()); //$NON-NLS-1$
		assertEquals(Integer.valueOf(7000), options.getIssPort());
	}
}
