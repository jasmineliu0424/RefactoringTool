package cmu.detector.metrics.calculators.type;

import cmu.detector.ast.visitors.CallTreeFieldAccessVisitor;
import cmu.detector.ast.visitors.VisibleInstanceMethodsVisitor;
import cmu.detector.metrics.MetricName;
import cmu.detector.metrics.calculators.MetricValueCalculator;
import org.eclipse.jdt.core.dom.*;

import java.util.*;

/**
 * This class computes the Tight Class Cohesion (TCC) for a given class.
 * The Tight Class Cohesion (TCC) measures the ratio between the actual number of visible directly connected methods (we exclude private and static methods)
 * in a class, NDC(C), divided by the number of maximal possible connections between the methods of
 * a class, NP(C).
 * <p>
 * Two visible methods are directly connected if they are accessing the same instance variables of the class or the call trees starting at A and B access the same variable.
 * </p.
 * <p>
 * TCC is defined as:
 * </p>
 * <ul>
 * <li>NP = maximum number of possible connections</li>
 * <li>NP = N * (N − 1) / 2 where N is the number of methods</li>
 * <li>NDC = number of direct connections (number of edges in the connection graph)</li>
 * <li>Tight class cohesion TCC = NDC / NP</li>
 * </ul>
 * <p>
 * TCC is in the range 0..1. The higher the TCC, the more cohesive the class is.
 * </p>
 * <p>
 * According to the authors, TCC &lt; 0.5 is considered non-cohesive.
 * TCC = 1 indicates a maximally cohesive class where all methods are connected.
 * </p>
 * <p>
 * To calculate the Tight Class Cohesion (TCC) for a given class, we need to follow these steps:
 * </p>
 * <ol>
 * <li>Identify the methods and fields in the class.</li>
 * <li>Determine the number of direct connections (NDC) between methods through shared instance variables.</li>
 * <li>Calculate the maximum number of possible connections (NP) between methods.</li>
 * <li>Compute the TCC value using the formula: TCC = NDC / NP.</li>
 * </ol>
 * <p>
 * Adapted from <a href="https://www.aivosto.com/project/help/pm-oo-cohesion.html#TCC_LCC">Aivosto</a>
 * </p>
 *
 * @author Leonardo Sousa
 */
public class TCCMetricValueCalculator extends MetricValueCalculator {

    @Override
    protected Double computeValue(ASTNode target) {
        if (!(target instanceof TypeDeclaration)) {
            return 0.0;
        }

        TypeDeclaration typeDecl = (TypeDeclaration) target;
        
        // Collect visible instance methods (non-static, non-private)
        VisibleInstanceMethodsVisitor visibleMethodsVisitor = new VisibleInstanceMethodsVisitor();
        typeDecl.accept(visibleMethodsVisitor);
        List<MethodDeclaration> visibleMethods = visibleMethodsVisitor.getVisibleMethods();
        
        int N = visibleMethods.size();
        
        // If less than 2 methods, TCC is 0
        if (N < 2) {
            return 0.0;
        }
        
        // Calculate NP = N * (N - 1) / 2
        int NP = N * (N - 1) / 2;
        
        // Get instance fields
        Set<IVariableBinding> instanceFields = getInstanceFields(typeDecl);
        
        // Analyze field access with call tree
        CallTreeFieldAccessVisitor callTreeVisitor = new CallTreeFieldAccessVisitor(instanceFields, visibleMethods);
        callTreeVisitor.analyze(typeDecl);
        Map<MethodDeclaration, Set<IVariableBinding>> methodFieldAccess = 
            callTreeVisitor.getMethodFieldAccessWithCallTree();
        
        // Count direct connections (NDC)
        int NDC = 0;
        for (int i = 0; i < visibleMethods.size(); i++) {
            for (int j = i + 1; j < visibleMethods.size(); j++) {
                MethodDeclaration m1 = visibleMethods.get(i);
                MethodDeclaration m2 = visibleMethods.get(j);
                
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
                    NDC++;
                }
            }
        }
        
        // TCC = NDC / NP
        return (double) NDC / NP;
    }

    private Set<IVariableBinding> getInstanceFields(TypeDeclaration typeDecl) {
        Set<IVariableBinding> fields = new HashSet<>();
        for (FieldDeclaration field : typeDecl.getFields()) {
            if (!Modifier.isStatic(field.getModifiers())) {
                for (Object fragment : field.fragments()) {
                    VariableDeclarationFragment vdf = (VariableDeclarationFragment) fragment;
                    IVariableBinding binding = vdf.resolveBinding();
                    if (binding != null) {
                        fields.add(binding);
                    }
                }
            }
        }
        return fields;
    }

    @Override
    public MetricName getMetricName() {
        return MetricName.TCC;
    }

    @Override
    public boolean shouldComputeAggregate() {
        return true;
    }
}