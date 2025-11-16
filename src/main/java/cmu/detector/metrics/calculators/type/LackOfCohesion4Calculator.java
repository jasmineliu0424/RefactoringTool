package cmu.detector.metrics.calculators.type;

import cmu.detector.ast.visitors.AllMembersVisitor;
import cmu.detector.ast.visitors.FieldAccessVisitor;
import cmu.detector.ast.visitors.MethodCallGraphVisitor;
import cmu.detector.metrics.MetricName;
import cmu.detector.metrics.calculators.MetricValueCalculator;
import org.eclipse.jdt.core.dom.*;

import java.util.*;

/**
 * Class to calculate the LCOM4 (Lack of Cohesion of Methods 4) metric.
 * <p>
 * LCOM4 is the lack of cohesion metric we adapted from <a href="https://www.aivosto.com/project/help/pm-oo-cohesion.html#LCOM4">Hitz & Montazeri</a>
 *
 * LCOM4 measures the number of "connected components" in a class. A connected component
 * is a set of related methods (and class-level variables). There should be only one
 * such component in each class. If there are 2 or more components, the class should be
 * split into so many smaller classes.
 *</p>
 * <p>Which methods are related? Methods A and B are related if:</p>
 * <ul>
 *   <li>they both access the same class-level variable, or</li>
 *   <li>A calls B or vice versa.</li>
 * </ul>
 *
 * <p>After determining the related methods, we draw a graph linking the related methods
 * to each other. LCOM4 equals the number of connected groups of methods.</p>
 *
 * <p>LCOM4 interpretation:</p>
 * <ul>
 *   <li>LCOM4=1 indicates a cohesive class, which is the "good" class.</li>
 *   <li>LCOM4>=2 indicates a problem. The class should be split into so many smaller classes.</li>
 *   <li>LCOM4=0 happens when there are no methods in a class. This is also a "bad" class.</li>
 * </ul>
 *
 * <strong>Observation:</strong> We DO NOT ignore static variables and methods.
 *
 * @author Leonardo Sousa
 */
public class LackOfCohesion4Calculator extends MetricValueCalculator {

    @Override
    protected Double computeValue(ASTNode target) {
        if (!(target instanceof TypeDeclaration)) {
            return 0.0;
        }

        TypeDeclaration typeDecl = (TypeDeclaration) target;
        
        // Collect all members (including static)
        AllMembersVisitor membersVisitor = new AllMembersVisitor();
        typeDecl.accept(membersVisitor);
        
        Set<IVariableBinding> allFields = membersVisitor.getAllFields();
        List<MethodDeclaration> allMethods = membersVisitor.getAllMethods();
        
        if (allMethods.isEmpty()) {
            return 0.0;
        }
        
        // Collect field access for each method
        FieldAccessVisitor fieldAccessVisitor = new FieldAccessVisitor(allFields);
        typeDecl.accept(fieldAccessVisitor);
        Map<MethodDeclaration, Set<IVariableBinding>> methodFieldAccess = fieldAccessVisitor.getMethodFieldAccess();
        
        // Collect method calls
        MethodCallGraphVisitor callGraphVisitor = new MethodCallGraphVisitor();
        typeDecl.accept(callGraphVisitor);
        Map<MethodDeclaration, Set<SimpleName>> methodCalls = callGraphVisitor.getMethodCalls();
        
        // Build adjacency graph
        Map<MethodDeclaration, Set<MethodDeclaration>> adjacencyGraph = buildAdjacencyGraph(
            allMethods, methodFieldAccess, methodCalls
        );
        
        // Count connected components using DFS
        int components = countConnectedComponents(allMethods, adjacencyGraph);
        
        return (double) components;
    }

    private Map<MethodDeclaration, Set<MethodDeclaration>> buildAdjacencyGraph(
            List<MethodDeclaration> methods,
            Map<MethodDeclaration, Set<IVariableBinding>> methodFieldAccess,
            Map<MethodDeclaration, Set<SimpleName>> methodCalls) {
        
        Map<MethodDeclaration, Set<MethodDeclaration>> graph = new HashMap<>();
        
        // Initialize graph
        for (MethodDeclaration method : methods) {
            graph.put(method, new HashSet<>());
        }
        
        // Build field access map (field -> methods that access it)
        Map<IVariableBinding, Set<MethodDeclaration>> fieldAccessMap = new HashMap<>();
        for (MethodDeclaration method : methods) {
            Set<IVariableBinding> accessedFields = methodFieldAccess.get(method);
            if (accessedFields != null) {
                for (IVariableBinding field : accessedFields) {
                    fieldAccessMap.computeIfAbsent(field, k -> new HashSet<>()).add(method);
                }
            }
        }
        
        // Connect methods that access the same field
        for (Set<MethodDeclaration> methodsAccessingField : fieldAccessMap.values()) {
            List<MethodDeclaration> methodList = new ArrayList<>(methodsAccessingField);
            for (int i = 0; i < methodList.size(); i++) {
                for (int j = i + 1; j < methodList.size(); j++) {
                    MethodDeclaration m1 = methodList.get(i);
                    MethodDeclaration m2 = methodList.get(j);
                    graph.get(m1).add(m2);
                    graph.get(m2).add(m1);
                }
            }
        }
        
        // Connect methods that call each other
        for (MethodDeclaration caller : methods) {
            Set<SimpleName> calledNames = methodCalls.get(caller);
            if (calledNames != null) {
                for (MethodDeclaration callee : methods) {
                    if (caller != callee) {
                        String calleeName = callee.getName().getIdentifier();
                        for (SimpleName calledName : calledNames) {
                            if (calledName.getIdentifier().equals(calleeName)) {
                                graph.get(caller).add(callee);
                                graph.get(callee).add(caller);
                                break;
                            }
                        }
                    }
                }
            }
        }
        
        return graph;
    }

    private int countConnectedComponents(List<MethodDeclaration> methods, 
                                        Map<MethodDeclaration, Set<MethodDeclaration>> graph) {
        Set<MethodDeclaration> visited = new HashSet<>();
        int components = 0;
        
        for (MethodDeclaration method : methods) {
            if (!visited.contains(method)) {
                components++;
                dfs(method, graph, visited);
            }
        }
        
        return components;
    }

    private void dfs(MethodDeclaration method, Map<MethodDeclaration, Set<MethodDeclaration>> graph, 
                    Set<MethodDeclaration> visited) {
        visited.add(method);
        
        Set<MethodDeclaration> neighbors = graph.get(method);
        if (neighbors != null) {
            for (MethodDeclaration neighbor : neighbors) {
                if (!visited.contains(neighbor)) {
                    dfs(neighbor, graph, visited);
                }
            }
        }
    }

    @Override
    public MetricName getMetricName() {
        return MetricName.LCOM4;
    }

    @Override
    public boolean shouldComputeAggregate() {
        return true;
    }
}