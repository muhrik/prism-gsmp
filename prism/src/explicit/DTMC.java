//==============================================================================
//	
//	Copyright (c) 2002-
//	Authors:
//	* Dave Parker <david.parker@comlab.ox.ac.uk> (University of Oxford)
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

import java.util.*;
import java.util.Map.Entry;
import java.util.PrimitiveIterator.OfInt;

import common.IterableStateSet;
import prism.Pair;
import explicit.rewards.MCRewards;

/**
 * Interface for classes that provide (read) access to an explicit-state DTMC.
 */
public interface DTMC extends Model
{
	/**
	 * Get the number of transitions from state s.
	 */
	public int getNumTransitions(int s);

	/**
	 * Get an iterator over the transitions from state s.
	 */
	public Iterator<Entry<Integer, Double>> getTransitionsIterator(int s);

	/**
	 * Get an iterator over the transitions from state s, with their attached actions if present.
	 */
	public Iterator<Entry<Integer, Pair<Double, Object>>> getTransitionsAndActionsIterator(int s);

	/**
	 * Perform a single step of precomputation algorithm Prob0 for a single state,
	 * i.e., for the state {@code s} returns true iff there is a transition from
	 * {@code s} to a state in {@code u}.
	 * <br>
	 * <i>Default implementation</i>: Iterates using {@code getSuccessors()} and performs the check.
	 * @param s The state in question
	 * @param u Set of states {@code u}
	 * @return true iff there is a transition from s to a state in u
	 */
	public default boolean prob0step(int s, BitSet u)
	{
		for (SuccessorsIterator succ = getSuccessors(s); succ.hasNext(); ) {
			int t = succ.nextInt();
			if (u.get(t))
				return true;
		}
		return false;
	}

	/**
	 * Perform a single step of precomputation algorithm Prob0, i.e., for states i in {@code subset},
	 * set bit i of {@code result} iff there is a transition to a state in {@code u}.
	 * <br>
	 * <i>Default implementation</i>: Iterate over {@code subset} and use {@code prob0step(s,u)}
	 * to determine result for {@code s}.
	 * @param subset Only compute for these states
	 * @param u Set of states {@code u}
	 * @param result Store results here
	 */
	public default void prob0step(BitSet subset, BitSet u, BitSet result)
	{
		for (OfInt it = new IterableStateSet(subset, getNumStates()).iterator(); it.hasNext();) {
			int s = it.nextInt();
			result.set(s, prob0step(s,u));
		}
	}

	/**
	 * Perform a single step of precomputation algorithm Prob1 for a single state,
	 * i.e., for states s return true iff there is a transition to a state in
	 * {@code v} and all transitions go to states in {@code u}.
	 * @param s The state in question
	 * @param u Set of states {@code u}
	 * @param v Set of states {@code v}
	 * @return true iff there is a transition from s to a state in v and all transitions go to u.
	 */
	public default boolean prob1step(int s, BitSet u, BitSet v)
	{
		boolean allTransitionsToU = true;
		boolean hasTransitionToV = false;
		for (SuccessorsIterator succ = getSuccessors(s); succ.hasNext(); ) {
			int t = succ.nextInt();
			if (!u.get(t)) {
				allTransitionsToU = false;
				// early abort, as overall result is false
				break;
			}
			hasTransitionToV = hasTransitionToV || v.get(t);
		}
		return (allTransitionsToU && hasTransitionToV);
	}

	/**
	 * Perform a single step of precomputation algorithm Prob1, i.e., for states i in {@code subset},
	 * set bit i of {@code result} iff there is a transition to a state in {@code v} and all transitions go to states in {@code u}.
	 * @param subset Only compute for these states
	 * @param u Set of states {@code u}
	 * @param v Set of states {@code v}
	 * @param result Store results here
	 */
	public default void prob1step(BitSet subset, BitSet u, BitSet v, BitSet result)
	{
		for (OfInt it = new IterableStateSet(subset, getNumStates()).iterator(); it.hasNext();) {
			int s = it.nextInt();
			result.set(s, prob1step(s,u,v));
		}
	}

