/**
 * GOAL: Simulate crows voting on whether to alert humans about threats.
 * WHY: Real crows debate risks (e.g., nest safety vs. food rewards).
 * INPUT: Threat type + crow trustScores.
 * OUTPUT: Boolean (true = alert rangers).
 * RULES:
 *   1. Logging threats always trigger alerts.
 *   2. Fire alerts require 70% crow consensus.
 *   3. Veteran crows (trustScore > 0.8) get 2x voting power.
 */

import java.util.*;

public class EthicalVoter {
    private static final double CONSENSUS_THRESHOLD = 0.7;
    private static final double VETERAN_VOTE_MULTIPLIER = 2.0;
    
    public static boolean vote(Threat threat, List<Crow> flock) {
        if (flock.isEmpty()) return false;
        
        // Rule 1: Logging threats always trigger alerts (immediate danger)
        if (threat.type.equals("logging")) {
            System.out.println("🚨 LOGGING DETECTED - Automatic alert to rangers!");
            return true;
        }
        
        // Calculate weighted votes for other threats
        double totalVotingPower = 0;
        double yesVotes = 0;
        
        for (Crow crow : flock) {
            double votingPower = crow.isVeteran() ? VETERAN_VOTE_MULTIPLIER : 1.0;
            totalVotingPower += votingPower;
            
            // Crows vote based on trust score, alert level, and threat intensity
            double voteStrength = calculateVoteStrength(crow, threat);
            if (voteStrength > 0.5) { // Crow votes YES to alert
                yesVotes += votingPower;
            }
        }
        
        double consensusRatio = yesVotes / totalVotingPower;
        boolean shouldAlert = consensusRatio >= CONSENSUS_THRESHOLD;
        
        // Detailed voting results
        System.out.printf("🗳️  CROW DEMOCRACY: %.1f%% consensus (need %.0f%%)%n", 
            consensusRatio * 100, CONSENSUS_THRESHOLD * 100);
        System.out.printf("   Voting power: %.1f YES / %.1f TOTAL%n", yesVotes, totalVotingPower);
        
        return shouldAlert;
    }
    
    private static double calculateVoteStrength(Crow crow, Threat threat) {
        double baseVote = crow.getTrustScore(); // More experienced = stronger opinion
        
        // Alert crows are more likely to vote for human intervention
        double alertModifier = switch (crow.getAlertStatus()) {
            case "CALM" -> 0.0;
            case "ALERT" -> 0.2;
            case "ALARMED" -> 0.4;
            default -> 0.0;
        };
        
        // Threat intensity affects voting
        double intensityModifier = threat.intensity * 0.3;
        
        // Distance to threat affects concern level
        double distance = Math.hypot(threat.x - crow.getX(), threat.y - crow.getY());
        double proximityModifier = Math.max(0, (50 - distance) / 50) * 0.2;
        
        return Math.min(1.0, baseVote + alertModifier + intensityModifier + proximityModifier);
    }
    
    // Advanced voting with different threat scenarios
    public static VotingResult detailedVote(Threat threat, List<Crow> flock) {
        VotingResult result = new VotingResult();
        result.threat = threat;
        result.totalCrows = flock.size();
        
        if (flock.isEmpty()) {
            result.decision = false;
            result.reason = "No crows available to vote";
            return result;
        }
        
        // Automatic alert for logging
        if (threat.type.equals("logging")) {
            result.decision = true;
            result.reason = "Logging threat - automatic alert";
            result.consensusRatio = 1.0;
            return result;
        }
        
        // Count votes by category
        for (Crow crow : flock) {
            double votingPower = crow.isVeteran() ? VETERAN_VOTE_MULTIPLIER : 1.0;
            result.totalVotingPower += votingPower;
            
            if (crow.isVeteran()) result.veteranVoters++;
            
            double voteStrength = calculateVoteStrength(crow, threat);
            if (voteStrength > 0.5) {
                result.yesVotes += votingPower;
                result.yesVoters++;
            } else {
                result.noVoters++;
            }
        }
        
        result.consensusRatio = result.yesVotes / result.totalVotingPower;
        result.decision = result.consensusRatio >= CONSENSUS_THRESHOLD;
        result.reason = String.format("%.1f%% consensus %s threshold", 
            result.consensusRatio * 100, 
            result.decision ? "exceeds" : "below");
        
        return result;
    }
    
    // Voting result data structure
    public static class VotingResult {
        public boolean decision;
        public String reason;
        public Threat threat;
        public int totalCrows;
        public int veteranVoters;
        public int yesVoters;
        public int noVoters;
        public double yesVotes;
        public double totalVotingPower;
        public double consensusRatio;
        
        @Override
        public String toString() {
            return String.format("Vote Result: %s (%s) - %d/%d crows, %.1f%% consensus", 
                decision ? "ALERT" : "NO ALERT", reason, yesVoters, totalCrows, consensusRatio * 100);
        }
    }
}