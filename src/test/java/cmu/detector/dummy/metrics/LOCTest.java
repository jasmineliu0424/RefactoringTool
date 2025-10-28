package cmu.detector.dummy.metrics;

import cmu.detector.metrics.calculators.method.MethodLOCCalculator;
import cmu.detector.metrics.calculators.type.TypeLOCCalculator;
import cmu.detector.resources.Method;
import cmu.detector.resources.Type;
import cmu.detector.util.TypeLoader;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.File;
import java.io.IOException;
import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LOCTest {

    @BeforeAll
    public void setUp() throws IOException {
        File dir = new File("src/test/java/cmu/detector/dummy/kind");
        TypeLoader.loadAllFromDir(dir);
    }

    @ParameterizedTest
    @CsvSource({
            "DummyAbstractType, 29",
            "DummyFinalType, 19",
            "DummyInterface, 4",
            "DummyType, 52",
            "EdgeCases, 60"
    })
    public void countTypeLines(String input, double expected) {
        Type typeClass = TypeLoader.findTypeByName(input);

        TypeLOCCalculator calculator = new TypeLOCCalculator();

        Assertions.assertEquals(expected, calculator.getValue(typeClass.getNode()));
    }

    @ParameterizedTest
    @CsvSource({
            "publicMethod, 3",
            "protectedMethod, 3",
            "privateMethod, 3",
            "packagePrivateMethod, 3",
            "publicStaticMethod, 3",
            "packagePrivateStaticMethod, 3",
            "privateFinalMethod, 3",
            "packagePrivateFinalMethod, 3",
            "publicAbstractMethod, 1",
            "packagePrivateAbstractMethod, 1"
    })
    public void countMethodLines(String input, double expected) {
            Type typeClass = TypeLoader.findTypeByName("DummyAbstractType");
        MethodLOCCalculator calculator = new MethodLOCCalculator();

        List<Method> methods = typeClass.getMethods();
        for (Method method : methods) {
            MethodDeclaration dc = (MethodDeclaration) method.getNode();
            if (input.equals(dc.getName().toString())) {
                Assertions.assertEquals(expected, calculator.getValue(dc));
            }
        }
    }

    @ParameterizedTest
    @CsvSource({
            "method1, 5",
            "method2, 9",
            "method3, 6",
            "method4, 10",
            "testMultipleAnnotation, 8",
            "provideTestArgumentsForClassNames, 6",
            "provideTestArgumentsForInterfaceNames, 5"

    })
    public void countMethodLinesEdgeCases(String input, double expected) {
        Type typeClass = TypeLoader.findTypeByName("EdgeCases");
        MethodLOCCalculator calculator = new MethodLOCCalculator();

        List<Method> methods = typeClass.getMethods();
        for (Method method : methods) {
            MethodDeclaration dc = (MethodDeclaration) method.getNode();
            if (input.equals(dc.getName().toString())) {
                Assertions.assertEquals(expected, calculator.getValue(dc));
            }
        }
    }
}
