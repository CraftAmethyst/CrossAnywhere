package io.github.qianmo0721.crossAnywhere.safety;

public final class SafetyResult {
    private final boolean safe;
    private final String reason;

    public SafetyResult(boolean safe, String reason) {
        this.safe = safe;
        this.reason = reason;
    }

    public boolean isSafe() {
        return safe;
    }

    public String getReason() {
        return reason;
    }
}
