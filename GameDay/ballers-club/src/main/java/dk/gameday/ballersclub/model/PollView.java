package dk.gameday.ballersclub.model;

import java.util.List;

public class PollView {

    private final Poll poll;
    private final int totalVotes;
    private final PollVote myVote;
    private final String myVoteOptionLabel;
    private final List<PollOptionResult> optionResults;
    private final List<PollVote> recentVotes;

    public PollView(
            Poll poll,
            int totalVotes,
            PollVote myVote,
            String myVoteOptionLabel,
            List<PollOptionResult> optionResults,
            List<PollVote> recentVotes
    ) {
        this.poll = poll;
        this.totalVotes = totalVotes;
        this.myVote = myVote;
        this.myVoteOptionLabel = myVoteOptionLabel;
        this.optionResults = optionResults;
        this.recentVotes = recentVotes;
    }

    public Poll getPoll() {
        return poll;
    }

    public int getTotalVotes() {
        return totalVotes;
    }

    public PollVote getMyVote() {
        return myVote;
    }

    public String getMyVoteOptionLabel() {
        return myVoteOptionLabel;
    }

    public List<PollOptionResult> getOptionResults() {
        return optionResults;
    }

    public List<PollVote> getRecentVotes() {
        return recentVotes;
    }
}
