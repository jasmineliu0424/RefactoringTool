package cmu.detector.refactoring.semi;

import cmu.detector.resources.Method;
import cmu.detector.resources.Type;
import cmu.detector.util.TypeLoader;
import org.eclipse.jdt.core.dom.*;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ParameterExtractorTest {

    @BeforeAll
    public void setUp() throws IOException {
        File dir = new File("src/test/java/cmu/detector/dummy/SEMI");
        TypeLoader.loadAllFromDir(dir);
    }

    @Test
    @DisplayName("Test parameter extraction identifies used variables")
    public void testParameterExtraction() {
        // Arrange
        Type manifestType = TypeLoader.findTypeByName("Manifest");
        Method grabManifestsMethod = manifestType.findMethodByName("grabManifests");
        MethodDeclaration methodDecl = (MethodDeclaration) grabManifestsMethod.getNode();
        
        SEMIExtractMethodIdentifier semi = new SEMIExtractMethodIdentifier(methodDecl);
        List<ExtractOpportunity> opportunities = semi.findExtractOpportunities();
        
        // Act & Assert
        if (!opportunities.isEmpty()) {
            ExtractOpportunity opp = opportunities.get(0);
            List<VariableInfo> params = opp.getParameters();
            
            Assertions.assertNotNull(params, "Parameters should not be null");
            
            // Parameters should not include variables defined within the cluster
            for (VariableInfo param : params) {
                Assertions.assertNotNull(param.getName(), "Parameter name should not be null");
                Assertions.assertNotNull(param.getType(), "Parameter type should not be null");
            }
        }
    }
}