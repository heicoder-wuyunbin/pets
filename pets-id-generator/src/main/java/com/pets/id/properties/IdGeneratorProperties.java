package com.pets.id.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pets.id-generator")
public class IdGeneratorProperties {

    private int step = 1000;

    private double prefetchThreshold = 0.2;

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public double getPrefetchThreshold() {
        return prefetchThreshold;
    }

    public void setPrefetchThreshold(double prefetchThreshold) {
        this.prefetchThreshold = prefetchThreshold;
    }
}
