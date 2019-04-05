package de.omegazirkel.risingworld;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import de.omegazirkel.risingworld.tools.Colors;
import de.omegazirkel.risingworld.tools.FileChangeListener;
import de.omegazirkel.risingworld.tools.I18n;
import de.omegazirkel.risingworld.tools.db.SQLite;
import net.risingworld.api.Plugin;
import net.risingworld.api.Server;
import net.risingworld.api.events.EventMethod;
import net.risingworld.api.events.Listener;
import net.risingworld.api.events.player.PlayerChangePositionEvent;
import net.risingworld.api.events.player.PlayerCommandEvent;
import net.risingworld.api.events.player.PlayerConnectEvent;
import net.risingworld.api.events.player.PlayerDisconnectEvent;
import net.risingworld.api.events.player.PlayerSpawnEvent;
import net.risingworld.api.gui.Font;
import net.risingworld.api.gui.GuiLabel;
import net.risingworld.api.gui.PivotPosition;
import net.risingworld.api.objects.Player;
import net.risingworld.api.utils.Vector3f;

public class GPS extends Plugin implements Listener, FileChangeListener {

	static final String pluginVersion = "1.5.0";
	static final String pluginName = "GPS";
	static final String pluginCMD = "gps";

	static final de.omegazirkel.risingworld.tools.Logger log = new de.omegazirkel.risingworld.tools.Logger("[OZ.GPS]");
	static final Colors c = Colors.getInstance();
	static I18n t = null;
	static SQLite db = null;

	static final float gpsXPosDef = 0.5f;
	static final float gpsHintYPos = -0.25f;

	// Settings
	static int logLevel = 0;
	static boolean restartOnUpdate = true;
	static boolean sendPluginWelcome = false;
	// GPS
	static boolean allowTpToWp = false; // whether teleporting to waypoints (in addition to home) is possible or not
	static boolean coordNativeFormat = false;
	static float gpsYPos = 0.1f;
	static int wpDispLen = 8; // the max length of waypoint names to display on screen
	static int wpHdgPrecis = 5; // the waypoint radial delta below which route corrections arrows are not
								// displayed

	// END Settings

	// KEYS FOR PLAYER ATTRIBUTES
	static final String key_gpsShow = "de.oz.gpsShow";
	static final String key_gpsGUIcurrWp = "de.oz.gpsGUIcurrWp";
	static final String key_gpsHomeShow = "de.oz.gpsHomeShow";
	static final String key_gpsLabel = "de.oz.gpsLabel";
	static final String key_gpsHint = "de.oz.gpsHint";
	static final String key_gpsTargetList = "de.oz.gpsTargetList";
	static final String key_gpsWpList = "de.oz.gpsWpList";
	static final String key_gpsWpShow = "de.oz.gpsWpShow";

	// CONSTANTS
	static final double RAD2DEG = 180.0 / Math.PI;
	public static final int HOME_WP = 0; // the index of the home waypoint
	public static final int MAX_WP = 15; // the max waypoint index
	public static final int MIN_WP = 0; // the min waypoint index (including home)
	public static final int MIN_WP_PROPER = 1; // the min waypoint index (EXCLUDING home)
	public static final int TARGET_ID = -1; // the wp ID common to all targets
	static final float TARGET_MIN_DIST = 9; // the distance (in blocks) below which a target has been reached
	static final int FONT_SIZE = 18; // the size of the info window font
	static final int HINT_SIZE = 13; // the size of the info window font

	static boolean flagRestart = false;

	@Override
	public void onEnable() {
		registerEventListener(this);
		this.initSettings();
		t = t != null ? t : new I18n(this, logLevel);
		db = db != null ? db : new SQLite(this, logLevel);
		initDatabase();
		log.out(pluginName + " Plugin is enabled", 10);
	}

	@Override
	public void onDisable() {
		db.destroy();
		log.out(pluginName + " Plugin is disabled", 10);
	}

	@EventMethod
	public void onPlayerConnect(PlayerConnectEvent event) {
		initPlayer(event.getPlayer());
	}

