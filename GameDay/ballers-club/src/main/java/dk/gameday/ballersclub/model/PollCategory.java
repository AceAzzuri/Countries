package dk.gameday.ballersclub.model;

public enum PollCategory {
    TOURNAMENT("Bonus pick"),
    PLAYER("Bonus award"),
    CULTURE("Fan/kultur"),
    DAILY("Dagens pick");

    private final String label;

    PollCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
