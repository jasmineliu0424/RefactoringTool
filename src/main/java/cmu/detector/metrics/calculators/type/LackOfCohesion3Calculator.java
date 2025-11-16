package cmu.detector.metrics.calculators.type;

import cmu.detector.ast.visitors.FieldAccessVisitor;
import cmu.detector.ast.visitors.InstanceMembersVisitor;
import cmu.detector.metrics.MetricName;
import cmu.detector.metrics.calculators.MetricValueCalculator;
import org.eclipse.jdt.core.dom.*;

import java.util.*;

/**
 * Class to calculate the LCOM3 (Lack of Cohesion of Methods 3) metric.
 *
 * <p>
 * The equation to calculate LCOM3 is:
 * </p>
 * <ul>
 *   <li><code>m = #declaredInstanceMethods(C)</code></li>
 *   <li><code>a = #declaredInstanceAttributes(C)</code></li>
 *   <li><code>m(A) = # of instance methods in C that reference instance attribute A</code></li>
 *   <li><code>s = sum(m(A)) for A in declaredInstanceAttributes(C)</code></li>
 *   <li><code>LCOM3(C) = (m - s / a) / (m - 1)</code></li>
 * </ul>
 *
 * <p>
 * <strong>Observation:</strong> We ignore static variables and methods. If the class has no attributes or 1 method, calculating LCOM3 is impossible due to division by zero, so we assign it a value of 0.
 * </p>
 *
 * <p>
 * The metric definition, as well as its implementation details, are available at:
 * <ul>
 *   <li><a href="https://www.cs.sjsu.edu/~pearce/cs251b/DesignMetrics.htm">LCOM1, LCOM2 and LCOM3</a></li>
 *   <li><a href="http://www.aivosto.com/project/help/pm-oo-cohesion.html">Aivosto - OO Cohesion</a></li>
 * </ul>
 * </p>
 *
 * @author Leonardo Sousa
 */
public class LackOfCohesion3Calculator extends MetricValueCalculator {

    @Override
    protected Double computeValue(ASTNode target) {
        if (!(target instanceof TypeDeclaration)) {
            return 0.0;
        }

        TypeDeclaration typeDecl = (TypeDeclaration) target;
        
        // Collect instance members
        InstanceMembersVisitor membersVisitor = new InstanceMembersVisitor();
        typeDecl.accept(membersVisitor);
        
        Set<IVariableBinding> instanceFields = membersVisitor.getInstanceFields();
        List<MethodDeclaration> instanceMethods = membersVisitor.getInstanceMethods();
        
        int m = instanceMethods.size();
        int a = instanceFields.size();
        
        // If no attributes or only 1 method, return 0
        if (a == 0 || m <= 1) {
            return 0.0;
        }
        
        // Collect field access for each method
        FieldAccessVisitor fieldAccessVisitor = new FieldAccessVisitor(instanceFields);
        typeDecl.accept(fieldAccessVisitor);
        Map<MethodDeclaration, Set<IVariableBinding>> methodFieldAccess = fieldAccessVisitor.getMethodFieldAccess();
        
        // Calculate sum of m(A) for each attribute A
        int sum = 0;
        for (IVariableBinding field : instanceFields) {
            int methodsAccessingField = 0;
            for (MethodDeclaration method : instanceMethods) {
                Set<IVariableBinding> accessedFields = methodFieldAccess.get(method);
                if (accessedFields != null && accessedFields.contains(field)) {
                    methodsAccessingField++;
                }
            }
            sum += methodsAccessingField;
        }
        
        // LCOM3 = (m - s / a) / (m - 1)
        double lcom3 = (m - ((double) sum / a)) / (m - 1);
        
        return lcom3;
    }

    @Override
    public MetricName getMetricName() {
        return MetricName.LCOM3;
    }

    @Override
    public boolean shouldComputeAggregate() {
        return true;
    }
}