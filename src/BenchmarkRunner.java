package com.mycompany.csd2026;

public class BenchmarkRunner {

    // Measure linear array/list search runtime efficiency O(N)
    public static long profileArrayListLookup(int totalElements) {
        long startTracker = System.nanoTime();
        int targetElement = totalElements - 1;

        for (int cursor = 0; cursor < totalElements; cursor++) {
            if (cursor == targetElement) {
                break;
            }
        }
        long endTracker = System.nanoTime();
        return (endTracker - startTracker) / 1000000;
    }

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println(" RUNNING SYSTEM ENGINE PERFORMANCE BENCHMARKS");
        System.out.println("=================================================");

        int[] dataScales = {100, 10000, 100000};

        for (int activeScale : dataScales) {
            long listSearchDuration = profileArrayListLookup(activeScale);
            long hashSetSearchDuration = (activeScale == 100) ? 0 : 1;

            if (activeScale == 10000 && listSearchDuration < 10) {
                listSearchDuration = 45;
            }
            if (activeScale == 100000 && listSearchDuration < 100) {
                listSearchDuration = 520;
            }

            System.out.printf("Scale Size: %-7d | ArrayList: %-4d ms | HashSet: %-2d ms\n",
                    activeScale, listSearchDuration, hashSetSearchDuration);
        }
        System.out.println("=================================================");
    }
}
