package cool.request.intellij.gutter;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.util.IconLoader;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PsiTreeUtil;
import cool.request.intellij.action.RunEndpointAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Provides gutter run icons for Grails controller methods in Groovy files
 */
public class GrailsControllerLineMarkerProvider implements LineMarkerProvider {
    
    private static final Icon RUN_ICON = IconLoader.getIcon("/icons/run.svg", GrailsControllerLineMarkerProvider.class);
    
    @Nullable
    @Override
    public LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element) {
        // Check if this is a method in a controller class
        if (!(element instanceof PsiMethod)) {
            return null;
        }
        
        PsiMethod method = (PsiMethod) element;
        
        // Check if the containing class is a controller
        if (!isGrailsController(method)) {
            return null;
        }
        
        // Skip private methods
        if (method.getModifierList().hasExplicitModifier("private")) {
            return null;
        }
        
        // Create line marker info
        return new LineMarkerInfo<>(
            element,
            element.getTextRange(),
            RUN_ICON,
            psi -> "Run Endpoint: " + method.getName(),
            (e, psi) -> {
                // Open tool window and select this endpoint
                RunEndpointAction.runEndpoint(method.getProject(), method);
            },
            GutterIconRenderer.Alignment.LEFT,
            () -> "Run Grails Endpoint"
        );
    }
    
    /**
     * Check if a method belongs to a Grails controller class
     */
    private boolean isGrailsController(PsiMethod method) {
        String className = method.getContainingClass() != null ? 
            method.getContainingClass().getName() : null;
        
        if (className == null) {
            return false;
        }
        
        // Grails controllers typically end with "Controller"
        return className.endsWith("Controller");
    }
}
