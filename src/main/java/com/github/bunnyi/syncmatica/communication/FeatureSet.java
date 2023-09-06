package com.github.bunnyi.syncmatica.communication;

import com.github.bunnyi.syncmatica.Feature;

import java.util.*;

// a class representing what kind of features a syncmatica instance supports
// or has enabled/disabled

public class FeatureSet {
    private static final Map<String, FeatureSet> versionFeatures;
    private final Collection<Feature> features;

    static {
        versionFeatures = new HashMap<>();
        versionFeatures.put("0.1", new FeatureSet(Collections.singletonList(Feature.CORE)));
    }

    public static FeatureSet fromVersionString(String version) {
        if (version.matches("^\\d+(\\.\\d+){2,4}$")) {
            int minSize = version.indexOf(".");
            while (version.length() > minSize) {
                if (versionFeatures.containsKey(version)) {
                    return versionFeatures.get(version);
                }
                int lastDot = version.lastIndexOf(".");
                version = version.substring(0, lastDot);
            }
        }
        return null;
    }

    public static FeatureSet fromString(final String features) {
        FeatureSet featureSet = new FeatureSet(new ArrayList<>());
        for (final String feature : features.split("\n")) {
            Feature f = Feature.fromString(feature);
            if (f != null) {
                featureSet.features.add(f);
            }
        }
        return featureSet;
    }

    @Override
    public String toString() {
        final StringBuilder output = new StringBuilder();
        boolean b = false;
        for (final Feature feature : features) {
            output.append(b ? "\n" + feature.toString() : feature.toString());
            b = true;
        }
        return output.toString();
    }

    public FeatureSet(Collection<Feature> features) {
        this.features = features;
    }

    public boolean hasFeature(Feature feature) {
        return features.contains(feature);
    }
}