	@EventMethod
	public void onPlayerSpawn(PlayerSpawnEvent event) {
		if (sendPluginWelcome) {
			Player player = event.getPlayer();
			String lang = player.getSystemLanguage();
			player.sendTextMessage(t.get("MSG_PLUGIN_WELCOME", lang));
		}
		setGpsText(event.getPlayer());
	}

	@EventMethod
	public void onPlayerDisconnect(PlayerDisconnectEvent event) {
		Server server = getServer();

		if (flagRestart) {
			int playersLeft = server.getPlayerCount() - 1;
			if (playersLeft == 0) {
				log.out("Last player left the server, shutdown now due to flagRestart is set", 100); // INFO LEVEL
				server.shutdown();
			} else if (playersLeft > 1) {
				this.broadcastMessage("BC_PLAYER_REMAIN", playersLeft);
			}
		}
	}

	@EventMethod
	public void onPlayerCommand(PlayerCommandEvent event) {
		Player player = event.getPlayer();
		String command = event.getCommand();
		String lang = event.getPlayer().getSystemLanguage();
		String[] cmd = command.split(" ");

		if (cmd[0].equals("/sethome"))
			setHome(player);

		if (cmd[0].equals("/home"))
			teleportToWp(player, HOME_WP);

		if (cmd[0].equals("/" + pluginCMD)) {
			if (cmd.length < 2) {
				// Open GUI
				mainGui(player);
				return;
			}

			String option = cmd[1];
			switch (option) {

			case "info":
				String infoMessage = t.get("CMD_INFO", lang);
				player.sendTextMessage(c.okay + pluginName + ":> " + infoMessage);
				break;
			case "help":
				String helpMessage = t.get("CMD_HELP", lang)
						.replace("PH_CMD_GUI", c.command + "/" + pluginCMD + c.text)
						.replace("PH_CMD_HELP", c.command + "/" + pluginCMD + " help" + c.text)
						.replace("PH_CMD_INFO", c.command + "/" + pluginCMD + " info" + c.text)
						.replace("PH_CMD_STATUS", c.command + "/" + pluginCMD + " status" + c.text);
				player.sendTextMessage(c.okay + pluginName + ":> " + helpMessage);
				break;
			case "status":
				String statusMessage = t.get("CMD_STATUS", lang).replace("PH_VERSION", c.okay + pluginVersion + c.text)
						.replace("PH_LANGUAGE",
								c.comment + player.getLanguage() + " / " + player.getSystemLanguage() + c.text)
						.replace("PH_USEDLANG", c.info + t.getLanguageUsed(lang) + c.text)
						.replace("PH_LANG_AVAILABLE", c.okay + t.getLanguageAvailable() + c.text);
				player.sendTextMessage(c.okay + pluginName + ":> " + statusMessage);
				break;
			default:
				player.sendTextMessage(c.error + pluginName + ":> " + c.text
						+ t.get("MSG_CMD_ERR_UNKNOWN_OPTION", lang).replace("PH_OPTION", option));
				break;
			}
		}

	}
	// -------------------------------------------------------------------------

	private void initDatabase() {
		db.execute("CREATE TABLE IF NOT EXISTS `waypoints` (" + "`player_id`   INTEGER  NOT NULL DEFAULT ( 0 ),"
				+ "`wp_name`     CHAR(64) NOT NULL DEFAULT ('[NoName]')," + "`wp_id`       INTEGER  NOT NULL,"
				+ "`wp_x`        INTEGER  NOT NULL DEFAULT ( 0 )," + "`wp_y`        INTEGER  NOT NULL DEFAULT ( 0 ),"
				+ "`wp_z`        INTEGER  NOT NULL DEFAULT ( 0 )," + "UNIQUE (player_id, wp_id) ON CONFLICT REPLACE"
				+ ");");
	}

