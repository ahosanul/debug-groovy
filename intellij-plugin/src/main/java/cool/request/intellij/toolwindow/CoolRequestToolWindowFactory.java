package cool.request.intellij.toolwindow;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import cool.request.intellij.ui.CoolRequestPanel;
import org.jetbrains.annotations.NotNull;

/**
 * Factory for creating the Cool Request tool window
 */
public class CoolRequestToolWindowFactory implements ToolWindowFactory {
    
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        SimpleToolWindowPanel panel = new SimpleToolWindowPanel(true);
        
        // Create the main panel with controller tree and request editor
        CoolRequestPanel coolRequestPanel = new CoolRequestPanel(project);
        panel.setContent(coolRequestPanel);
        
        // Add content to tool window
        ContentFactory contentFactory = ContentFactory.getInstance();
        Content content = contentFactory.createContent(panel, "", false);
        toolWindow.getContentManager().addContent(content);
    }
}