	/**
	 * Do a matrix-vector multiplication for
	 * the DTMC's transition probability matrix P and the vector {@code vect} passed in.
	 * i.e. for all s: result[s] = sum_j P(s,j)*vect[j]
	 * @param vect Vector to multiply by
	 * @param result Vector to store result in
	 * @param subset Only do multiplication for these rows (ignored if null)
	 * @param complement If true, {@code subset} is taken to be its complement (ignored if {@code subset} is null)
	 */
	public default void mvMult(double vect[], double result[], BitSet subset, boolean complement)
	{
		for (int s : new IterableStateSet(subset, getNumStates(), complement)) {
			result[s] = mvMultSingle(s, vect);
		}
	}

	/**
	 * Do a single row of matrix-vector multiplication for
	 * the DTMC's transition probability matrix P and the vector {@code vect} passed in.
	 * i.e. return sum_j P(s,j)*vect[j]
	 * @param s Row index
	 * @param vect Vector to multiply by
	 */
	public double mvMultSingle(int s, double vect[]);

	/**
	 * Do a Gauss-Seidel-style matrix-vector multiplication for
	 * the DTMC's transition probability matrix P and the vector {@code vect} passed in,
	 * storing new values directly in {@code vect} as computed.
	 * i.e. for all s: vect[s] = (sum_{j!=s} P(s,j)*vect[j]) / (1-P(s,s))
	 * The maximum (absolute/relative) difference between old/new
	 * elements of {@code vect} is also returned.
	 * @param vect Vector to multiply by (and store the result in)
	 * @param subset Only do multiplication for these rows (ignored if null)
	 * @param complement If true, {@code subset} is taken to be its complement (ignored if {@code subset} is null)
	 * @param absolute If true, compute absolute, rather than relative, difference
	 * @return The maximum difference between old/new elements of {@code vect}
	 */
	public default double mvMultGS(double vect[], BitSet subset, boolean complement, boolean absolute)
	{
		double d, diff, maxDiff = 0.0;
		for (int s : new IterableStateSet(subset, getNumStates(), complement)) {
			d = mvMultJacSingle(s, vect);
			diff = absolute ? (Math.abs(d - vect[s])) : (Math.abs(d - vect[s]) / d);
			maxDiff = diff > maxDiff ? diff : maxDiff;
			vect[s] = d;
		}
		// Use this code instead for backwards Gauss-Seidel
		/*for (s = numStates - 1; s >= 0; s--) {
			if (subset.get(s)) {
				d = mvMultJacSingle(s, vect);
				diff = absolute ? (Math.abs(d - vect[s])) : (Math.abs(d - vect[s]) / d);
				maxDiff = diff > maxDiff ? diff : maxDiff;
				vect[s] = d;
			}
		}*/
		return maxDiff;
	}

	/**
	 * Do a single row of Jacobi-style matrix-vector multiplication for
	 * the DTMC's transition probability matrix P and the vector {@code vect} passed in.
	 * i.e. return (sum_{j!=s} P(s,j)*vect[j]) / (1-P(s,s))
	 * @param s Row index
	 * @param vect Vector to multiply by
	 */
	public double mvMultJacSingle(int s, double vect[]);

	/**
	 * Do a matrix-vector multiplication and sum of action reward.
	 * @param vect Vector to multiply by
	 * @param mcRewards The rewards
	 * @param result Vector to store result in
	 * @param subset Only do multiplication for these rows (ignored if null)
	 * @param complement If true, {@code subset} is taken to be its complement (ignored if {@code subset} is null)
	 */
	public default void mvMultRew(double vect[], MCRewards mcRewards, double result[], BitSet subset, boolean complement)
	{
		for (int s : new IterableStateSet(subset, getNumStates(), complement)) {
			result[s] = mvMultRewSingle(s, vect, mcRewards);
		}
	}

	/**
	 * Do a single row of matrix-vector multiplication and sum of action reward.
	 * @param s Row index
	 * @param vect Vector to multiply by
	 * @param mcRewards The rewards
	 */
	public double mvMultRewSingle(int s, double vect[], MCRewards mcRewards);

	/**
	 * Do a vector-matrix multiplication for
	 * the DTMC's transition probability matrix P and the vector {@code vect} passed in.
	 * i.e. for all s: result[s] = sum_i P(i,s)*vect[i]
	 * @param vect Vector to multiply by
	 * @param result Vector to store result in
	 */
	public void vmMult(double vect[], double result[]);
}
