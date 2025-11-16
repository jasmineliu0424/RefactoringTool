package cmu.detector.ast.visitors;

import org.eclipse.jdt.core.dom.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AllMembersVisitor extends ASTVisitor {
    
    private Set<IVariableBinding> allFields;
    private List<MethodDeclaration> allMethods;
    private boolean visitingTypeDeclaration;
    
    public AllMembersVisitor() {
        this.allFields = new HashSet<>();
        this.allMethods = new ArrayList<>();
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
        for (Object fragment : node.fragments()) {
            VariableDeclarationFragment vdf = (VariableDeclarationFragment) fragment;
            IVariableBinding binding = vdf.resolveBinding();
            if (binding != null) {
                allFields.add(binding);
            }
        }
        return false;
    }
    
    @Override
    public boolean visit(MethodDeclaration node) {
        allMethods.add(node);
        return false;
    }
    
    public Set<IVariableBinding> getAllFields() {
        return allFields;
    }
    
    public List<MethodDeclaration> getAllMethods() {
        return allMethods;
    }
}