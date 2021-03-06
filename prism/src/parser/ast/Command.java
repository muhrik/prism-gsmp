//==============================================================================
//	
//	Copyright (c) 2002-
//	Authors:
//	* Dave Parker <david.parker@comlab.ox.ac.uk> (University of Oxford, formerly University of Birmingham)
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

package parser.ast;

import parser.visitor.*;
import prism.PrismLangException;

public class Command extends ASTElement
{
	// Action label
	private String synch;
	// Index of action label in model's list of all actions ("synchs")
	// This is 1-indexed, with 0 denoting an independent ("tau"-labelled) command.
	// -1 denotes not (yet) known.
	private int synchIndex;
	// Guard
	private Expression guard;
	/** Assigned GSMP Event. Can be null. */
	private ExpressionIdent eventIdent;
	 /** 
	 * Used for GSMP models. 
	 * If true, this command is a slave to other commands with the same action label.
	 * As a slave, this command does not have its own time distribution and relies on other
	 * commands (i.e. masters) to provide it during synchronization.
	 * <br>
	 * Example use case: 
	 * <br>
	 * There are 2 synchronized modules M1 and M2, each containing a command with action label [a].
	 * Let the command in M1 be a "slave", and the command in M2 be a "master".
	 * The joint time distribution for these synchronized commands is chosen by the master.
	 */
	private boolean isSlave;
	// List of updates
	private Updates updates;
	// Parent module
	private Module parent;
	
	// Constructor
	
	public Command()
	{
		synch = "";
		synchIndex = -1;
		guard = null;
		updates = null;
		isSlave = false;
		eventIdent = null;
	}
	
	/** Deepcopy constructor */
	public Command(Command comm)
	{
		Command tmp = (Command) comm.deepCopy();
		this.synch = tmp.getSynch();
		this.synchIndex = tmp.getSynchIndex();
		this.guard = tmp.getGuard();
		this.updates = tmp.getUpdates();
		this.isSlave = tmp.isSlave();
		this.eventIdent = tmp.getEventIdent();
		this.parent = tmp.getParent();
	}
	
	// Set methods
	
	public void setSynch(String s)
	{
		synch = s;
	}
	
	public void setSynchIndex(int i)
	{
		synchIndex = i;
	}
	
	public void setGuard(Expression g)
	{
		guard = g;
	}
	
	public void setUpdates(Updates u)
	{
		updates = u;
		u.setParent(this);
	}
	
	public void setParent(Module m)
	{
		parent = m;
	}
	
	public void setSlave(boolean s)
	{
		isSlave = s;
	}
	
	public void setEventIdent(ExpressionIdent e)
	{
		eventIdent = e;
	}

	// Get methods

	/**
	 * Get the action label for this command. For independent ("tau"-labelled) commands,
	 * this is the empty string "" (it should never be null).
	 */
	public String getSynch()
	{
		return synch;
	}
	
	/**
	 * Get the index of the action label for this command (in the model's list of actions).
	 * This is 1-indexed, with 0 denoting an independent ("tau"-labelled) command.
	// -1 denotes not (yet) known.
	 */
	public int getSynchIndex()
	{
		return synchIndex;
	}
	
	public Expression getGuard()
	{
		return guard;
	}
	
	public Updates getUpdates()
	{
		return updates;
	}
	
	public Module getParent()
	{
		return parent;
	}
	
	public boolean isSlave()
	{
		return isSlave;
	}
	
	public ExpressionIdent getEventIdent()
	{
		return eventIdent;
	}
	
	/**
	 * @return True iff this is a GSMP command (has an assigned event or is a slave)
	 */
	public boolean isGSMPCommand() {
		return (isSlave() || getEventIdent() != null);
	}
	
	// Methods required for ASTElement:
	
	/**
	 * Visitor method.
	 */
	public Object accept(ASTVisitor v) throws PrismLangException
	{
		return v.visit(this);
	}
	
	/**
	 * Convert to string.
	 */
	public String toString()
	{
		String s = "[" + synch;
		s += "] " + guard;
		if (isSlave || eventIdent != null) {
			s += " --";
		}
		if (isSlave) {
			s += "void(slave)";
		} else if (eventIdent != null) {
			s += eventIdent.getName();
		}
		s += "-> " + updates;
		return s;
	}
	
	/**
	 * Perform a deep copy.
	 */
	public ASTElement deepCopy()
	{
		Command ret = new Command();
		ret.setSynch(getSynch());
		ret.setSynchIndex(getSynchIndex());
		ret.setGuard(getGuard().deepCopy());
		ret.setUpdates((Updates)getUpdates().deepCopy());
		
		ExpressionIdent eventIdent = (getEventIdent() == null) ? null : (ExpressionIdent)getEventIdent().deepCopy();
		ret.setEventIdent(eventIdent);
		ret.setParent(this.getParent());
		ret.setSlave(isSlave());
		ret.setPosition(this);
		return ret;
	}
}

//------------------------------------------------------------------------------
