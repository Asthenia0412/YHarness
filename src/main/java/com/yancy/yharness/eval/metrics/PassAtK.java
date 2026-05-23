package com.yancy.yharness.eval.metrics;

import lombok.Data;

import java.util.List;

@Data
public class PassAtK {
    private double passAt1;
    private double passAt3;
    private double passAt5;
    private double passK;

    public static PassAtK calculate(List<Boolean> trialResults, int k) {
        PassAtK result = new PassAtK();
        if (trialResults == null || trialResults.isEmpty()) return result;

        long passed = trialResults.stream().filter(b -> b).count();
        double successRate = (double) passed / trialResults.size();

        result.setPassAt1(successRate);

        if (trialResults.size() >= k) {
            List<Boolean> firstK = trialResults.subList(0, k);
            double rate = (double) firstK.stream().filter(b -> b).count() / k;
            if (k == 3) {
                result.setPassAt3(rate);
            } else if (k == 5) {
                result.setPassAt5(rate);
            }
        }

        result.setPassK(1.0 - Math.pow(1.0 - successRate, k));
        return result;
    }
}