import java.util.*;

/**
 * GOAL: Generate optimal crow flight paths using fractal L-systems.
 * WHY: Real crows follow fractal-like Levy flights for efficient searching.
 * INPUT: Threat locations (List<Threat>).
 * OUTPUT: Fractal waypoints for crows to follow.
 * RULES:
 *   1. Use L-system grammar (A -> A+B, B -> A-B).
 *   2. Confine fractal growth to threat-rich areas.
 *   3. Scale branches by crow trustScore.
 */
public class FractalPathfinder {
    private static final String[] L_RULES = {"A→A+B", "B→A-B"};
    private static final double BRANCH_ANGLE = Math.PI / 4; // 45 degrees
    private static final double STEP_SIZE = 5.0;
    
    public static List<double[]> generatePath(List<Threat> threats, double trustScore) {
        return generatePath(threats, trustScore, new ArrayList<>());
    }
    
    public static List<double[]> generatePath(List<Threat> threats, double trustScore, List<Threat> memory) {
        List<double[]> waypoints = new ArrayList<>();
        
        if (threats.isEmpty()) {
            waypoints.add(new double[]{0, 0});
            return waypoints;
        }
        
        // Combine current threats with memory for better pathfinding
        List<Threat> allThreats = new ArrayList<>(threats);
        allThreats.addAll(memory);
        
        // Find threat centroid as starting point
        double[] centroid = calculateThreatCentroid(allThreats);
        waypoints.add(centroid);
        
        // Generate fractal path biased toward threat clusters
        int depth = (int) (3 + trustScore * 2); // Veteran crows get deeper recursion
        String lSystem = generateLSystem("A", depth);
        
        // Convert L-system string to waypoints
        double currentX = centroid[0];
        double currentY = centroid[1];
        double currentAngle = 0;
        
        for (char command : lSystem.toCharArray()) {
            switch (command) {
                case 'A':
                case 'B':
                    // Move forward with bias toward nearest threat (including memory)
                    double[] nearestThreat = findNearestThreat(allThreats, currentX, currentY);
                    double biasAngle = Math.atan2(nearestThreat[1] - currentY, nearestThreat[0] - currentX);
                    
                    // Blend fractal direction with threat bias
                    double blendedAngle = currentAngle * 0.7 + biasAngle * 0.3;
                    
                    currentX += STEP_SIZE * Math.cos(blendedAngle) * trustScore;
                    currentY += STEP_SIZE * Math.sin(blendedAngle) * trustScore;
                    waypoints.add(new double[]{currentX, currentY});
                    break;
                case '+':
                    currentAngle += BRANCH_ANGLE;
                    break;
                case '-':
                    currentAngle -= BRANCH_ANGLE;
                    break;
            }
        }
        
        return waypoints;
    }
    
    private static String generateLSystem(String axiom, int iterations) {
        String current = axiom;
        
        for (int i = 0; i < iterations; i++) {
            StringBuilder next = new StringBuilder();
            for (char c : current.toCharArray()) {
                switch (c) {
                    case 'A':
                        next.append("A+B"); // A → A+B
                        break;
                    case 'B':
                        next.append("A-B"); // B → A-B
                        break;
                    default:
                        next.append(c);
                        break;
                }
            }
            current = next.toString();
        }
        
        return current;
    }
    
    private static double[] calculateThreatCentroid(List<Threat> threats) {
        double sumX = 0, sumY = 0;
        for (Threat threat : threats) {
            sumX += threat.x;
            sumY += threat.y;
        }
        return new double[]{sumX / threats.size(), sumY / threats.size()};
    }
    
    private static double[] findNearestThreat(List<Threat> threats, double x, double y) {
        double minDistance = Double.MAX_VALUE;
        double[] nearest = {x, y};
        
        for (Threat threat : threats) {
            double distance = Math.hypot(threat.x - x, threat.y - y);
            if (distance < minDistance) {
                minDistance = distance;
                nearest = new double[]{threat.x, threat.y};
            }
        }
        
        return nearest;
    }
}