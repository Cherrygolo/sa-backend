package ld.sa_backend.dto;

public class ReviewStatsDTO {

    private final long positive;
    private final long negative;
    private final long neutral;

    public ReviewStatsDTO(long positive, long negative, long neutral) {
        this.positive = positive;
        this.negative = negative;
        this.neutral = neutral;
    }

    public long getPositive() {
        return positive;
    }

    public long getNegative() {
        return negative;
    }

    public long getNeutral() {
        return neutral;
    }

}
