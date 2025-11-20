package cmu.detector.refactoring.semi;

import cmu.detector.resources.Method;
import cmu.detector.resources.Type;
import cmu.detector.util.TypeLoader;
import org.eclipse.jdt.core.dom.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.File;
import java.io.IOException;
import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SEMIExtractMethodIdentifierTest {

    @BeforeAll
    public void setUp() throws IOException {
        File dir = new File("src/test/java/cmu/detector/dummy/SEMI");
        TypeLoader.loadAllFromDir(dir);
    }

    @Test
    @DisplayName("Test SEMI can identify extract opportunities in Manifest.grabManifests method")
    public void testIdentifyExtractOpportunities() {
        // Arrange
        Type manifestType = TypeLoader.findTypeByName("Manifest");
        Assertions.assertNotNull(manifestType, "Manifest type should be loaded");
        
        Method grabManifestsMethod = manifestType.findMethodByName("grabManifests");
        Assertions.assertNotNull(grabManifestsMethod, "grabManifests method should exist");
        
        MethodDeclaration methodDecl = (MethodDeclaration) grabManifestsMethod.getNode();
        
        // Act
        SEMIExtractMethodIdentifier semi = new SEMIExtractMethodIdentifier(methodDecl);
        List<ExtractOpportunity> opportunities = semi.findExtractOpportunities();
        
        // Assert
        Assertions.assertNotNull(opportunities, "Opportunities list should not be null");
        Assertions.assertTrue(opportunities.size() > 0, "Should find at least one extract opportunity");
        
        System.out.println("Found " + opportunities.size() + " extract opportunities:");
        for (int i = 0; i < opportunities.size(); i++) {
            ExtractOpportunity opp = opportunities.get(i);
            System.out.println(String.format("  %d. Lines %d-%d (benefit: %.2f, statements: %d)", 
                i + 1, opp.getStartLine(), opp.getEndLine(), 
                opp.getBenefit(), opp.getStatementCount()));
        }
    }

    @Test
    @DisplayName("Test extract opportunity has valid parameters")
    public void testExtractOpportunityParameters() {
        // Arrange
        Type manifestType = TypeLoader.findTypeByName("Manifest");
        Method grabManifestsMethod = manifestType.findMethodByName("grabManifests");
        MethodDeclaration methodDecl = (MethodDeclaration) grabManifestsMethod.getNode();
        
        // Act
        SEMIExtractMethodIdentifier semi = new SEMIExtractMethodIdentifier(methodDecl);
        List<ExtractOpportunity> opportunities = semi.findExtractOpportunities();
        
        // Assert - Check first opportunity has parameters
        if (!opportunities.isEmpty()) {
            ExtractOpportunity firstOpp = opportunities.get(0);
            List<VariableInfo> parameters = firstOpp.getParameters();
            
            Assertions.assertNotNull(parameters, "Parameters should not be null");
            
            System.out.println("\nFirst opportunity parameters:");
            for (VariableInfo param : parameters) {
                System.out.println("  - " + param.getTypeName() + " " + param.getName());
            }
        }
    }

    @Test
    @DisplayName("Test extract opportunity has valid return type")
    public void testExtractOpportunityReturnType() {
        // Arrange
        Type manifestType = TypeLoader.findTypeByName("Manifest");
        Method grabManifestsMethod = manifestType.findMethodByName("grabManifests");
        MethodDeclaration methodDecl = (MethodDeclaration) grabManifestsMethod.getNode();
        
        // Act
        SEMIExtractMethodIdentifier semi = new SEMIExtractMethodIdentifier(methodDecl);
        List<ExtractOpportunity> opportunities = semi.findExtractOpportunities();
        
        // Assert
        if (!opportunities.isEmpty()) {
            ExtractOpportunity firstOpp = opportunities.get(0);
            ITypeBinding returnType = firstOpp.getReturnType();
            
            String returnTypeName = (returnType != null) ? returnType.getName() : "void";
            System.out.println("\nFirst opportunity return type: " + returnTypeName);
            
            // Return type should be either void or a valid type
            Assertions.assertTrue(
                returnType == null || returnType.getName() != null,
                "Return type should be valid"
            );
        }
    }

    @Test
    @DisplayName("Test opportunities are ranked by benefit")
    public void testOpportunitiesRanking() {
        // Arrange
        Type manifestType = TypeLoader.findTypeByName("Manifest");
        Method grabManifestsMethod = manifestType.findMethodByName("grabManifests");
        MethodDeclaration methodDecl = (MethodDeclaration) grabManifestsMethod.getNode();
        
        // Act
        SEMIExtractMethodIdentifier semi = new SEMIExtractMethodIdentifier(methodDecl);
        List<ExtractOpportunity> opportunities = semi.findExtractOpportunities();
        
        // Assert - Check that opportunities are sorted by benefit (descending)
        if (opportunities.size() > 1) {
            for (int i = 0; i < opportunities.size() - 1; i++) {
                double currentBenefit = opportunities.get(i).getBenefit();
                double nextBenefit = opportunities.get(i + 1).getBenefit();
                
                Assertions.assertTrue(
                    currentBenefit >= nextBenefit,
                    String.format("Opportunities should be sorted by benefit: %.2f >= %.2f", 
                        currentBenefit, nextBenefit)
                );
            }
        }
    }

    @Test
    @DisplayName("Test opportunity validation")
    public void testOpportunityValidation() {
        // Arrange
        Type manifestType = TypeLoader.findTypeByName("Manifest");
        Method grabManifestsMethod = manifestType.findMethodByName("grabManifests");
        MethodDeclaration methodDecl = (MethodDeclaration) grabManifestsMethod.getNode();
        
        // Act
        SEMIExtractMethodIdentifier semi = new SEMIExtractMethodIdentifier(methodDecl);
        List<ExtractOpportunity> opportunities = semi.findExtractOpportunities();
        
        // Assert - All returned opportunities should be valid
        for (ExtractOpportunity opp : opportunities) {
            Assertions.assertTrue(
                opp.isValid(),
                "All opportunities should pass validation"
            );
        }
    }
}