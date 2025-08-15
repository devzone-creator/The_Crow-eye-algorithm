import java.util.List;
import java.util.ArrayList;
import java.util.stream.IntStream;
import java.util.stream.Collectors;
import java.util.Random;

// FILE: src/demo/Main.java (KIRO ENTRY POINT)
public class Main {
    private static final Random random = new Random();

    public static void main(String[] args) {
        System.out.println("=== Crow Eye Threat Detection Simulation ===");

        // Load threats from data/threats.csv
        List<Threat> threats = loadThreats();
        System.out.println("Loaded " + threats.size() + " threats");

        // Initialize swarm (10 crows)
        List<Crow> swarm = IntStream.range(0, 10)
                .mapToObj(i -> new Crow(
                        random.nextDouble() * 100, // x position
                        random.nextDouble() * 100, // y position
                        random.nextDouble() // trust score
                ))
                .collect(Collectors.toList());

        System.out.println("Initialized swarm of " + swarm.size() + " crows");

        // Run simulation for 15 steps
        for (int step = 0; step < 15; step++) {
            System.out.println("\n--- Step " + (step + 1) + " ---");
            swarm.forEach(crow -> crow.updatePosition(threats, swarm));

            // Show veteran crows (trustScore > 0.8) leading
            swarm.stream()
                    .filter(crow -> crow.getTrustScore() > 0.8)
                    .forEach(crow -> System.out.println("LEADER: " + crow));

            // Enhanced statistics
            long fractalCrows = swarm.stream()
                    .mapToLong(c -> c.isUsingFractalPath() ? 1 : 0)
                    .sum();

            long alertCrows = swarm.stream()
                    .mapToLong(c -> c.getAlertStatus().equals("CALM") ? 0 : 1)
                    .sum();

            double avgEnergy = swarm.stream()
                    .mapToDouble(c -> Double.parseDouble(c.toString().split("E:")[1].split(" ")[0]))
                    .average().orElse(0.0);

            System.out.printf("Stats: %d fractal, %d alert, avg energy: %.1f%n",
                    fractalCrows, alertCrows, avgEnergy);

            // Show threat detection alerts
            if (alertCrows > 0) {
                System.out.printf("⚠️  SWARM ALERT: %d crows detecting threats!%n", alertCrows);
            }

            // Ethical voting on most critical threat every 5 steps
            if (step % 5 == 4 && !threats.isEmpty()) {
                System.out.println("\n🏛️  === CROW DEMOCRACY SESSION ===");

                // Find the most critical threat (closest + highest intensity)
                Threat criticalThreat = threats.stream()
                        .max((t1, t2) -> {
                            double score1 = t1.intensity * 2 - getMinDistanceToSwarm(t1, swarm);
                            double score2 = t2.intensity * 2 - getMinDistanceToSwarm(t2, swarm);
                            return Double.compare(score1, score2);
                        })
                        .orElse(threats.get(0));

                System.out.println("Voting on: " + criticalThreat);

                // Conduct the vote
                EthicalVoter.VotingResult result = EthicalVoter.detailedVote(criticalThreat, swarm);
                System.out.println(result);

                if (result.decision) {
                    System.out.println("📞 RANGERS ALERTED! Human intervention requested.");
                } else {
                    System.out.println("🤝 Swarm decides to handle threat independently.");
                }
                System.out.println("=".repeat(40));
            }
        }
    }

    // Load threats from CSV file
    private static List<Threat> loadThreats() {
        List<Threat> threats = new ArrayList<>();

        try {
            java.nio.file.Files.lines(java.nio.file.Paths.get("data/threats.csv"))
                    .skip(1) // Skip header
                    .forEach(line -> {
                        String[] parts = line.split(",");
                        if (parts.length >= 3) {
                            threats.add(new Threat(
                                    Double.parseDouble(parts[0]),
                                    Double.parseDouble(parts[1]),
                                    parts[2].trim()));
                        }
                    });
        } catch (Exception e) {
            System.err.println("Error loading CSV, using sample data: " + e.getMessage());
            // Fallback to sample data
            threats.add(new Threat(50, 30, "logging"));
            threats.add(new Threat(80, 70, "fire"));
        }

        return threats;
    }

    // Helper method to find minimum distance from threat to any crow in swarm
    private static double getMinDistanceToSwarm(Threat threat, List<Crow> swarm) {
        return swarm.stream()
                .mapToDouble(crow -> Math.hypot(threat.x - crow.getX(), threat.y - crow.getY()))
                .min()
                .orElse(Double.MAX_VALUE);
    }
}