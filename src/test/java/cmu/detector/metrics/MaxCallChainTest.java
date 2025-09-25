package cmu.detector.metrics;

import cmu.detector.metrics.calculators.method.MaxCallChainCalculator;
import cmu.detector.resources.Type;
import cmu.detector.util.TypeLoader;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MaxCallChainTest {

    @BeforeAll
    public void setUp() throws IOException {
        File dir = new File("src/test/java/cmu/detector/dummy/kind");
        TypeLoader.loadAllFromDir(dir);

    }

    @ParameterizedTest(name = "{index} ⇒ {0} → maxChain={1}")
    @CsvSource({
            "DummyType, 0",
            "oneCallChain, 1",
            "oneMoreCallChain, 1",
            "anotherOneCallChain, 1",
            "twoCallChain, 2",
            "anotherTwoCallChain, 2",
            "threeCallChain, 3",
            "fourCallChain, 4"
    })
    void countMaxCallChain(String input, int expected) {
        //Load the class under testing
        Type typeClass = TypeLoader.findTypeByName("DummyType");

        /*
        Iterate over its methods and find the name of the method (parameter 0) and
         compare with the expected value (parameter 1)
         */
        Map<String, MethodDeclaration> byName = typeClass.getMethods().stream()
                .map(m -> (MethodDeclaration) m.getNode())
                .collect(Collectors.toMap(md -> md.getName().toString(), Function.identity()));

        MethodDeclaration method = byName.get(input);
        Assertions.assertNotNull(method, () -> "Method not found: " + input);

        MaxCallChainCalculator calculator = new MaxCallChainCalculator();
        Assertions.assertEquals(expected, calculator.getValue(method));

    }


    /*
    public void countMaxCallChain(String input, double expected) {
        Type typeClass = TypeLoader.findTypeByName("DummyType");
        MaxCallChainCalculator calculator = new MaxCallChainCalculator();

        List<Method> methods = typeClass.getMethods();
        for (Method method : methods) {
            MethodDeclaration dc = (MethodDeclaration) method.getNode();
            if (input.equals(dc.getName().toString())) {
                Assertions.assertEquals(expected, calculator.getValue(dc));
            }
        }
    }
     */
}
