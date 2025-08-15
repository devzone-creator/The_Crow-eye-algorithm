import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * GOAL: Simulate a swarm of crows that flock toward environmental threats using Bayesian logic.
 * INPUT: Threat data (coordinates + type: "logging", "fire").
 * OUTPUT: Crow movement patterns and threat detection alerts.
 * BEHAVIOR RULES (YOUR INPUT):
 *   1. Crows prioritize loud sounds (logging > fire > noise).
 *   2. They avoid crowding (min. 10m apart).
 *   3. Older crows (trustScore > 0.8) lead the swarm.
 *   4. Veteran crows (trustScore > 0.7) use fractal Levy flight patterns.
 */

// FILE: src/core/Crow.java
public class Crow {
    private double x, y;           // Position
    private double trustScore;     // 0.0 (new) to 1.0 (veteran)
    private String lastThreatType; // "logging", "fire", or null
    private List<double[]> fractalPath; // Fractal waypoints for veteran crows
    private int currentWaypoint = 0;
    private List<Threat> memoryBank; // Veteran crows remember past threats
    private int alertLevel = 0;     // 0=calm, 1=alert, 2=alarmed
    private double energy = 100.0;  // Energy depletes with movement

    // YOUR INPUT: Adjust weights for threat priority
    private static final double LOGGING_WEIGHT = 0.9;
    private static final double FIRE_WEIGHT = 0.7;

    public void updatePosition(List<Threat> threats, List<Crow> flock) {
        // Update memory and alert level
        updateMemoryAndAlerts(threats);
        
        // Energy management - tired crows move slower
        double energyFactor = Math.max(0.3, energy / 100.0);
        
        // Veteran crows use fractal pathfinding for efficient search
        if (this.trustScore > 0.7 && energy > 20.0) {
            if (fractalPath == null || fractalPath.isEmpty() || shouldRecalculatePath()) {
                fractalPath = FractalPathfinder.generatePath(threats, this.trustScore, memoryBank);
                currentWaypoint = 0;
            }
            
            // Follow fractal waypoints with energy consideration
            if (currentWaypoint < fractalPath.size()) {
                double[] waypoint = fractalPath.get(currentWaypoint);
                double dx = waypoint[0] - this.x;
                double dy = waypoint[1] - this.y;
                double distance = Math.hypot(dx, dy);
                
                if (distance < 2.0) { // Reached waypoint
                    currentWaypoint++;
                    energy += 2.0; // Small energy boost for reaching waypoint
                } else {
                    // Move toward waypoint with energy and alert modifiers
                    double speed = 0.5 * trustScore * energyFactor * (1 + alertLevel * 0.3);
                    this.x += (dx / distance) * speed;
                    this.y += (dy / distance) * speed;
                    energy -= 0.8; // Energy cost for movement
                }
            }
        } else {
            // Regular crows or low-energy veterans use simple targeting
            Threat target = selectThreat(threats);
            if (target != null) {
                double speed = 0.01 * trustScore * energyFactor;
                this.x += (target.x - this.x) * speed;
                this.y += (target.y - this.y) * speed;
                energy -= 0.5;
            }
        }

        // Always avoid crowding and manage energy
        avoidCrowding(flock);
        energy = Math.min(100.0, energy + 0.1); // Slow energy recovery
    }

    // Constructor
    public Crow(double x, double y, double trustScore) {
        this.x = x;
        this.y = y;
        this.trustScore = trustScore;
        this.lastThreatType = null;
        this.fractalPath = null;
        this.currentWaypoint = 0;
        this.memoryBank = new ArrayList<>();
        this.alertLevel = 0;
        this.energy = 100.0;
    }

    // Getters for position
    public double getX() { return x; }
    public double getY() { return y; }
    public double getTrustScore() { return trustScore; }
    public boolean isVeteran() { return trustScore > 0.8; }

    // YOUR INPUT: Change threat selection logic if needed
    private Threat selectThreat(List<Threat> threats) {
        if (threats.isEmpty()) return null;
        
        return threats.stream()
            .max(Comparator.comparingDouble(t -> {
                double weight = t.type.equals("logging") ? LOGGING_WEIGHT : FIRE_WEIGHT;
                double distance = Math.sqrt(Math.pow(t.x - this.x, 2) + Math.pow(t.y - this.y, 2));
                return weight / (1 + distance * 0.01); // Bayesian: priority / distance
            }))
            .orElse(null);
    }

    // Avoid crowding implementation
    private void avoidCrowding(List<Crow> flock) {
        for (Crow other : flock) {
            if (other == this) continue;
            
            double distance = Math.sqrt(Math.pow(other.x - this.x, 2) + Math.pow(other.y - this.y, 2));
            if (distance < 10.0 && distance > 0) { // Min 10m apart
                double pushX = (this.x - other.x) / distance * 0.5;
                double pushY = (this.y - other.y) / distance * 0.5;
                this.x += pushX;
                this.y += pushY;
            }
        }
    }

    // Memory and learning system
    private void updateMemoryAndAlerts(List<Threat> threats) {
        // Veterans remember threats they've encountered
        if (trustScore > 0.7) {
            for (Threat threat : threats) {
                double distance = Math.hypot(threat.x - x, threat.y - y);
                if (distance < 15.0 && !memoryBank.contains(threat)) {
                    memoryBank.add(threat);
                    if (memoryBank.size() > 10) { // Limit memory size
                        memoryBank.remove(0);
                    }
                }
            }
        }
        
        // Update alert level based on nearby threats
        long nearbyThreats = threats.stream()
            .mapToLong(t -> Math.hypot(t.x - x, t.y - y) < 20.0 ? 1 : 0)
            .sum();
        
        if (nearbyThreats >= 3) alertLevel = 2; // Alarmed
        else if (nearbyThreats >= 1) alertLevel = 1; // Alert
        else alertLevel = Math.max(0, alertLevel - 1); // Calm down gradually
    }
    
    private boolean shouldRecalculatePath() {
        return currentWaypoint >= fractalPath.size() * 0.8 || alertLevel >= 2;
    }

    public boolean isUsingFractalPath() {
        return trustScore > 0.7 && fractalPath != null && energy > 20.0;
    }
    
    public String getAlertStatus() {
        return switch (alertLevel) {
            case 0 -> "CALM";
            case 1 -> "ALERT";
            case 2 -> "ALARMED";
            default -> "UNKNOWN";
        };
    }

    @Override
    public String toString() {
        String pathType = isUsingFractalPath() ? " [FRACTAL]" : " [DIRECT]";
        String status = String.format(" E:%.0f %s", energy, getAlertStatus());
        return String.format("Crow[%.1f,%.1f] trust=%.2f%s%s", x, y, trustScore, pathType, status);
    }
}