	/**
	 * Loads from the DB the Home/way-point data for a player and caches them in
	 * player attributes.
	 * 
	 * @param player
	 */
	private void loadPlayer(Player player) {
		Waypoint waypoints[] = new Waypoint[GPS.MAX_WP + 1];
		player.setAttribute(GPS.key_gpsWpList, waypoints);
		try (ResultSet result = db.executeQuery(
				"SELECT * FROM `waypoints` WHERE `player_id` = '" + player.getDbID() + "' ORDER BY `wp_id`;")) {
			while (result.next()) {
				int wpIdx = result.getInt("wp_id");
				if (wpIdx < GPS.MIN_WP || wpIdx >= GPS.MAX_WP)
					continue;
				waypoints[result.getInt("wp_id")] = new Waypoint(result.getInt("wp_id"), result.getString("wp_name"),
						result.getFloat("wp_x"), result.getFloat("wp_y"), result.getFloat("wp_z"));
			}
		} catch (SQLException e) {
			log.out(e.getMessage(), 999);
		}
	}

	/**
	 * Inserts into the DB (or replace if already present) data for a way-point at
	 * arbitrary position, also updating the player attribute cache.
	 * 
	 * @param player
	 * @param wpIndex
	 * @param wpName
	 */
	public void setWp(Player player, int wpIndex, String wpName) {
		setWp(player, wpIndex, player.getPosition(), wpName);
	}

	/**
	 * Inserts into the DB (or replace if already present) data for a way-point at
	 * arbitrary position, also updating the player attribute cache.
	 * 
	 * @param player
	 * @param wpIdx
	 * @param pos
	 * @param wpName
	 */
	public void setWp(Player player, int wpIdx, Vector3f pos, String wpName) {
		if (wpIdx < GPS.MIN_WP || wpIdx > GPS.MAX_WP) {
			return;
		}
		int playerId = player.getDbID();
		String lang = player.getSystemLanguage();
		// update DB
		db.executeUpdate("INSERT OR REPLACE INTO waypoints (player_id,wp_name,wp_id,wp_x,wp_y,wp_z) VALUES ('"
				+ playerId + "','" + wpName + "'," + wpIdx + "," + pos.x + "," + pos.y + "," + pos.z + ");");
		// update player cache
		Waypoint wp = new Waypoint(wpIdx, wpName, pos.x, pos.y, pos.z);
		((Waypoint[]) player.getAttribute(GPS.key_gpsWpList))[wpIdx] = wp;

		if (wpIdx == 0) {
			player.sendTextMessage(c.okay + pluginName + ":> " + c.text + t.get("GPS_003", lang));
		} else {
			player.sendTextMessage(c.okay + pluginName + ":> " + c.text
					+ t.get("GPS_004", lang).replace("PH_INDEX", wpIdx + "").replace("PH_WP_NAME", wpName + ""));
		}

	}

	public void deleteWp(Player player, int wpIdx) {
		String lang = player.getSystemLanguage();
		if (wpIdx < GPS.MIN_WP || wpIdx >= GPS.MAX_WP) {
			player.sendTextMessage(c.error + pluginName + ":> " + c.text
					+ t.get("GPS_001", lang).replace("PH_MIN", MIN_WP + "").replace("PH_MAX", MAX_WP + ""));
			return;
		}
		int playerId = player.getDbID();
		// update DB
		db.executeUpdate("DELETE FROM waypoints WHERE player_id = '" + playerId + "' AND wp_id=" + wpIdx + ";");
		// update player cache
		((Waypoint[]) player.getAttribute(GPS.key_gpsWpList))[wpIdx] = null;

		if (wpIdx == 0) {
			player.sendTextMessage(c.warning + pluginName + ":> " + c.text + t.get("GPS_005", lang));
		} else {
			player.sendTextMessage(
					c.warning + pluginName + ":> " + c.text + t.get("GPS_006", lang).replace("PH_INDEX", wpIdx + ""));
		}
	}

	/**
	 * Inserts into the DB (or replace if already present) Home data at current
	 * player position, also updating the player attribute cache.
	 * 
	 * @param player
	 */
	public void setHome(Player player) {
		String lang = player.getSystemLanguage();
		setWp(player, 0, t.get("GPS_007", lang));
	}

	@EventMethod
	public void onPlayerChangePosition(PlayerChangePositionEvent event) {
		setGpsText(event.getPlayer());
	}

