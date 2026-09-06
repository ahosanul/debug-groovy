package cool.request.intellij.gutter;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.util.IconLoader;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import cool.request.intellij.action.RunEndpointAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Provides gutter run icons for Java controller methods
 */
public class JavaControllerLineMarkerProvider implements LineMarkerProvider {
    
    private static final Icon RUN_ICON = IconLoader.getIcon("/icons/run.svg", JavaControllerLineMarkerProvider.class);
    
    @Nullable
    @Override
    public LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element) {
        if (!(element instanceof PsiMethod)) {
            return null;
        }
        
        PsiMethod method = (PsiMethod) element;
        
        if (!isController(method)) {
            return null;
        }
        
        if (method.getModifierList().hasExplicitModifier("private")) {
            return null;
        }
        
        return new LineMarkerInfo<>(
            element,
            element.getTextRange(),
            RUN_ICON,
            psi -> "Run Endpoint: " + method.getName(),
            (e, psi) -> RunEndpointAction.runEndpoint(method.getProject(), method),
            GutterIconRenderer.Alignment.LEFT,
            () -> "Run Endpoint"
        );
    }
    
    private boolean isController(PsiMethod method) {
        String className = method.getContainingClass() != null ? 
            method.getContainingClass().getName() : null;
        
        if (className == null) {
            return false;
        }
        
        return className.endsWith("Controller") || 
               className.contains("Controller");
    }
}
