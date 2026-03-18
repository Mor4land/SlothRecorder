package space.kaelus.slothrecorder.util;

import it.unimi.dsi.fastutil.doubles.Double2IntMap;
import it.unimi.dsi.fastutil.doubles.Double2IntOpenHashMap;
import java.util.ArrayDeque;
import java.util.Queue;

public class RunningMode {
    private final int maxSize;
    private final Queue<Double> addList;
    private final Double2IntMap popularityMap = new Double2IntOpenHashMap();
    private double modeValue = 0.0;
    private int modeCount = 0;
    private static final double THRESHOLD = 1e-3;

    public RunningMode(int maxSize) {
        if (maxSize == 0) {
            throw new IllegalArgumentException("There's no mode to a size 0 list!");
        }
        this.maxSize = maxSize;
        this.addList = new ArrayDeque<>(maxSize);
    }

    public int size() {
        return addList.size();
    }

    public void add(double value) {
        pop();

        for (Double2IntMap.Entry entry : popularityMap.double2IntEntrySet()) {
            if (Math.abs(entry.getDoubleKey() - value) < THRESHOLD) {
                entry.setValue(entry.getIntValue() + 1);
                addList.add(entry.getDoubleKey());
                return;
            }
        }

        popularityMap.put(value, 1);
        addList.add(value);
    }

    private void pop() {
        if (addList.size() >= maxSize) {
            Double type = addList.poll();
            if (type != null) {
                int popularity = popularityMap.get((double) type);
                if (popularity == 1) {
                    popularityMap.remove((double) type);
                } else {
                    popularityMap.put((double) type, popularity - 1);
                }
            }
        }
    }

    public void updateMode() {
        int max = 0;
        double mostPopular = 0.0;

        for (Double2IntMap.Entry entry : popularityMap.double2IntEntrySet()) {
            if (entry.getIntValue() > max) {
                max = entry.getIntValue();
                mostPopular = entry.getDoubleKey();
            }
        }

        modeValue = mostPopular;
        modeCount = max;
    }

    public double getModeValue() {
        return modeValue;
    }

    public int getModeCount() {
        return modeCount;
    }
}
