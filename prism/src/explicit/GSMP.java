//==============================================================================
//	
//	Copyright (c) 2017-
//	Authors:
//	* Mario Uhrik <433501@mail.muni.cz> (Masaryk University)
//	
//------------------------------------------------------------------------------
//	
//	This file is part of PRISM.
//	
//	PRISM is free software; you can redistribute it and/or modify
//	it under the terms of the GNU General Public License as published by
//	the Free Software Foundation; either version 2 of the License, or
//	(at your option) any later version.
//	
//	PRISM is distributed in the hope that it will be useful,
//	but WITHOUT ANY WARRANTY; without even the implied warranty of
//	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//	GNU General Public License for more details.
//	
//	You should have received a copy of the GNU General Public License
//	along with PRISM; if not, write to the Free Software Foundation,
//	Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//	
//==============================================================================

package explicit;

import java.util.List;

/**
 * Interface for classes that represent an explicit-state GSMP.
 * 
 * GSMP is a model driven by events with general time distributions.
 * GSMP may have any number of events, and any number of events can be active at any given time.
 * Out of the active states, only one "wins" by occurring the soonest.
 * Each event has a distribution on states for each state, determining the next state.
 */
public interface GSMP extends ModelSimple
{
	/**
	 * Get all events.
	 */
	public List<GSMPEvent> getEventList();
	
	/**
	 * Get GSMPEvent of name {@code eventName}. Null if not present.
	 */
	public GSMPEvent getEvent(String eventName);

	/**
	 * Returns a list of events active in state {@code state}.
	 */
	public List<GSMPEvent> getActiveEvents(int state);
	
	/**
	 * Adds a new empty event into the GSMP iff this event has not yet been added.
	 * @param event to add
	 * @return true if the event was successfully added,
	 *         false if the event is already in and nothing happened
	 */
	public boolean addEvent(GSMPEvent event);
	
	/**
	 * Constructs a CTMC from the exponentially distributed events within this GSMP.
	 * This is useful for CTMC-based model checking methods, e.g. ACTMC construction.
	 */
	public CTMC generateCTMC();
}
