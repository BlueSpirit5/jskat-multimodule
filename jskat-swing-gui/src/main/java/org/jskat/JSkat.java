/**
 * JSkat - A skat program written in Java
 * by Jan Schäfer, Markus J. Luzius and Daniel Loreck
 *
 * Version 0.13.0-SNAPSHOT
 * Copyright (C) 2013-05-10
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
package org.jskat;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.SplashScreen;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import org.apache.log4j.PropertyConfigurator;
import org.jskat.control.JSkatMaster;
import org.jskat.data.DesktopSavePathResolver;
import org.jskat.data.JSkatOptions;
import org.jskat.gui.img.JSkatGraphicRepository;
import org.jskat.gui.swing.JSkatViewImpl;
import org.jskat.gui.swing.LookAndFeelSetter;
import org.jskat.util.JSkatResourceBundle;
import org.jskat.util.version.VersionChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main class for JSkat
 */
public class JSkat {

	private static Logger log = LoggerFactory.getLogger(JSkat.class);

	private static String VERSION = "0.13.0"; //$NON-NLS-1$

	/**
	 * Main method
	 * 
	 * @param args
	 *            Command line arguments
	 */
	public static void main(final String[] args) {

		PropertyConfigurator.configure(ClassLoader
				.getSystemResource("org/jskat/config/log4j.properties")); //$NON-NLS-1$
		log.debug("Welcome to JSkat!"); //$NON-NLS-1$

		initializeOptions();

		JSkatResourceBundle.instance();

		trySettingNimbusLookAndFeel();

		final SplashScreen splash = SplashScreen.getSplashScreen();
		if (splash == null) {
			System.out.println("SplashScreen.getSplashScreen() returned null");
			return;
		}
		Graphics2D g = splash.createGraphics();
		if (g == null) {
			System.out.println("g is null");
			return;
		}
		JSkatMaster jskat = null;
		JSkatViewImpl jskatView = null;
		for (int i = 0; i < 3; i++) {
			renderSplashFrame(g, i);
			splash.update();
			switch (i) {
			case 0:
				jskat = JSkatMaster.instance();
				break;
			case 1:
				JSkatGraphicRepository.instance();
				break;
			case 2:
				jskatView = new JSkatViewImpl();
				jskat.setView(jskatView);
				break;
			}
		}

		splash.close();
		jskatView.setVisible();

		if (JSkatOptions.instance().isShowTipsAtStartUp()) {
			jskat.showWelcomeDialog();
		}

		if (JSkatOptions.instance().isCheckForNewVersionAtStartUp()) {
			jskat.checkJSkatVersion(getVersion(),
					VersionChecker.getLatestVersion());
		}
	}

	/**
	 * Gets the version of JSkat
	 * 
	 * @return Version of JSkat
	 */
	public static String getVersion() {
		return VERSION;
	}

	private static void renderSplashFrame(Graphics2D g, int frame) {
		final JSkatResourceBundle strings = JSkatResourceBundle.instance();
		final String[] frameStrings = {
				strings.getString("splash_init_application"),
				strings.getString("splash_load_card_sets"),
				strings.getString("splash_look_for_ai_players") };
		g.setComposite(AlphaComposite.Clear);
		g.fillRect(10, 180, 200, 40);
		g.setPaintMode();
		g.setColor(Color.BLACK);
		g.drawString(frameStrings[frame] + "...", 10, 190);
	}

	private static void initializeOptions() {
		JSkatOptions.instance(new DesktopSavePathResolver());
	}

	private static void trySettingNimbusLookAndFeel() {
		for (LookAndFeelInfo laf : UIManager.getInstalledLookAndFeels()) {
			if ("Nimbus".equals(laf.getName())) { //$NON-NLS-1$
				LookAndFeelSetter.setLookAndFeel();
			}
		}
	}
}
