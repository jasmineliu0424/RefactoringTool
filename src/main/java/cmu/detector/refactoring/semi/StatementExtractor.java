//package cmu.detector.refactoring.semi;
//
//import org.eclipse.jdt.core.dom.*;
//import java.util.*;
//
//public class StatementExtractor extends ASTVisitor {
//
//    private final List<Statement> statements = new ArrayList<>();
//    private boolean inMethod = false;
//
//    @Override
//    public boolean visit(MethodDeclaration node) {
//        if (!inMethod) {
//            inMethod = true;
//            return true;
//        }
//        return false;
//    }
//
//    @Override
//    public boolean visit(Block node) {
//        // Traverse all statements in this block
//        for (Object obj : node.statements()) {
//            Statement stmt = (Statement) obj;
//            collect(stmt);
//        }
//        return false; // prevent default recursion
//    }
//
//    private void collect(Statement stmt) {
//        statements.add(stmt);
//        stmt.accept(new ASTVisitor() {
//            @Override
//            public boolean visit(Block nestedBlock) {
//                // visit children of nested block
//                for (Object obj : nestedBlock.statements()) {
//                    Statement s = (Statement) obj;
//                    collect(s);
//                }
//                return false;
//            }
//
//            @Override
//            public boolean visit(IfStatement ifStmt) {
//                // add THEN block statements
//                Statement thenStmt = ifStmt.getThenStatement();
//                if (thenStmt instanceof Block) {
//                    ((Block) thenStmt).accept(this);
//                } else if (thenStmt != null) {
//                    collect(thenStmt);
//                }
//
//                // add ELSE block statements
//                Statement elseStmt = ifStmt.getElseStatement();
//                if (elseStmt instanceof Block) {
//                    ((Block) elseStmt).accept(this);
//                } else if (elseStmt != null) {
//                    collect(elseStmt);
//                }
//                return false;
//            }
//
//            @Override
//            public boolean visit(ForStatement forStmt) {
//                Statement body = forStmt.getBody();
//                if (body instanceof Block) {
//                    ((Block) body).accept(this);
//                } else if (body != null) {
//                    collect(body);
//                }
//                return false;
//            }
//
//            @Override
//            public boolean visit(EnhancedForStatement forStmt) {
//                Statement body = forStmt.getBody();
//                if (body instanceof Block) {
//                    ((Block) body).accept(this);
//                } else if (body != null) {
//                    collect(body);
//                }
//                return false;
//            }
//
//            @Override
//            public boolean visit(WhileStatement wStmt) {
//                Statement body = wStmt.getBody();
//                if (body instanceof Block) {
//                    ((Block) body).accept(this);
//                } else if (body != null) {
//                    collect(body);
//                }
//                return false;
//            }
//
//            @Override
//            public boolean visit(DoStatement dStmt) {
//                Statement body = dStmt.getBody();
//                if (body instanceof Block) {
//                    ((Block) body).accept(this);
//                } else if (body != null) {
//                    collect(body);
//                }
//                return false;
//            }
//
//            @Override
//            public boolean visit(TryStatement tryStmt) {
//                // TRY { }
//                if (tryStmt.getBody() != null) {
//                    tryStmt.getBody().accept(this);
//                }
//
//                // CATCH blocks
//                for (Object obj : tryStmt.catchClauses()) {
//                    CatchClause cc = (CatchClause) obj;
//                    cc.getBody().accept(this);
//                }
//
//                // FINALLY { }
//                if (tryStmt.getFinally() != null) {
//                    tryStmt.getFinally().accept(this);
//                }
//
//                return false;
//            }
//        });
//    }
//
//    public List<Statement> getStatements() {
//        return statements;
//    }
//}

package cmu.detector.refactoring.semi;

import org.eclipse.jdt.core.dom.*;
import java.util.*;

/**
 * Extract every executable Statement inside a method body.
 * - Collects all Statement nodes except Block (so we get nested statements too).
 * - Sorts by source position to preserve original order.
 */
public class StatementExtractor extends ASTVisitor {
    private final List<Statement> statements = new ArrayList<>();

    @Override
    public void preVisit(ASTNode node) {
        // collect any Statement except Block (Block is just a container)
        if (node instanceof Statement && !(node instanceof Block)) {
            // cast safe because of instanceof
            statements.add((Statement) node);
        }
    }

    /**
     * Return statements sorted by start position (source order).
     */
    public List<Statement> getStatements() {
        // remove duplicates if the same node was added twice (shouldn't normally happen,
        // but defensive)
        Set<Integer> seenPos = new HashSet<>();
        List<Statement> unique = new ArrayList<>();
        for (Statement s : statements) {
            int pos = s.getStartPosition();
            if (!seenPos.contains(pos)) {
                seenPos.add(pos);
                unique.add(s);
            }
        }

        unique.sort(Comparator.comparingInt(ASTNode::getStartPosition));
        return Collections.unmodifiableList(unique);
    }
}

