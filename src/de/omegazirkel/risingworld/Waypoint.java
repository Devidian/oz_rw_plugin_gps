/****************************
	G P S  -  A Java plug-in for Rising World.

	Waypoint.java - A waypoint

	Originally created by : Maurizio M. Gavioli 2016-08-15

	(C) Maurizio M. Gavioli (a.k.a. Miwarre), 2016
	Licensed under the Creative Commons by-sa 3.0 license (see http://creativecommons.org/licenses/by-sa/3.0/ for details)

*****************************/

package de.omegazirkel.risingworld;

import net.risingworld.api.utils.Vector3f;

public class Waypoint
{

	protected	int			id;
	protected	String		name;
	protected	Vector3f	pos;

	// C'TOR

	public Waypoint(int id, String name, float x, float y, float z)
	{
		this.id		= id;
		this.name	= name;
		pos			= new Vector3f(x, y, z);
	}

	// toString()
	//
	// Returns a textual representation of the waypoint suitable for a list
	@Override
	public String toString()
	{
		if (name == null || name.length() < 1)		// undefined
			return null;
		float		n		= pos.z;
		String		latDir	= "N";
		float		e		= pos.x;
		String		longDir	= "W";
		if (n < 0.0f)
		{
			n		= -n;
			latDir	= "S";
		}
		if (e < 0.0f)
		{
			e		= -e;
			longDir	= "E";
		}
		return String.format("%d: %s (%.1f%s,%.1f%s) h%.1f", id, name, n, latDir, e, longDir, pos.y);
	}

	// toString(double, Vector3f)
	//
	// Returns a textual representation of the waypoint suitable for the GPS GUI output
	public String toString(double playerHdg, Vector3f playerPos)
	{
		double	deltaN		= pos.z - playerPos.z;
		double	deltaW		= pos.x - playerPos.x;
		double	dist		= Math.sqrt(deltaN * deltaN + deltaW * deltaW);	// distance in blocks
		String	shortName	= name.length() > GPS.wpDispLen ?  name.substring(0, GPS.wpDispLen) : name;
		if (dist < 4)			// if distance less than 2 m, data are unreliable, only output wp name
			return  " | ---°  " + shortName + "   <2m";
		double	radial;
		radial				= Math.acos(deltaN / dist) * GPS.RAD2DEG;
		if (deltaW > 0)
			radial 			= 360 - radial;		// for this adjustment,  see setGpsText() in GPS.java
		int 	rdl			= (int)/*Math.floor*/(radial + 0.5);
		if (rdl == 0)
			rdl = 360;

		// text build up
		double	wpHdgDelta	= playerHdg - rdl;
		String	text		= String.format("%03d°", rdl);	// separator and radial
		if ( (wpHdgDelta > GPS.wpHdgPrecis && wpHdgDelta < (180-GPS.wpHdgPrecis))	// left arrow
				|| (wpHdgDelta > (GPS.wpHdgPrecis-360) && wpHdgDelta < (-GPS.wpHdgPrecis-180)) )
			text += " <";
		else
			text += "  ";

		text  += shortName;

		if ( (wpHdgDelta < -GPS.wpHdgPrecis && wpHdgDelta > (GPS.wpHdgPrecis-180))	// right arrow
				|| (wpHdgDelta < (360-GPS.wpHdgPrecis) && wpHdgDelta > (GPS.wpHdgPrecis+180)) )
			text += "> ";
		else
			text += "  ";

		text += Math.floor(dist / 2 + 0.5) + "m";			// distance in m
		return text;
	}
}
