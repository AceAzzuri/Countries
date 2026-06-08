package dk.gameday.ballersclub.service;

import dk.gameday.ballersclub.model.AppUser;
import dk.gameday.ballersclub.model.LeaderboardRow;
import dk.gameday.ballersclub.model.Pool;
import dk.gameday.ballersclub.model.PoolLeaderboardView;
import dk.gameday.ballersclub.model.PoolMembership;
import dk.gameday.ballersclub.model.Prediction;
import dk.gameday.ballersclub.repository.PoolMembershipRepository;
import dk.gameday.ballersclub.repository.PoolRepository;
import dk.gameday.ballersclub.repository.PredictionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PoolService {

    private static final String CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 6;

    private final PoolRepository poolRepository;
    private final PoolMembershipRepository membershipRepository;
    private final PredictionRepository predictionRepository;
    private final LeaderboardService leaderboardService;
    private final SecureRandom random = new SecureRandom();

    public PoolService(
            PoolRepository poolRepository,
            PoolMembershipRepository membershipRepository,
            PredictionRepository predictionRepository,
            LeaderboardService leaderboardService
    ) {
        this.poolRepository = poolRepository;
        this.membershipRepository = membershipRepository;
        this.predictionRepository = predictionRepository;
        this.leaderboardService = leaderboardService;
    }

    @Transactional
    public Pool createPool(AppUser owner, String rawName) {
        String name = normalizeName(rawName);
        Pool pool = poolRepository.save(new Pool(name, generateCode(), owner));
        membershipRepository.save(new PoolMembership(pool, owner));
        return pool;
    }

    @Transactional
    public Pool joinPool(AppUser user, String rawCode) {
        String code = normalizeCode(rawCode);
        Pool pool = poolRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new IllegalArgumentException("Liga-koden blev ikke fundet."));
        if (membershipRepository.existsByPoolAndUser(pool, user)) {
            throw new IllegalArgumentException("Du er allerede med i den liga.");
        }
        membershipRepository.save(new PoolMembership(pool, user));
        return pool;
    }

    @Transactional(readOnly = true)
    public List<PoolLeaderboardView> findPoolLeaderboards(AppUser user) {
        List<Pool> pools = membershipRepository.findByUserWithPool(user).stream()
                .map(PoolMembership::getPool)
                .toList();
        List<Prediction> allPredictions = predictionRepository.findAllForLeaderboard();

        return pools.stream()
                .map(pool -> toLeaderboardView(pool, allPredictions))
                .toList();
    }

    private PoolLeaderboardView toLeaderboardView(Pool pool, List<Prediction> allPredictions) {
        Set<Long> memberIds = membershipRepository.findByPoolWithUser(pool).stream()
                .map(membership -> membership.getUser().getId())
                .collect(Collectors.toSet());
        List<Prediction> poolPredictions = allPredictions.stream()
                .filter(prediction -> memberIds.contains(prediction.getUser().getId()))
                .toList();
        List<LeaderboardRow> rows = leaderboardService.buildLeaderboard(poolPredictions);
        return new PoolLeaderboardView(pool, memberIds.size(), rows);
    }

    private String normalizeName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Giv ligaen et navn.");
        }
        String normalized = name.trim().replaceAll("\\s+", " ");
        if (normalized.length() > 60) {
            throw new IllegalArgumentException("Liga-navn må maks være 60 tegn.");
        }
        return normalized;
    }

    private String normalizeCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Indtast en liga-kode.");
        }
        return code.trim().replaceAll("\\s+", "").toUpperCase();
    }

    private String generateCode() {
        String code;
        do {
            StringBuilder builder = new StringBuilder(CODE_LENGTH);
            for (int i = 0; i < CODE_LENGTH; i++) {
                builder.append(CODE_ALPHABET.charAt(random.nextInt(CODE_ALPHABET.length())));
            }
            code = builder.toString();
        } while (poolRepository.existsByCodeIgnoreCase(code));
        return code;
    }
}
