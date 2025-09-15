// FILE: src/core/Threat.java
public class Threat {
    public double x, y;
    public String type; // "logging" or "fire"
    public double intensity; // 0.1 to 1.0 - threat severity

    public Threat() {
        this.intensity = 0.5; // Default intensity
    }

    public Threat(double x, double y, String type) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.intensity = type.equals("logging") ? 0.8 : 0.6; // Logging more intense
    }

    public Threat(double x, double y, String type, double intensity) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.intensity = Math.max(0.1, Math.min(1.0, intensity));
    }

    @Override
    public String toString() {
        return String.format("Threat[%s %.0f%% at %.1f,%.1f]", type, intensity * 100, x, y);
    }
}