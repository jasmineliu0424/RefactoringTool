package cmu.detector.dummy.metrics;

// This class is designed to have methods with varying cyclomatic complexity (CC) values, WMC = 27
public class DummyCyclomaticComplexity {
    // Simple method with no control flow CC = 1
    public void simpleMethod() {
        int a = 1;
        int b = 2;
        int c = a + b;
        System.out.println(c);
    }

    // Method with an if-else statement CC = 2
    public void ifElseMethod(int x) {
        if (x > 0) {
            System.out.println("Positive");
        } else {
            System.out.println("Non-positive");
        }
    }

    // Method with a switch statement CC = 4
    public void switchMethod(int y) {
        switch (y) {
            case 1:
                System.out.println("One");
                break;
            case 2:
                System.out.println("Two");
                break;
            case 3:
                System.out.println("Three");
                break;
        }
    }

    // Method with a switch statement CC = 5
    public void switchMethodDefault(int y) {
        switch (y) {
            case 1:
                System.out.println("One");
                break;
            case 2:
                System.out.println("Two");
                break;
            case 3:
                System.out.println("Three");
                break;
            default:
                System.out.println("Other");
                break;
        }
    }

    // Method with a for loop CC = 2
    public void forLoopMethod() {
        for (int i = 0; i < 5; i++) {
            System.out.println(i);
        }
    }

    // Method with a while loop CC = 2
    public void whileLoopMethod(int n) {
        int i = 0;
        while (i < n) {
            System.out.println(i);
            i++;
        }
    }

    // Method with a do-while loop CC = 2
    public void doWhileLoopMethod(int n) {
        int i = 0;
        do {
            System.out.println(i);
            i++;
        } while (i < n);
    }

    // Method with nested control structures CC = 4
    public void nestedControlMethod(int a, int b) {
        if (a > 0) {
            for (int i = 0; i < b; i++) {
                if (i % 2 == 0) {
                    System.out.println(i + " is even");
                } else {
                    System.out.println(i + " is odd");
                }
            }
        } else {
            System.out.println("a is non-positive");
        }
    }

    // Method with multiple return points CC = 3
    public int multipleReturnMethod(int x) {
        if (x < 0) {
            return -1;
        } else if (x == 0) {
            return 0;
        } else {
            return 1;
        }
    }

    // Method with try-catch-finally block CC = 2
    public void tryCatchFinallyMethod() {
        try {
            System.out.println("In try block");
        } catch (Exception e) {
            System.out.println("In catch block");
        } finally {
            System.out.println("In finally block");
        }
    }
}
