package com.hearthsim.card.spellcard.concrete;

import com.hearthsim.card.minion.Minion;
import com.hearthsim.card.minion.Minion.MinionTribe;
import com.hearthsim.card.spellcard.SpellDamage;
import com.hearthsim.event.EffectMinionAction;
import com.hearthsim.event.EffectMinionSpellDamage;
import com.hearthsim.event.MinionFilterTargetedSpell;
import com.hearthsim.exception.HSException;
import com.hearthsim.model.PlayerSide;
import com.hearthsim.util.tree.HearthTreeNode;

public class Demonheart extends SpellDamage {

    /**
     * Constructor
     *
     * Defaults to hasBeenUsed = false
     */
    public Demonheart() {
        super();

        this.minionFilter = MinionFilterTargetedSpell.ALL_MINIONS;
    }

    @Override
    protected EffectMinionSpellDamage<SpellDamage> getEffect() {
        if (this.effect == null) {
            this.effect = new EffectMinionSpellDamage<SpellDamage>(damage_) {
                @Override
                public HearthTreeNode applyEffect(PlayerSide originSide, SpellDamage origin, PlayerSide targetSide, int targetCharacterIndex, HearthTreeNode boardState) throws HSException {
                    HearthTreeNode toRet = boardState;
                    Minion targetCharacter = boardState.data_.getCharacter(targetSide, targetCharacterIndex);
                    if (isCurrentPlayer(targetSide) && targetCharacter.getTribe() == MinionTribe.DEMON) {
                        targetCharacter.setAttack((byte) (targetCharacter.getAttack() + 5));
                        targetCharacter.setMaxHealth((byte) (targetCharacter.getMaxHealth() + 5));
                        targetCharacter.setHealth((byte)(targetCharacter.getHealth() + 5));
                    } else {
                        toRet = super.applyEffect(originSide, origin, targetSide, targetCharacterIndex, boardState);
                    }
                    return toRet;
                }
            };
        }
        return this.effect;
    }
}
