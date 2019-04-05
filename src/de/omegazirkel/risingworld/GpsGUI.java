/****************************
	G P S  -  A Java plug-in for Rising World.

	GpsGUI.java - The GUI Panel to interact with the GUI engine

	Originally created by : Maurizio M. Gavioli 2016-10-20

	(C) Maurizio M. Gavioli (a.k.a. Miwarre), 2016
	Licensed under the Creative Commons by-sa 3.0 license (see http://creativecommons.org/licenses/by-sa/3.0/ for details)

*****************************/

package de.omegazirkel.risingworld;

import com.vistamaresoft.rwgui.GuiDialogueBox;
import com.vistamaresoft.rwgui.GuiInputDlgBox;
import com.vistamaresoft.rwgui.GuiLayout;
import com.vistamaresoft.rwgui.RWGui;
import com.vistamaresoft.rwgui.RWGui.RWGuiCallback;
import java.util.List;
import net.risingworld.api.gui.GuiImage;
import net.risingworld.api.gui.GuiLabel;
import net.risingworld.api.objects.Player;
import net.risingworld.api.utils.ImageInformation;

class GpsGUI extends GuiDialogueBox {
	// CONSTANTS
	//
	private static final int BUTTONOFF_ID = 1;
	private static final int BUTTGOTO_ID = 2;
	private static final int BUTTHOMESET_ID = 3;
	private static final int BUTTHOMESHOW_ID = 4;
	private static final int BUTTWPDEL_ID = 5;
	private static final int BUTTWPSET_ID = 6;
	private static final int BUTTWPSHOW_ID = 7;
	private static final int BUTTWPHIDE_ID = 8;
	private static final int BUTTTARGETSHOW_ID = 9;
	// private static final int BUTTWPIMPORT_ID =10;
	private static final int BUTTPREV_ID = 11;
	private static final int BUTTNEXT_ID = 12;
	private static final int NEWWPNAME_ID = 13;
	private static final int IMPORTWPNAME_ID = 14;

	private static final int BUTTONOFF_ICN = 0;
	private static final int BUTTGOTO_ICN = 1;
	private static final int BUTTHOMESET_ICN = 2;
	private static final int BUTTHOMESHOW_ICN = 3;
	private static final int BUTTHOMEHIDE_ICN = 4;
	private static final int BUTTWPDEL_ICN = 5;
	private static final int BUTTWPSET_ICN = 6;
	private static final int BUTTWPSHOW_ICN = 7;
	private static final int BUTTWPHIDE_ICN = 8;
	private static final int BUTTTARGETSHOW_ICN = 9;
	// private static final int BUTTWPIMPORT_ICN =10;
	// private static final int NUM_OF_IMGINFO = 11;
	private static final int NUM_OF_IMGINFO = 10;

	private static final float PANEL_XPOS = 0.5f;
	private static final int BUTTON_SIZE = 32;

	private static final int STATE_NONE = 0;
	private static final int STATE_WPLIST = 1;
	//
	// FIELDS
	//
	private final GuiImage buttGoto; // the "go to way point" button
	private final GuiImage buttHomeShow; // the "show/hide home" button
	private final GuiImage buttWpDel; // the "delete home/wp" button
	private final GuiImage buttWpSet; // the "set way point" button
	private final GuiImage buttWpShow; // the "show waypoint" button
	private final GuiImage buttWpHide; // the "hide waypoint" button
	private final GuiImage buttTargetShow; // the "show target" button
	// private final GuiImage buttWpImport; // the "import waypoint" button
	private final GuiImage buttonNext; // the "next wp" button
	private final GuiImage buttonPrev; // the "prev wp" button
	private int currWp; // the current wp displayed in the GUI
	private final GuiLabel labelWp; // the label with current way point data

	RWGuiCallback dlgHandler;
	Waypoint[] globalWps;
	int globalWpIdx;
	int state;

	GPS Gps;

	private static final ImageInformation[] icons = new ImageInformation[NUM_OF_IMGINFO];
	private static final String[] iconPaths = { "/assets/onoff.png", "/assets/goto.png", "/assets/homeset.png",
			"/assets/homeshow.png", "/assets/homehide.png", "/assets/del.png", "/assets/wpset.png",
			"/assets/wpshow.png", "/assets/wphide.png", "/assets/targetshow.png" };

