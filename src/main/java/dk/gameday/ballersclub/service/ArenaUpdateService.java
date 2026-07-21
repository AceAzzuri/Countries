package dk.gameday.ballersclub.service;

import dk.gameday.ballersclub.model.ArenaChatMessage;
import dk.gameday.ballersclub.model.ArenaUpdateItem;
import dk.gameday.ballersclub.model.LatePredictionUpdate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class ArenaUpdateService {

    private final ArenaChatService arenaChatService;
    private final PredictionService predictionService;

    public ArenaUpdateService(ArenaChatService arenaChatService, PredictionService predictionService) {
        this.arenaChatService = arenaChatService;
        this.predictionService = predictionService;
    }

    public List<ArenaUpdateItem> getLatestUpdates() {
        List<ArenaUpdateItem> updates = new ArrayList<>();

        for (ArenaChatMessage message : arenaChatService.findLatest()) {
            updates.add(new ArenaUpdateItem(
                    "Chat",
                    message.getUsername() != null ? message.getUsername() : "Anonym",
                    message.getMessage(),
                    message.getCreatedAt()
            ));
        }

        for (LatePredictionUpdate update : predictionService.findLatePredictionUpdates()) {
            updates.add(new ArenaUpdateItem(
                    "Resultat",
                    update.username(),
                    update.matchLabel() + " • pick " + update.predictionLabel(),
                    update.updatedAt()
            ));
        }

        return updates.stream()
                .sorted(Comparator.comparing(ArenaUpdateItem::occurredAt).reversed())
                .limit(8)
                .toList();
    }
}