	public void mainGui(Player player) {
		int currWp = 0;
		if (player.hasAttribute(key_gpsGUIcurrWp))
			currWp = (int) player.getAttribute(key_gpsGUIcurrWp);
		GpsGUI gui = new GpsGUI(this, player, gpsYPos, currWp);
		gui.show(player);
	}

	public void setGPSShow(Player player, boolean show) {
		player.setAttribute(key_gpsShow, show);
		GuiLabel labelgpsInfo = (GuiLabel) player.getAttribute(key_gpsLabel);
		if (labelgpsInfo != null)
			labelgpsInfo.setVisible(show);
		setGpsText(player); // update displayed text
	}

	public void setShowHome(Player player) {
		player.setAttribute(key_gpsHomeShow, !(boolean) player.getAttribute(key_gpsHomeShow));
		setGpsText(player); // update displayed text
	}

	public void setShowWp(Player player, Integer index) {
		String lang = player.getSystemLanguage();
		// check index is there and is legal
		if (index == null || index < TARGET_ID || index > MAX_WP) {
			player.sendTextMessage(c.error + pluginName + ":> " + c.text
					+ t.get("GPS_001", lang).replace("PH_MIN", MIN_WP + "").replace("PH_MAX", MAX_WP + ""));
			return;
		}
		// if not turning off (index = 0), check that waypoint exists
		if (index == TARGET_ID && player.getAttribute(key_gpsTargetList) == null
				|| index > 0 && ((Waypoint[]) player.getAttribute(key_gpsWpList))[index] == null) {
			player.sendTextMessage(
					c.error + pluginName + ":> " + c.text + t.get("GPS_002", lang).replace("PH_INDEX", index + ""));
			return;
		}
		player.setAttribute(key_gpsWpShow, index);
		setGpsText(player); // update displayed text
	}

	/**
	 * Adds the given point to the list of targets for player 'player'
	 * 
	 * @param player the player to whom add the target
	 * @param x      the target x coordinate
	 * @param y      the target y coordinate
	 * @param z      the target z coordinate
	 * @param name   a human readable name for the target
	 */
	public void addTarget(Player player, String name, float x, float y, float z) {
		List<Waypoint> targets = (List<Waypoint>) player.getAttribute(key_gpsTargetList);
		if (targets == null) {
			targets = new ArrayList<>();
			player.setAttribute(key_gpsTargetList, targets);
		}
		Waypoint wp = new Waypoint(TARGET_ID, name, x, y, z);
		targets.add(wp);
		setShowWp(player, TARGET_ID);
	}

	/**
	 * Tele-transports to the index-th way-point. The way-point must be defined and,
	 * if different from Home, teleport to way-points must be enabled.
	 * 
	 * @param player the affected player.
	 * @param index  a int from 0 to 9 with the index of the way-point.
	 */
	public void teleportToWp(Player player, Integer index) {
		String lang = player.getSystemLanguage();
		// check index is there and is legal
		if (index == null || index < MIN_WP || index > MAX_WP) {
			player.sendTextMessage(c.error + pluginName + ":> " + c.text
					+ t.get("GPS_001", lang).replace("PH_MIN", MIN_WP + "").replace("PH_MAX", MAX_WP + ""));
			return;
		}
		// check teleporting to waypoint is enabled
		if (index > 0 && !allowTpToWp) {
			player.sendTextMessage(c.error + pluginName + ":> " + c.text + t.get("GPS_000", lang));
			return;
		}
		// check that waypoint exists
		Waypoint wp = ((Waypoint[]) player.getAttribute(key_gpsWpList))[index];
		if (wp == null) {
			player.sendTextMessage(
					c.error + pluginName + ":> " + c.text + t.get("GPS_002", lang).replace("PH_INDEX", index + ""));
			return;
		}
		player.setPosition(wp.pos.x, wp.pos.y, wp.pos.z);
		setGpsText(player); // update displayed text
	}

