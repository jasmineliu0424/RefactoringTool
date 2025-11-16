package cmu.detector.ast.visitors;

import org.eclipse.jdt.core.dom.*;
import java.util.*;

public class CallTreeFieldAccessVisitor {
    
    private Set<IVariableBinding> instanceFields;
    private List<MethodDeclaration> methods;
    private Map<MethodDeclaration, Set<IVariableBinding>> directFieldAccess;
    private Map<MethodDeclaration, Set<SimpleName>> methodCalls;
    
    public CallTreeFieldAccessVisitor(Set<IVariableBinding> instanceFields, List<MethodDeclaration> methods) {
        this.instanceFields = instanceFields;
        this.methods = methods;
        this.directFieldAccess = new HashMap<>();
        this.methodCalls = new HashMap<>();
    }
    
    /**
     * Analyze the type declaration and build field access and call information
     */
    public void analyze(TypeDeclaration typeDecl) {
        // First pass: collect direct field access
        FieldAccessVisitor fieldVisitor = new FieldAccessVisitor(instanceFields);
        typeDecl.accept(fieldVisitor);
        this.directFieldAccess = fieldVisitor.getMethodFieldAccess();
        
        // Second pass: collect method calls
        MethodCallGraphVisitor callVisitor = new MethodCallGraphVisitor();
        typeDecl.accept(callVisitor);
        this.methodCalls = callVisitor.getMethodCalls();
    }
    
    /**
     * Get all fields accessed by a method including through its call tree
     */
    public Set<IVariableBinding> getFieldsAccessedByCallTree(MethodDeclaration method) {
        Set<MethodDeclaration> visited = new HashSet<>();
        return getFieldsRecursive(method, visited);
    }
    
    private Set<IVariableBinding> getFieldsRecursive(MethodDeclaration method, Set<MethodDeclaration> visited) {
        if (visited.contains(method)) {
            return new HashSet<>();
        }
        visited.add(method);
        
        Set<IVariableBinding> allFields = new HashSet<>();
        
        // Add directly accessed fields
        Set<IVariableBinding> directFields = directFieldAccess.get(method);
        if (directFields != null) {
            allFields.addAll(directFields);
        }
        
        // Add fields accessed through method calls
        Set<SimpleName> calledNames = methodCalls.get(method);
        if (calledNames != null) {
            for (SimpleName calledName : calledNames) {
                for (MethodDeclaration calledMethod : methods) {
                    if (calledMethod.getName().getIdentifier().equals(calledName.getIdentifier())) {
                        Set<IVariableBinding> calledFields = getFieldsRecursive(calledMethod, visited);
                        allFields.addAll(calledFields);
                    }
                }
            }
        }
        
        return allFields;
    }
    
    /**
     * Get mapping of all methods to fields they access (including call tree)
     */
    public Map<MethodDeclaration, Set<IVariableBinding>> getMethodFieldAccessWithCallTree() {
        Map<MethodDeclaration, Set<IVariableBinding>> result = new HashMap<>();
        for (MethodDeclaration method : methods) {
            result.put(method, getFieldsAccessedByCallTree(method));
        }
        return result;
    }
}