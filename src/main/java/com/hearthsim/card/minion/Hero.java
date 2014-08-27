package com.hearthsim.card.minion;

import com.hearthsim.card.Deck;
import com.hearthsim.exception.HSException;
import com.hearthsim.exception.HSInvalidPlayerIndexException;
import com.hearthsim.util.DeepCopyable;
import com.hearthsim.util.boardstate.BoardState;
import com.hearthsim.util.boardstate.BoardStateFactoryBase;
import com.hearthsim.util.tree.HearthTreeNode;
import org.json.JSONObject;

public class Hero extends Minion {

	protected static final byte HERO_ABILITY_COST = 2;  //Assumed to be 2 for all heroes
	
	protected byte weaponCharge_;
	protected byte armor_;
		
	public Hero() {
		this("NoHero", (byte)30);
	}

	public Hero(String name, byte health) {
		this(name, (byte)0, (byte)0, health, (byte)0, (byte)0, false, false, false, false, false);
	}
	
	public Hero(
			String name,
			byte attack,
			byte extraAttackUntilTurnEnd,
			byte health,
			byte armor,
			byte weaponCharge,
			boolean windFury,
			boolean hasAttacked,
			boolean hasWindFuryAttacked,
			boolean frozen,
			boolean hasBeenUsed) {
	
		super(name, (byte)0, attack, health, (byte)0, extraAttackUntilTurnEnd, (byte)0, (byte)30, (byte)30, (byte)0, (byte)0, false, false, windFury, false, hasAttacked, hasWindFuryAttacked, frozen, false, false, false, false, false, false, false, null, null, false, hasBeenUsed);
		armor_ = armor;
		weaponCharge_ = weaponCharge;
	}


	public byte getWeaponCharge() {
		return weaponCharge_;
	}
	
	public void setWeaponCharge(byte weaponCharge) {
		weaponCharge_ = weaponCharge;
	}
	
	public byte getArmor() {
		return armor_;
	}
	
	public void setArmor(byte armor) {
		armor_ = armor;
	}
	
	@Override
	public DeepCopyable deepCopy() {
		return new Hero(
				this.name_, 
				this.attack_,
				this.extraAttackUntilTurnEnd_,
				this.health_,
				this.armor_,
				this.weaponCharge_,
				this.windFury_,
				this.hasAttacked_,
				this.hasWindFuryAttacked_,
				this.frozen_,
				this.hasBeenUsed_
				);
	}
	
	
	/**
	 * 
	 * Attack with the hero
	 * 
	 * A hero can only attack if it has a temporary buff, such as weapons
	 * 
	 * @param targetMinionPlayerIndex The index of the target player.  0 if targeting yourself or your own minions, 1 if targeting the enemy
	 * @param targetMinion The target minion
	 * @param minionIndex The index of the target minion.
	 * @param boardState The BoardState before this card has performed its action.  It will be manipulated and returned.
	 * @param deckPlayer0 The deck of player0
	 * @param deckPlayer0 The deck of player1
	 * 
	 * @return The boardState is manipulated and returned
	 */
	@Override
	public HearthTreeNode attack(
			int targetMinionPlayerIndex,
			Minion targetMinion,
			HearthTreeNode boardState,
			Deck deckPlayer0,
			Deck deckPlayer1)
		throws HSException
	{
		
		if (attack_ + extraAttackUntilTurnEnd_ == 0) {
			return null;
		}

		if (this.weaponCharge_ == 0 && this.extraAttackUntilTurnEnd_ == 0) {
			return null;
		}
		
		//this is somewhat redundant, but it must be done here...
		if (frozen_) {
			this.hasAttacked_ = true;
			this.frozen_ = false;
			return boardState;
		}
		
		
		HearthTreeNode toRet = super.attack(targetMinionPlayerIndex, targetMinion, boardState, deckPlayer0, deckPlayer1);
		
		if (toRet != null) {
			if (this.weaponCharge_ > 0) {
				this.weaponCharge_ -= 1;
				if (this.weaponCharge_ == 0) {
					this.attack_ = 0;
				}
			}
		}
		return toRet;
	}
	