	/**
	 * Sets the text of the player GPS data text.
	 * 
	 * @param player the affected player
	 */
	void setGpsText(Player player) {
		if (player == null)
			return;
		if (player.getAttribute(key_gpsLabel) == null)
			return;
		GuiLabel labelgpsInfo = (GuiLabel) player.getAttribute(key_gpsLabel);
		if (labelgpsInfo == null)
			return;

		String lang = player.getSystemLanguage();
		// check for targets
		Vector3f playerPos = player.getPosition();
		List<Waypoint> targets = (List<Waypoint>) player.getAttribute(key_gpsTargetList);
		if (targets != null && !targets.isEmpty()) {
			Waypoint wp = targets.get(0);
			float dist = (playerPos.x - wp.pos.x) * (playerPos.x - wp.pos.x)
					+ (playerPos.z - wp.pos.z) * (playerPos.z - wp.pos.z);
			// has the first target in the list been reached?
			if (dist < TARGET_MIN_DIST) {
				// yes, remove it from the list
				targets.remove(0);
				// if list is empty, remove it as an attribute and turn wp display off
				if (targets.isEmpty()) {
					player.setAttribute(key_gpsTargetList, null);
					player.setAttribute(key_gpsWpShow, 0);
				}
			}
		}

		if ((boolean) player.getAttribute(key_gpsShow)) {
			// PLAYER ROTATION
			Vector3f playerRot = player.getViewDirection();
			// x- and z-components of the viewing vector
			double rotX = playerRot.x;
			double rotZ = playerRot.z;
			// if the viewing vector is not horizontal but slanted up or down,
			// the x and z components are shortened: scale them up until
			// they are the catheti of a triangle whose hypotenuse is long 1
			double scale = Math.sqrt(rotX * rotX + rotZ * rotZ);
			double heading;
			if (scale < 0.00001) // avoid division by 0
				heading = 0.0;
			else
				// get the heading angle and convert to degrees
				heading = Math.acos(rotZ / scale) * RAD2DEG;
			// hdg is correct from N (0°) through E (90°) to S (180°)
			// then, when view is toward W, it decreases down to 0° again;
			// when view is toward W, correct by taking the complementary angle
			if (rotX > 0)
				heading = 360 - heading;
			// round to nearest integer and uniform 0° to 360°
			int hdg = (int) Math.floor(heading + 0.5);
			if (hdg == 0)
				hdg = 360;

			// PLAYER POSITION
			int posW = (int) playerPos.x;
			int posE = -posW;
			int posN = (int) playerPos.z;
			int posH = (int) playerPos.y;
			String latDir = "";
			String longDir = "";
			if (!coordNativeFormat) {
				// set N/S and E/W according to signs of coordinates
				latDir = t.get("GPS_012", lang);
				if (posN < 0) {
					posN = -posN;
					latDir = t.get("GPS_014", lang);
				}
				longDir = t.get("GPS_013", lang);
				if (posE < 0) {
					posE = -posE;
					longDir = t.get("GPS_015", lang);
				}
			}
			// OUTPUT: home
			String text = "";
			Waypoint[] wps = (Waypoint[]) player.getAttribute(key_gpsWpList);
			Waypoint home;
			if ((boolean) player.getAttribute(key_gpsHomeShow) && wps != null && (home = wps[HOME_WP]) != null)
				text = home.toString(heading, playerPos) + " | ";
			// main data
			text += String.format("%03d°", hdg)
					+ (!coordNativeFormat ? (" (" + posN + latDir + "," + posE + longDir + ") h" + posH)
							: (" (" + posW + "," + posH + "," + posN + ")"));
			// waypoint
			int wpToShow = (int) player.getAttribute(key_gpsWpShow);
			Waypoint wp = null;
			if (wpToShow == TARGET_ID) {
				if (targets != null && !targets.isEmpty())
					wp = targets.get(0);
			} else if (wpToShow > 0) {
				wp = ((Waypoint[]) player.getAttribute(key_gpsWpList))[wpToShow];
			}
			if (wp != null)
				text += " | " + wp.toString(heading, playerPos);

			labelgpsInfo.setText(text);
		} else
			labelgpsInfo.setText("");
	}