	public GpsGUI(GPS plugin, Player player, float infoYPos, int currWp) {
		super(plugin, "G P S", RWGui.LAYOUT_VERT, null);
		Gps = plugin;
		dlgHandler = new DlgHandler();
		setCallback(dlgHandler);
		state = STATE_NONE;
		setPosition(PANEL_XPOS, infoYPos + (infoYPos < 0.5f ? 0.1f : -0.25f), true);
		this.currWp = currWp;
		// the wp description
		labelWp = new GuiLabel(0, 0, false);
		addChild(labelWp);
		// the PREV / NEXT buttons
		GuiLayout locLayout = addNewLayoutChild(RWGui.LAYOUT_HORIZ, RWGui.LAYOUT_H_SPREAD | RWGui.LAYOUT_V_TOP);
		buttonPrev = new GuiImage(0, 0, false, RWGui.BUTTON_SIZE, RWGui.BUTTON_SIZE, false);
		locLayout.addChild(buttonPrev, BUTTPREV_ID);
		RWGui.setImage(buttonPrev, RWGui.ICN_ARROW_LEFT);
		buttonNext = new GuiImage(0, 0, false, RWGui.BUTTON_SIZE, RWGui.BUTTON_SIZE, false);
		RWGui.setImage(buttonNext, RWGui.ICN_ARROW_RIGHT);
		locLayout.addChild(buttonNext, BUTTNEXT_ID);
		// The BUTTONS
		locLayout = addNewLayoutChild(RWGui.LAYOUT_HORIZ, RWGui.LAYOUT_H_LEFT | RWGui.LAYOUT_V_TOP);
		locLayout.setPadding(RWGui.DEFAULT_PADDING * 2);
		// load button icons, if not loaded yet
		String pluginPath = plugin.getPath();
		if (icons[0] == null)
			for (int i = BUTTONOFF_ICN; i < NUM_OF_IMGINFO; i++)
				icons[i] = new ImageInformation(pluginPath + iconPaths[i]);
		GuiImage image = new GuiImage(icons[BUTTONOFF_ICN], 0, 0, false, BUTTON_SIZE, BUTTON_SIZE, false);
		locLayout.addChild(image, BUTTONOFF_ID);
		buttGoto = new GuiImage(icons[BUTTGOTO_ICN], 0, 0, false, BUTTON_SIZE, BUTTON_SIZE, false);
		locLayout.addChild(buttGoto, BUTTGOTO_ID);
		image = new GuiImage(icons[BUTTHOMESET_ICN], 0, 0, false, BUTTON_SIZE, BUTTON_SIZE, false);
		locLayout.addChild(image, BUTTHOMESET_ID);
		buttHomeShow = new GuiImage(icons[BUTTHOMESHOW_ICN], 0, 0, false, BUTTON_SIZE, BUTTON_SIZE, false);
		locLayout.addChild(buttHomeShow, BUTTHOMESHOW_ID);
		buttWpDel = new GuiImage(icons[BUTTWPDEL_ICN], 0, 0, false, BUTTON_SIZE, BUTTON_SIZE, false);
		locLayout.addChild(buttWpDel, BUTTWPDEL_ID);
		buttWpSet = new GuiImage(icons[BUTTWPSET_ICN], 0, 0, false, BUTTON_SIZE, BUTTON_SIZE, false);
		locLayout.addChild(buttWpSet, BUTTWPSET_ID);
		buttWpShow = new GuiImage(icons[BUTTWPSHOW_ICN], 0, 0, false, BUTTON_SIZE, BUTTON_SIZE, false);
		locLayout.addChild(buttWpShow, BUTTWPSHOW_ID);
		buttWpHide = new GuiImage(icons[BUTTWPHIDE_ICN], 0, 0, false, BUTTON_SIZE, BUTTON_SIZE, false);
		locLayout.addChild(buttWpHide, BUTTWPHIDE_ID);
		buttTargetShow = new GuiImage(icons[BUTTTARGETSHOW_ICN], 0, 0, false, BUTTON_SIZE, BUTTON_SIZE, false);
		locLayout.addChild(buttTargetShow, BUTTTARGETSHOW_ID);

		updateControls(player);
	}

	// ********************
	// HANDLERS
	// ********************

	private class DlgHandler implements RWGuiCallback {
		@Override
		public void onCall(Player player, int id, Object data) {
			String lang = player.getSystemLanguage();
			Waypoint[] waypoints = (Waypoint[]) player.getAttribute(Gps.key_gpsWpList);
			boolean isCurrWpDefined = false;
			if (waypoints != null)
				isCurrWpDefined = waypoints[currWp] != null;

			switch (id) {
			case RWGui.ABORT_ID:
				switch (state) {
				case STATE_NONE:
					player.setAttribute(Gps.key_gpsGUIcurrWp, currWp);
					break;
				case STATE_WPLIST:
					state = STATE_NONE;
					break;
				}
				return;
			case BUTTONOFF_ID:
				Gps.setGPSShow(player, !(boolean) player.getAttribute(Gps.key_gpsShow));
				return;
			case BUTTPREV_ID:
				currWp--;
				if (currWp < 0) // wrap to last wp
					currWp = Gps.MAX_WP;
				break;
			case BUTTNEXT_ID:
				currWp++;
				if (currWp > Gps.MAX_WP) // wrap to first wp
					currWp = 0;
				break;
			case BUTTGOTO_ID:
				if (isCurrWpDefined)
					Gps.teleportToWp(player, currWp);
				return;
			case BUTTHOMESET_ID:
				Gps.setHome(player);
				Gps.setGpsText(player);
				break;
			case BUTTHOMESHOW_ID:
				Gps.setShowHome(player);
				break;
			case BUTTWPDEL_ID:
				Gps.deleteWp(player, currWp);
				Gps.setGpsText(player);
				break;
			case BUTTWPSET_ID:
				String defaultText = (waypoints[currWp] != null) ? waypoints[currWp].name : null;
				push(player, new GuiInputDlgBox(plugin, player, GPS.t.get("GPS_010", lang), GPS.t.get("GPS_011", lang),
						defaultText, NEWWPNAME_ID, this));
				return;
			case BUTTWPSHOW_ID:
				if (currWp != Gps.HOME_WP && isCurrWpDefined)
					Gps.setShowWp(player, currWp);
				break;
			case BUTTWPHIDE_ID:
				Gps.setShowWp(player, 0);
				break;
			case BUTTTARGETSHOW_ID:
				List<Waypoint> targets = (List<Waypoint>) player.getAttribute(Gps.key_gpsTargetList);
				if (targets != null && !targets.isEmpty())
					Gps.setShowWp(player, Gps.TARGET_ID);
				break;
			/*
			 * GLOBAL WAYPOINT HAVE BEEN REMOVED case BUTTWPIMPORT_ID:
			 * listGlobalWps(player); return;
			 */
			case NEWWPNAME_ID:
				if (data != null && ((String) data).length() > 0)
					Gps.setWp(player, currWp, (String) data);
				break;
			/*
			 * GLOBAL WAYPOINT HAVE BEEN REMOVED case IMPORTWPNAME_ID: if (data != null &&
			 * ((String)data).length() > 0) Db.setWp(player, currWp,
			 * globalWps[globalWpIdx].pos, (String)data); break;
			 */
			default:
				return;
			}
			updateControls(player);
		}
	}

