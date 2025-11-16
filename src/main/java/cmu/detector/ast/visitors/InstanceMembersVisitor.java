package cmu.detector.ast.visitors;

import org.eclipse.jdt.core.dom.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InstanceMembersVisitor extends ASTVisitor {
    
    private Set<IVariableBinding> instanceFields;
    private List<MethodDeclaration> instanceMethods;
    private boolean visitingTypeDeclaration;
    
    public InstanceMembersVisitor() {
        this.instanceFields = new HashSet<>();
        this.instanceMethods = new ArrayList<>();
        this.visitingTypeDeclaration = false;
    }
    
    @Override
    public boolean visit(TypeDeclaration node) {
        if (!visitingTypeDeclaration) {
            visitingTypeDeclaration = true;
            return true;
        }
        // Don't visit inner classes
        return false;
    }
    
    @Override
    public boolean visit(AnonymousClassDeclaration node) {
        // Don't visit anonymous classes
        return false;
    }
    
    @Override
    public boolean visit(FieldDeclaration node) {
        if (!Modifier.isStatic(node.getModifiers())) {
            for (Object fragment : node.fragments()) {
                VariableDeclarationFragment vdf = (VariableDeclarationFragment) fragment;
                IVariableBinding binding = vdf.resolveBinding();
                if (binding != null) {
                    instanceFields.add(binding);
                }
            }
        }
        return false;
    }
    
    @Override
    public boolean visit(MethodDeclaration node) {
        if (!Modifier.isStatic(node.getModifiers())) {
            instanceMethods.add(node);
        }
        return false;
    }
    
    public Set<IVariableBinding> getInstanceFields() {
        return instanceFields;
    }
    
    public List<MethodDeclaration> getInstanceMethods() {
        return instanceMethods;
    }
}