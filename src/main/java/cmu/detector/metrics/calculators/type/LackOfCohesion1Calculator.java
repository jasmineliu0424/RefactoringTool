package cmu.detector.metrics.calculators.type;

import cmu.detector.ast.visitors.FieldAccessVisitor;
import cmu.detector.ast.visitors.InstanceMembersVisitor;
import cmu.detector.metrics.MetricName;
import cmu.detector.metrics.calculators.MetricValueCalculator;
import org.eclipse.jdt.core.dom.*;

import java.util.*;

/**
 * Class to calculate the LCOM1 (Lack of Cohesion of Methods) metric.
 *
 * <p>
 * LCOM1 was introduced in the <a href="https://www.aivosto.com/project/help/pm-oo-ck.html">Chidamber & Kemerer</a> metrics suite. It is also known as LCOM or LOCOM, and is calculated as follows:
 * </p>
 * <ul>
 *   <li>Take each pair of instance methods in the class (we don't consider static methods because they belong to the class rather than the instance).</li>
 *   <li>If they access disjoint sets of instance variables (i.e., non-static variables), we increase the <code>P</code> by one.</li>
 *   <li>If they share at least one variable access, increase <code>Q</code> by one.</li>
 *   <li><code>LCOM1 = P − Q</code>, if <code>P > Q</code></li>
 *   <li><code>LCOM1 = 0</code> otherwise</li>
 * </ul>
 *
 * <p>
 * Interpretation:
 * </p>
 * <ul>
 *   <li><code>LCOM1 = 0</code> indicates a cohesive class.</li>
 *   <li><code>LCOM1 > 0</code> indicates that the class could benefit from being split into two or more classes, as its variables belong in disjoint sets.</li>
 * </ul>
 *
 * <p>
 * Classes with a high LCOM1 value have been found to be fault-prone.
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
public class LackOfCohesion1Calculator extends MetricValueCalculator {

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
        
        if (instanceMethods.size() < 2) {
            return 0.0;
        }
        
        // Collect field access for each method
        FieldAccessVisitor fieldAccessVisitor = new FieldAccessVisitor(instanceFields);
        typeDecl.accept(fieldAccessVisitor);
        Map<MethodDeclaration, Set<IVariableBinding>> methodFieldAccess = fieldAccessVisitor.getMethodFieldAccess();
        
        // Count P and Q
        int P = 0; // Pairs with disjoint field access
        int Q = 0; // Pairs with shared field access
        
        for (int i = 0; i < instanceMethods.size(); i++) {
            for (int j = i + 1; j < instanceMethods.size(); j++) {
                MethodDeclaration m1 = instanceMethods.get(i);
                MethodDeclaration m2 = instanceMethods.get(j);
                
                Set<IVariableBinding> fields1 = methodFieldAccess.get(m1);
                Set<IVariableBinding> fields2 = methodFieldAccess.get(m2);
                
                if (fields1 == null) fields1 = new HashSet<>();
                if (fields2 == null) fields2 = new HashSet<>();
                
                // Check if they share any fields
                boolean sharesFields = false;
                for (IVariableBinding field : fields1) {
                    if (fields2.contains(field)) {
                        sharesFields = true;
                        break;
                    }
                }
                
                if (sharesFields) {
                    Q++;
                } else {
                    P++;
                }
            }
        }
        
        // LCOM1 = P - Q if P > Q, else 0
        return P > Q ? (double) (P - Q) : 0.0;
    }

    @Override
    public MetricName getMetricName() {
        return MetricName.LCOM1;
    }

    @Override
    public boolean shouldComputeAggregate() {
        return true;
    }
}