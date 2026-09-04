package tech.hakuri.teto.utils;

public class MSTimer {

    public double init;

    public MSTimer() {
        this.reset();
    }

    public boolean hasTimePassed(double ms) {
        return System.currentTimeMillis() > (init + ms);
    }

    public void reset() {
        this.init = System.currentTimeMillis();
    }

}
