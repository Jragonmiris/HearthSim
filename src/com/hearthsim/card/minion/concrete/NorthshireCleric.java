package com.hearthsim.card.minion.concrete;

import com.hearthsim.card.Card;
import com.hearthsim.card.Deck;
import com.hearthsim.card.minion.Minion;
import com.hearthsim.exception.HSInvalidPlayerIndexException;
import com.hearthsim.util.BoardState;
import com.hearthsim.util.HearthTreeNode;

/**
 * Northshire Cleric
 * 
 * @author oyachai
 *
 * This minion is a 1 mana, 1 attack, 3 health minion.
 * Whenever a minion is healed, this minion draws a card for its player.
 *
 */
public class NorthshireCleric extends Minion {

	private static final String NAME = "Northshire Cleric";
	private static final byte MANA_COST = 1;
	private static final byte ATTACK = 1;
	private static final byte HEALTH = 3;
	
	private static final boolean TAUNT = false;
	private static final boolean DIVINE_SHIELD = false;
	private static final boolean WINDFURY = false;
	private static final boolean CHARGE = false;
	
	private static final boolean SUMMONED = false;
	private static final boolean TRANSFORMED = false;
	
	public NorthshireCleric() {
		this(
				MANA_COST,
				ATTACK,
				HEALTH,
				ATTACK,
				(byte)0,
				HEALTH,
				HEALTH,
				TAUNT,
				DIVINE_SHIELD,
				WINDFURY,
				CHARGE,
				false,
				false,
				false,
				SUMMONED,
				TRANSFORMED,
				false,
				false,
				true,
				false
			);
	}
	
	public NorthshireCleric(	
			byte mana,
			byte attack,
			byte health,
			byte baseAttack,
			byte extraAttackUntilTurnEnd,
			byte baseHealth,
			byte maxHealth,
			boolean taunt,
			boolean divineShield,
			boolean windFury,
			boolean charge,
			boolean hasAttacked,
			boolean hasWindFuryAttacked,
			boolean frozen,
			boolean summoned,
			boolean transformed,
			boolean destroyOnTurnStart,
			boolean destroyOnTurnEnd,
			boolean isInHand,
			boolean hasBeenUsed) {
		
		super(
			NAME,
			mana,
			attack,
			health,
			baseAttack,
			extraAttackUntilTurnEnd,
			baseHealth,
			maxHealth,
			taunt,
			divineShield,
			windFury,
			charge,
			hasAttacked,
			hasWindFuryAttacked,
			frozen,
			summoned,
			transformed,
			destroyOnTurnStart,
			destroyOnTurnEnd,
			isInHand,
			hasBeenUsed);
	}
	
	@Override
	public Object deepCopy() {
		return new NorthshireCleric(
				this.mana_,
				this.attack_,
				this.health_,
				this.baseAttack_,
				this.extraAttackUntilTurnEnd_,
				this.baseHealth_,
				this.maxHealth_,
				this.taunt_,
				this.divineShield_,
				this.windFury_,
				this.charge_,
				this.hasAttacked_,
				this.hasWindFuryAttacked_,
				this.frozen_,
				this.summoned_,
				this.transformed_,
				this.destroyOnTurnStart_,
				this.destroyOnTurnEnd_,
				this.isInHand_,
				this.hasBeenUsed_);
	}
	
	
	/**
	 * 
	 * Called whenever another character (including the hero) is healed
	 * 
	 * @param playerIndex The index of the healed minion's player.  0 if targeting yourself or your own minions, 1 if targeting the enemy
	 * @param minionIndex The index of the healed minion.
	 * @param boardState The BoardState before this card has performed its action.  It will be manipulated and returned.
	 * 
	 * @return The boardState is manipulated and returned
	 */
	public HearthTreeNode<BoardState> minionHealedEvent(
			int thisMinionPlayerIndex,
			int thisMinionIndex,
			int healedMinionPlayerIndex,
			int healedMinionIndex,
			HearthTreeNode<BoardState> boardState,
			Deck deck)
		throws HSInvalidPlayerIndexException
	{

		Card card = deck.drawCard(boardState.data_.getDeckPos(thisMinionPlayerIndex));
		if (card == null) {
			byte fatigueDamage = boardState.data_.getFatigueDamage(thisMinionPlayerIndex);
			boardState.data_.setFatigueDamage(thisMinionPlayerIndex, (byte)(fatigueDamage + 1));
			boardState.data_.getHero(thisMinionPlayerIndex).setHealth((byte)(boardState.data_.getHero(thisMinionPlayerIndex).getHealth() - fatigueDamage));
		} else {
			boardState.data_.placeCard_hand(thisMinionPlayerIndex, card);
			boardState.data_.setDeckPos(thisMinionPlayerIndex, boardState.data_.getDeckPos(thisMinionPlayerIndex) + 1);
		}
	
		
		return boardState;
	}
}
