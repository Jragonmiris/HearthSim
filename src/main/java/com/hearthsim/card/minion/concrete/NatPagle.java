package com.hearthsim.card.minion.concrete;

import com.hearthsim.card.minion.Minion;
import com.hearthsim.exception.HSException;
import com.hearthsim.model.PlayerSide;
import com.hearthsim.util.tree.HearthTreeNode;

public class NatPagle extends Minion {

    public NatPagle() {
        super();
    }

    @Override
    public HearthTreeNode startTurn(PlayerSide thisMinionPlayerIndex, HearthTreeNode boardModel) throws HSException {
        HearthTreeNode toRet = boardModel;
        if (thisMinionPlayerIndex == PlayerSide.CURRENT_PLAYER && Math.random() > 0.5) {
            toRet.data_.drawCardFromCurrentPlayerDeck(1);
        }
        return super.startTurn(thisMinionPlayerIndex, toRet);
    }

}
