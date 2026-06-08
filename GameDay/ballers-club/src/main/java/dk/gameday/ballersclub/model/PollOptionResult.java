package dk.gameday.ballersclub.model;

public class PollOptionResult {

    private final PollOption option;
    private final int voteCount;
    private final int percentage;

    public PollOptionResult(PollOption option, int voteCount, int percentage) {
        this.option = option;
        this.voteCount = voteCount;
        this.percentage = percentage;
    }

    public PollOption getOption() {
        return option;
    }

    public int getVoteCount() {
        return voteCount;
    }

    public int getPercentage() {
        return percentage;
    }
}
