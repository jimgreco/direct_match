package com.core.match.services.security;


import com.core.match.itch.msgs.ITCHConstants;
import com.core.match.msgs.MatchConstants;

/**
 * Created by jgreco on 11/21/14.
 */
public enum SecurityType {
    NOTE("TreasuryNote", MatchConstants.SecurityType.TreasuryNote, "TNOTE", ITCHConstants.SecurityType.TreasuryNote, SecurityCategory.BOND),
    BOND("TreasuryBond",  MatchConstants.SecurityType.TreasuryBond, "TBOND", ITCHConstants.SecurityType.TreasuryBond, SecurityCategory.BOND),
    DISCRETE_SPREAD("DiscreteSpread",  MatchConstants.SecurityType.DiscreteSpread, "MLEG", ITCHConstants.SecurityType.DiscreteSpread, SecurityCategory.MULTI_LEG),
    DISCRETE_BUTTERFLY("DiscreteButterfly",  MatchConstants.SecurityType.DiscreteButterfly, "MLEG", ITCHConstants.SecurityType.DiscreteButterfly, SecurityCategory.MULTI_LEG),
    ROLL("Roll",  MatchConstants.SecurityType.Roll, "MLEG", ITCHConstants.SecurityType.Roll, SecurityCategory.MULTI_LEG);

    private final char val;
    private final String fixName;
    private final SecurityCategory category;
    private final String name;
    private final char itchType;

    SecurityType(String name, char val, String fixName, char itchType, SecurityCategory category) {
        this.name = name;
        this.val = val;
        this.fixName = fixName;
        this.category = category;
        this.itchType = itchType;
    }

    public char getValue() {
        return val;
    }

    public String getName() {
        return name;
    }

    public String getFixName() {
        return fixName;
    }

    public SecurityCategory getCategory() {
        return category;
    }

    public char getITCHType() {
        return itchType;
    }

    public static SecurityType lookup(String name) {
        if (name.equals(NOTE.getName())) {
            return NOTE;
        }
        else if (name.equals(BOND.getName())) {
            return BOND;
        }
        else if (name.equals(DISCRETE_BUTTERFLY.getName())) {
            return DISCRETE_BUTTERFLY;
        }else if (name.equals(DISCRETE_SPREAD.getName())) {
            return DISCRETE_SPREAD;
        }else if (name.equals(ROLL.getName())) {
            return ROLL;
        }
        return null;
    }

    public static SecurityType lookup(char val) {
        if (val == NOTE.getValue()) {
            return NOTE;
        }
        else if (val == BOND.getValue()) {
            return BOND;
        }
        else if (val == DISCRETE_SPREAD.getValue()) {
            return DISCRETE_SPREAD;
        }
        else if (val == DISCRETE_BUTTERFLY.getValue()) {
            return DISCRETE_BUTTERFLY;
        }
        else if (val == ROLL.getValue()) {
            return ROLL;
        }
        return null;
    }

    @Override
	public String toString() {
        return getName();
    }
}