	private void initPlayer(Player player) {
		if (player.getAttribute(key_gpsLabel) == null) {
			String lang = player.getSystemLanguage();
			// The main textual GUI element showing the GPS data
			GuiLabel info = new GuiLabel("", gpsXPosDef, gpsYPos, true);
			info.setColor(0x0000007f);
			info.setFont(Font.DefaultMono);
			info.setFontColor(0xFFFFFFFF);
			info.setFontSize(FONT_SIZE);
			info.setPivot(PivotPosition.Center);
			GuiLabel hint = new GuiLabel(t.get("GPS_009", lang).replace("PH_CMD", "/"+pluginCMD), gpsXPosDef, gpsHintYPos, true);
			hint.setPivot(PivotPosition.Center);
			hint.setFontSize(HINT_SIZE);
			info.addChild(hint);

			// player attributes keeping track of status (whether the GPS data are shown or
			// not
			// and what they should contain)
			player.setAttribute(key_gpsShow, true); // whether the GPS text is shown or not
			player.setAttribute(key_gpsHomeShow, false); // whether the home info is shown or not
			player.setAttribute(key_gpsWpShow, 0); // which waypoint is shown, if any (0 = none)

			loadPlayer(player); // load player-dependent data
			player.addGuiElement(info);
			player.addGuiElement(hint);
			player.setAttribute(key_gpsLabel, info);
		}
	}

	// -------------------------------------------------------------------------

	/** */
	private void initSettings() {
		Properties settings = new Properties();
		FileInputStream in;
		try {
			in = new FileInputStream(getPath() + "/settings.properties");
			settings.load(new InputStreamReader(in, "UTF8"));
			in.close();

			// GPS Stuff
			allowTpToWp = settings.getProperty("allowTpToWp", "false").contentEquals("true");
			coordNativeFormat = settings.getProperty("coordNativeFormat", "false").contentEquals("true");
			gpsYPos = Float.parseFloat(settings.getProperty("gpsYPos", "0.1"));
			wpDispLen = Integer.parseInt(settings.getProperty("wpDispLength", "8"));
			wpHdgPrecis = Integer.parseInt(settings.getProperty("wpHdgPrecis", "5"));

			// fill global values
			logLevel = Integer.parseInt(settings.getProperty("logLevel", "0"));
			sendPluginWelcome = settings.getProperty("sendPluginWelcome", "false").contentEquals("true");

			// restart settings
			restartOnUpdate = settings.getProperty("restartOnUpdate", "false").contentEquals("true");
			log.out(pluginName + " Plugin settings loaded", 10);
		} catch (Exception ex) {
			log.out("Exception on initSettings: " + ex.getMessage(), 100);
		}
	}

	// All stuff for plugin updates

	/**
	 *
	 * @param i18nIndex
	 * @param playerCount
	 */
	private void broadcastMessage(String i18nIndex, int playerCount) {
		getServer().getAllPlayers().forEach((player) -> {
			try {
				String lang = player.getSystemLanguage();
				player.sendTextMessage(c.warning + pluginName + ":> " + c.text
						+ t.get(i18nIndex, lang).replace("PH_PLAYERS", playerCount + ""));
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	@Override
	public void onFileChangeEvent(Path file) {
		if (file.toString().endsWith("jar")) {
			if (restartOnUpdate) {
				Server server = getServer();

				if (server.getPlayerCount() > 0) {
					flagRestart = true;
					this.broadcastMessage("BC_UPDATE_FLAG", server.getPlayerCount());
				} else {
					log.out("onFileCreateEvent: <" + file + "> changed, restarting now (no players online)", 100);
				}

			} else {
				log.out("onFileCreateEvent: <" + file + "> changed but restartOnUpdate is false", 0);
			}
		} else {
			log.out("onFileCreateEvent: <" + file + ">", 0);
		}
	}

	@Override
	public void onFileCreateEvent(Path file) {
		if (file.toString().endsWith("settings.properties")) {
			this.initSettings();
		} else {
			log.out(file.toString() + " was changed", 0);
		}
	}
}