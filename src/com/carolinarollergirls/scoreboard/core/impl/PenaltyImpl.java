package com.carolinarollergirls.scoreboard.core.impl;

import com.carolinarollergirls.scoreboard.core.BoxTrip;
import com.carolinarollergirls.scoreboard.core.Jam;
import com.carolinarollergirls.scoreboard.core.Penalty;
import com.carolinarollergirls.scoreboard.core.Skater;
import com.carolinarollergirls.scoreboard.event.Command;
import com.carolinarollergirls.scoreboard.event.NumberedScoreBoardEventProviderImpl;
import com.carolinarollergirls.scoreboard.event.Value;
import com.carolinarollergirls.scoreboard.rules.Rule;
import com.carolinarollergirls.scoreboard.utils.ScoreBoardClock;

public class PenaltyImpl extends NumberedScoreBoardEventProviderImpl<Penalty> implements Penalty {
    public PenaltyImpl(Skater s, int n) {
        super(s, n, Skater.PENALTY);
        addProperties(TIME, JAM, PERIOD_NUMBER, JAM_NUMBER, CODE, SERVING, SERVED, FORCE_SERVED, BOX_TRIP, REMOVE);
        set(TIME, ScoreBoardClock.getInstance().getCurrentWalltime());
        setInverseReference(JAM, Jam.PENALTY);
        setInverseReference(BOX_TRIP, BoxTrip.PENALTY);
        addWriteProtectionOverride(TIME, Source.ANY_FILE);
        setRecalculated(SERVED).addSource(this, BOX_TRIP).addSource(this, FORCE_SERVED);
        setCopy(SERVING, this, BOX_TRIP, BoxTrip.IS_CURRENT, true);
        setCopy(JAM_NUMBER, this, JAM, Jam.NUMBER, true);
        setCopy(PERIOD_NUMBER, this, JAM, Jam.PERIOD_NUMBER, true);
        if (s.isPenaltyBox()) { set(BOX_TRIP, s.getCurrentFielding().getCurrentBoxTrip()); }
        set(SERVED, get(BOX_TRIP) != null);
    }

    @Override
    public int compareTo(Penalty other) {
        if (other == null) { return -1; }
        if (getJam() == other.getJam()) {
            return (int) (get(Penalty.TIME) - other.get(Penalty.TIME));
        }
        if (getJam() == null) { return 1; }
        return getJam().compareTo(other.getJam());
    }

    @Override
    protected Object computeValue(Value<?> prop, Object value, Object last, Source source, Flag flag) {
        if (prop == NEXT && getNumber() == 0) { return null; }
        if (prop == PREVIOUS && value != null && ((Penalty) value).getNumber() == 0) { return null; }
        if (prop == SERVED) { return (get(BOX_TRIP) != null || get(FORCE_SERVED)); }
        return value;
    }
    @Override
    protected void valueChanged(Value<?> prop, Object value, Object last, Source source, Flag flag) {
        if (prop == JAM && !Skater.FO_EXP_ID.equals(getProviderId())) {
            int newPos = getNumber();
            if (value == null || ((Jam) value).compareTo((Jam) last) > 0) {
                Penalty comp = getNext();
                while (compareTo(comp) > 0) { // will be false if comp == null
                    newPos = comp.getNumber();
                    comp = comp.getNext();
                }
            } else {
                Penalty comp = getPrevious();
                while (comp != null && compareTo(comp) < 0) {
                    newPos = comp.getNumber();
                    comp = comp.getPrevious();
                }
            }
            moveToNumber(newPos);

            if (newPos == scoreBoard.getRulesets().getInt(Rule.FO_LIMIT)) {
                Penalty fo = parent.get(Skater.PENALTY, Skater.FO_EXP_ID);
                if (fo != null && fo.get(CODE) == "FO") {
                    fo.set(JAM, (Jam) value);
                }
            }
        }
        if (prop == CODE && value == null) {
            delete(source);
        }
    }

    @Override
    public void execute(Command prop, Source source) {
        if (prop == REMOVE) { delete(source); }
    }

    @Override
    public int getPeriodNumber() { return get(PERIOD_NUMBER); }
    @Override
    public int getJamNumber() { return get(JAM_NUMBER); }
    @Override
    public Jam getJam() { return get(JAM); }
    @Override
    public String getCode() { return get(CODE); }
    @Override
    public boolean isServed() { return get(SERVED); }
}
