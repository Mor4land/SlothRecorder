package space.kaelus.slothrecorder.logic;

import space.kaelus.slothrecorder.util.RunningMode;
import space.kaelus.slothrecorder.util.SlothMath;

import java.util.UUID;

public class PlayerTracker {
    private final UUID uuid;
    private final String name;

    private float lastYaw;
    private float lastPitch;
    private float lastDeltaYaw;
    private float lastDeltaPitch;
    private float lastYawAccel;
    private float lastPitchAccel;

    private float currentYawAccel;
    private float currentPitchAccel;

    private final RunningMode xRotMode = new RunningMode(80);
    private final RunningMode yRotMode = new RunningMode(80);
    private double modeX = 0.0;
    private double modeY = 0.0;

    private double lastXRot = 0.0;
    private double lastYRot = 0.0;

    public PlayerTracker(UUID uuid, String name, float yaw, float pitch) {
        this.uuid = uuid;
        this.name = name;
        this.lastYaw = yaw;
        this.lastPitch = pitch;
    }

    public void update(float yaw, float pitch) {
        float deltaYaw = yaw - lastYaw;
        float deltaPitch = pitch - lastPitch;

        float deltaYawAbs = Math.abs(deltaYaw);
        float deltaPitchAbs = Math.abs(deltaPitch);

        lastYawAccel = currentYawAccel;
        lastPitchAccel = currentPitchAccel;

        currentYawAccel = deltaYawAbs - Math.abs(lastDeltaYaw);
        currentPitchAccel = deltaPitchAbs - Math.abs(lastDeltaPitch);

        lastDeltaYaw = deltaYaw;
        lastDeltaPitch = deltaPitch;
        lastYaw = yaw;
        lastPitch = pitch;

        // GCD Logic
        double divisorX = SlothMath.gcd(deltaYawAbs, lastXRot);
        if (deltaYawAbs > 0 && deltaYawAbs < 5 && divisorX > SlothMath.getMinimumDivisor()) {
            xRotMode.add(divisorX);
            lastXRot = deltaYawAbs;
        }

        double divisorY = SlothMath.gcd(deltaPitchAbs, lastYRot);
        if (deltaPitchAbs > 0 && deltaPitchAbs < 5 && divisorY > SlothMath.getMinimumDivisor()) {
            yRotMode.add(divisorY);
            lastYRot = deltaPitchAbs;
        }

        if (xRotMode.size() > 15) {
            xRotMode.updateMode();
            if (xRotMode.getModeCount() > 15) {
                modeX = xRotMode.getModeValue();
            }
        }

        if (yRotMode.size() > 15) {
            yRotMode.updateMode();
            if (yRotMode.getModeCount() > 15) {
                modeY = yRotMode.getModeValue();
            }
        }
    }

    public String toCsvRow(boolean isCheating) {
        float jerkYaw = currentYawAccel - lastYawAccel;
        float jerkPitch = currentPitchAccel - lastPitchAccel;

        double gcdErrorYaw = 0;
        if (modeX > 0) {
            double errorX = Math.abs(lastDeltaYaw % modeX);
            gcdErrorYaw = Math.min(errorX, modeX - errorX);
        }

        double gcdErrorPitch = 0;
        if (modeY > 0) {
            double errorY = Math.abs(lastDeltaPitch % modeY);
            gcdErrorPitch = Math.min(errorY, modeY - errorY);
        }

        return String.format("%d,%.6f,%.6f,%.6f,%.6f,%.6f,%.6f,%.6f,%.6f",
                isCheating ? 1 : 0,
                lastDeltaYaw, lastDeltaPitch,
                currentYawAccel, currentPitchAccel,
                jerkYaw, jerkPitch,
                gcdErrorYaw, gcdErrorPitch);
    }

    public String getName() {
        return name;
    }

    public UUID getUuid() {
        return uuid;
    }

    public float getLastDeltaYaw() {
        return lastDeltaYaw;
    }

    public float getLastDeltaPitch() {
        return lastDeltaPitch;
    }

    public double getModeX() {
        return modeX;
    }

    public double getModeY() {
        return modeY;
    }
}