	@Override
    public boolean canBeUsedOn(int playerIndex, Minion minioin) {
		if (hasBeenUsed_) 
			return false;
		return true;
    }

	public final HearthTreeNode useHeroAbility(
			int targetPlayerIndex,
			Minion targetMinion,
			HearthTreeNode boardState,
			Deck deckPlayer0,
			Deck deckPlayer1)
		throws HSException
	{
		return this.useHeroAbility(targetPlayerIndex, targetMinion, boardState, deckPlayer0, deckPlayer1, false);
	}
	/**
	 * Use the hero ability on a given target
	 * 
	 * @param targetPlayerIndex The player index of the target minion
	 * @param targetMinion The target minion
	 * @param boardState
	 * @param deckPlayer0 The deck of player0
	 * @param deckPlayer0 The deck of player1
	 * 
	 * @return
	 */
	public final HearthTreeNode useHeroAbility(
			int targetPlayerIndex,
			Minion targetMinion,
			HearthTreeNode boardState,
			Deck deckPlayer0,
			Deck deckPlayer1,
			boolean singleRealizationOnly)
		throws HSException
	{
		if (boardState.data_.getMana_p0() < HERO_ABILITY_COST)
			return null;
		
		HearthTreeNode toRet = this.useHeroAbility_core(targetPlayerIndex, targetMinion, boardState, deckPlayer0, deckPlayer1, singleRealizationOnly);
		if (toRet != null) {
			toRet = BoardStateFactoryBase.handleDeadMinions(toRet, deckPlayer0, deckPlayer1);
		}
		return toRet;
	}
	
	public HearthTreeNode useHeroAbility_core(
			int targetPlayerIndex,
			Minion targetMinion,
			HearthTreeNode boardState,
			Deck deckPlayer0,
			Deck deckPlayer1,
			boolean singleRealizationOnly)
		throws HSException
	{
		return null;
	}
	
	/**
	 * Called when this minion takes damage
	 * 
	 * Overridden from Minion.  Need to handle armor.
	 * 
	 * @param damage The amount of damage to take
	 * @param attackerPlayerIndex The player index of the attacker.  This is needed to do things like +spell damage.
	 * @param thisPlayerIndex The player index of this minion
	 * @param boardState 
	 * @param deckPlayer0 The deck of player0
	 * @param deckPlayer0 The deck of player1
	 * @param isSpellDamage 
	 * 
	 * @throws HSInvalidPlayerIndexException
	 */
	@Override
	public HearthTreeNode takeDamage(
			byte damage,
			int attackerPlayerIndex,
			int thisPlayerIndex,
			HearthTreeNode boardState,
			Deck deckPlayer0, 
			Deck deckPlayer1,
			boolean isSpellDamage,
			boolean handleMinionDeath)
		throws HSException
	{
		HearthTreeNode toRet = boardState;
		byte damageRemaining = (byte)(damage - armor_);
		if (damageRemaining > 0) {
			armor_ = 0;
			toRet = super.takeDamage(damageRemaining, attackerPlayerIndex, thisPlayerIndex, toRet, deckPlayer0, deckPlayer1, isSpellDamage, handleMinionDeath);
		} else {
			armor_ = (byte)(armor_ - damage);
		}
		return toRet;
	}
	
	
	/**
	 * End the turn and resets the card state
	 * 
	 * This function is called at the end of the turn.  Any derived class must override it and remove any 
	 * temporary buffs that it has.
	 */
	@Override
	public BoardState endTurn(int thisMinionPlayerIndex, BoardState boardState, Deck deckPlayer0, Deck deckPlayer1) throws HSInvalidPlayerIndexException {
		this.extraAttackUntilTurnEnd_ = 0;
		return boardState;
	}

	public JSONObject toJSON() {
		JSONObject json = super.toJSON();
		json.put("armor", this.armor_);
		json.put("weaponCharge", this.weaponCharge_);
		return json;
	}
}
