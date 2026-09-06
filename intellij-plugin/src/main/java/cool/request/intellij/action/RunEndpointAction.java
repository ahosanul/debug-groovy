package cool.request.intellij.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.PsiMethod;
import cool.request.intellij.ui.EndpointDialog;
import org.jetbrains.annotations.NotNull;

/**
 * Action to run a Grails endpoint from the editor context menu or gutter icon
 */
public class RunEndpointAction extends AnAction {
    
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }
        
        // Try to get the method from the current context
        PsiMethod method = e.getData(CommonDataKeys.PSI_ELEMENT) instanceof PsiMethod ?
            (PsiMethod) e.getData(CommonDataKeys.PSI_ELEMENT) : null;
        
        if (method != null) {
            runEndpoint(project, method);
        } else {
            // Open the tool window without pre-selecting an endpoint
            openToolWindow(project);
        }
    }
    
    /**
     * Run a specific endpoint by opening the dialog
     */
    public static void runEndpoint(Project project, PsiMethod method) {
        // Open the tool window first
        openToolWindow(project);
        
        // Show parameter dialog
        EndpointDialog dialog = new EndpointDialog(project, method);
        dialog.show();
    }
    
    /**
     * Open the Cool Request tool window
     */
    private static void openToolWindow(Project project) {
        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
        ToolWindow toolWindow = toolWindowManager.getToolWindow("Cool Request");
        
        if (toolWindow != null) {
            toolWindow.show(null);
        }
    }
    
    @Override
    public void update(@NotNull AnActionEvent e) {
        // Enable the action only in Groovy or Java files
        PsiMethod method = e.getData(CommonDataKeys.PSI_ELEMENT) instanceof PsiMethod ?
            (PsiMethod) e.getData(CommonDataKeys.PSI_ELEMENT) : null;
        
        boolean enabled = method != null && isControllerMethod(method);
        e.getPresentation().setEnabledAndVisible(enabled);
        e.getPresentation().setText("Run Grails Endpoint");
    }
    
    private boolean isControllerMethod(PsiMethod method) {
        String className = method.getContainingClass() != null ? 
            method.getContainingClass().getName() : null;
        
        if (className == null) {
            return false;
        }
        
        return className.endsWith("Controller");
    }
}