	/*
	 * GLOBAL WAYPOINT HAVE BEEN REMOVED private class MenuHandler implements
	 * RWGuiCallback {
	 * 
	 * @Override public void onCall(Player player, int id, Object data) { if (id !=
	 * RWGui.ABORT_ID && globalWps != null && id >=0 && id < globalWps.length) {
	 * globalWpIdx = id; push(player, new GuiInputDlgBox(plugin, player,
	 * Msgs.msg[Msgs.txt_wpNameTitle], Msgs.msg[Msgs.txt_wpNameCapt],
	 * globalWps[globalWpIdx].name, IMPORTWPNAME_ID, dlgHandler)); } } } /* private
	 * void listGlobalWps(Player player) { globalWps = Db.getGlobalWps(); if
	 * (globalWps == null || globalWps.length < 1) {
	 * player.sendTextMessage("No global waypoints found"); return; } GuiMenu wpMenu
	 * = new GuiMenu(plugin, "Global waypoint to import", new MenuHandler(), true);
	 * for (int i = 0; i < globalWps.length; i++) wpMenu.addChild(globalWps[i].name,
	 * i, null); push(player, wpMenu); }
	 */
	private void updateControls(Player player) {
		String lang = player.getSystemLanguage();
		Waypoint[] waypoints = (Waypoint[]) player.getAttribute(Gps.key_gpsWpList);
		int wpShown = (int) player.getAttribute(Gps.key_gpsWpShow);
		Waypoint wp;
		if (waypoints != null) {
			wp = waypoints[currWp];
			// wp text and GOTO button
			if (wp != null) {
				// set waypoint data text and GOTO button status
				String txt;
				if ((txt = wp.toString()) != null) {
					labelWp.setText(txt); // if curr. wp is defined, show data
					buttGoto.setVisible(true);
				}
			} else {
				labelWp.setText("" + currWp + GPS.t.get("GPS_008", lang)); // otherwise, show no data
				buttGoto.setVisible(false);
			}
			// set HOME SET and HOME SHOW/HIDE buttons
			boolean isDef = (waypoints[0] != null); // if HOME defined?
			boolean isShown = (boolean) player.getAttribute(Gps.key_gpsHomeShow);
			// set HOME SHOW/HIDE text depending on home being currently shown or not
			buttHomeShow.setImage(isShown ? icons[BUTTHOMEHIDE_ICN] : icons[BUTTHOMESHOW_ICN]);
			// enable/disable HOME SHOW/HIDE depending on home being defined or not
			buttHomeShow.setVisible(isDef);
			// Home/WP DELETE button
			buttWpDel.setVisible(wp != null);
			// WP buttons
			isShown = (wpShown != 0);
			// enable/disable WP SET/IMPORT depending on the current wp being Home or not
			buttWpSet.setVisible(currWp > 0);
			// buttWpImport.setVisible(currWp > 0);
			// enable/disable WP SHOW/SHARE depending on curr. wp being defined or not
			buttWpShow.setVisible(currWp > 0 && wp != null);
			buttTargetShow.setVisible(wp != null);
			// enable/disable WP HIDE depending on some wp being shown or not
			buttWpHide.setVisible(isShown);
		}
		// Targets: enable/disable TARGET SHOW depending there is some target
		// and targets are currently not shown
		List<Waypoint> targets = (List<Waypoint>) player.getAttribute(Gps.key_gpsTargetList);
		buttTargetShow.setVisible(wpShown != Gps.TARGET_ID && targets != null && !targets.isEmpty());
	}

}
