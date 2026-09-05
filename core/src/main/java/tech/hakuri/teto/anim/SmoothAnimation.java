package tech.hakuri.teto.anim;

/**
 * 帧率无关的趋近动画（GuardLite 的 SmoothAnimationTimer 同款）。
 * value 以「与目标距离成正比的速度」逼近 target —— 距离大时快、接近时慢，
 * 这是「高级感」的主要来源。speed 系数与 GuardLite 一致（0.4）。
 */
public final class SmoothAnimation {

    public float target;
    public float value;
    public float speed = 0.4F;

    public SmoothAnimation(float target) {
        this.target = target;
        this.value = target;
    }

    public SmoothAnimation(float target, float value) {
        this.target = target;
        this.value = value;
    }

    /** 每帧调用。increment 为 false 时目标视为 0（GuardLite 的用法）。 */
    public void update(boolean increment) {
        float dest = increment ? target : 0F;
        // delta 由 AnimationClock 维护（毫秒）
        float speedPerMs = Math.max(10F, Math.abs(value - dest) * 40F) * speed;
        float add = AnimationClock.delta * (speedPerMs / 1000F);
        if (value < dest) {
            value = value + add < dest ? value + add : dest;
        } else if (value - add > dest) {
            value -= add;
        } else {
            value = dest;
        }
    }

    public boolean done(boolean increment) {
        return increment ? value == target : value == 0F;
    }
}
