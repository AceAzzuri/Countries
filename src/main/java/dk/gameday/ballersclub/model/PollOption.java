package dk.gameday.ballersclub.model;

public class PollOption {

    private final Long id;
    private final String label;

    public PollOption(Long id, String label) {
        this.id = id;
        this.label = label;
    }

    public Long getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }
}
