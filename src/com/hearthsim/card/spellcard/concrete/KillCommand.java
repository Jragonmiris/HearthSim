package com.hearthsim.card.spellcard.concrete;

import com.hearthsim.card.Deck;
import com.hearthsim.card.minion.Beast;
import com.hearthsim.card.minion.Minion;
import com.hearthsim.card.spellcard.SpellDamage;
import com.hearthsim.exception.HSInvalidPlayerIndexException;
import com.hearthsim.util.BoardState;
import com.hearthsim.util.HearthTreeNode;

public class KillCommand extends SpellDamage {

	public KillCommand() {
		this(false);
	}

	public KillCommand(boolean hasBeenUsed) {
		super("Kill Command", (byte)3, (byte)3, hasBeenUsed);
	}

	/**
	 * 
	 * Use the card on the given target
	 * 
	 * Deals 3 damage.  If you have a beast, deals 5 damage.
	 * 
	 * @param thisCardIndex The index (position) of the card in the hand
	 * @param playerIndex The index of the target player.  0 if targeting yourself or your own minions, 1 if targeting the enemy
	 * @param minionIndex The index of the target minion.
	 * @param boardState The BoardState before this card has performed its action.  It will be manipulated and returned.
	 * 
	 * @return The boardState is manipulated and returned
	 */
	@Override
	protected HearthTreeNode<BoardState> use_core(
			int thisCardIndex,
			int playerIndex,
			int minionIndex,
			HearthTreeNode<BoardState> boardState,
			Deck deck)
		throws HSInvalidPlayerIndexException
	{		
		boolean haveBeast = false;
		for (final Minion minion : boardState.data_.getMinions_p0()) {
			haveBeast = haveBeast || (minion instanceof Beast);
		}
		if (haveBeast)
			this.damage_ = (byte)5;
		else
			this.damage_ = (byte)3;
		HearthTreeNode<BoardState> toRet = super.use_core(thisCardIndex, playerIndex, minionIndex, boardState, deck);

		return toRet;
	}
}