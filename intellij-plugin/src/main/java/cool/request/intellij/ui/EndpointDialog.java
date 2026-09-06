package cool.request.intellij.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.ui.components.JBTextField;
import cool.request.intellij.service.CoolRequestService;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * Dialog for entering endpoint parameters and executing requests
 */
public class EndpointDialog extends DialogWrapper {
    
    private final Project project;
    private final PsiMethod method;
    private final CoolRequestService service;
    
    private JTextField urlField;
    private JComboBox<String> methodCombo;
    private JPanel paramsPanel;
    private JBTextArea bodyArea;
    private JBTextArea responseArea;
    private JLabel statusLabel;
    private JLabel timeLabel;
    
    public EndpointDialog(Project project, PsiMethod method) {
        super(project);
        this.project = project;
        this.method = method;
        this.service = CoolRequestService.getInstance();
        
        setTitle("Run Endpoint: " + method.getName());
        init();
    }
    
    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        
        // Request panel
        JPanel requestPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Method selector
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        requestPanel.add(new JLabel("HTTP Method:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1;
        methodCombo = new JComboBox<>(new String[]{"GET", "POST", "PUT", "DELETE", "PATCH"});
        requestPanel.add(methodCombo, gbc);
        
        // URL field - try to infer from method
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        requestPanel.add(new JLabel("URL:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1;
        urlField = new JBTextField();
        urlField.setText(inferUrl());
        requestPanel.add(urlField, gbc);
        
        // Parameters section
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        paramsPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        paramsPanel.setBorder(BorderFactory.createTitledBorder("Parameters"));
        
        // Add parameter fields based on method signature
        addParameterFields();
        
        JScrollPane paramsScroll = new JBScrollPane(paramsPanel);
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1;
        requestPanel.add(paramsScroll, gbc);
        
        // Body area
        gbc.gridy = 4;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        requestPanel.add(new JLabel("Request Body (JSON):"), gbc);
        
        gbc.gridy = 5;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        bodyArea = new JBTextArea(8, 40);
        requestPanel.add(new JBScrollPane(bodyArea), gbc);
        
        // Send button
        gbc.gridy = 6;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JButton sendButton = new JButton("Send Request");
        sendButton.addActionListener(l -> sendRequest());
        requestPanel.add(sendButton, gbc);
        
        mainPanel.add(requestPanel, BorderLayout.CENTER);
        
        // Response panel
        JPanel responsePanel = new JPanel(new BorderLayout());
        responsePanel.setBorder(BorderFactory.createTitledBorder("Response"));
        
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusLabel = new JLabel("Status: -");
        timeLabel = new JLabel("Time: - ms");
        statusPanel.add(statusLabel);
        statusPanel.add(Box.createHorizontalStrut(20));
        statusPanel.add(timeLabel);
        responsePanel.add(statusPanel, BorderLayout.NORTH);
        
        responseArea = new JBTextArea(15, 40);
        responseArea.setEditable(false);
        responseArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        responsePanel.add(new JBScrollPane(responseArea), BorderLayout.CENTER);
        
        mainPanel.add(responsePanel, BorderLayout.SOUTH);
        
        return mainPanel;
    }
    
    /**
     * Infer URL from controller and method name
     */
    private String inferUrl() {
        String className = method.getContainingClass().getName();
        String methodName = method.getName();
        
        // Remove "Controller" suffix and convert to lowercase
        String controllerName = className.replace("Controller", "").toLowerCase();
        
        // Default URL pattern
        return "/" + controllerName + "/" + methodName;
    }
    
    /**
     * Add parameter input fields based on method signature
     */
    private void addParameterFields() {
        paramsPanel.removeAll();
        
        PsiParameter[] parameters = method.getParameterList().getParameters();
        
        if (parameters.length == 0) {
            paramsPanel.add(new JLabel("No parameters"));
            paramsPanel.add(new JLabel(""));
        } else {
            for (PsiParameter param : parameters) {
                String paramName = param.getName();
                String paramType = param.getType().getPresentableText();
                
                paramsPanel.add(new JLabel(paramName + " (" + paramType + "):"));
                JBTextField paramField = new JBTextField();
                
                // Set default value based on type
                paramField.setText(getDefaultValue(paramType));
                
                paramsPanel.add(paramField);
            }
        }
        
        paramsPanel.revalidate();
        paramsPanel.repaint();
    }
    
    /**
     * Get default example value for a type
     */
    private String getDefaultValue(String type) {
        switch (type) {
            case "String":
                return "example";
            case "Long":
            case "long":
                return "1";
            case "Integer":
            case "int":
                return "1";
            case "Boolean":
            case "boolean":
                return "true";
            case "Double":
            case "double":
                return "1.0";
            default:
                return "";
        }
    }
    
    /**
     * Send the HTTP request
     */
    private void sendRequest() {
        String httpMethod = (String) methodCombo.getSelectedItem();
        String url = urlField.getText();
        String body = bodyArea.getText();
        
        if (url.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a URL");
            return;
        }
        
        statusLabel.setText("Status: Sending...");
        timeLabel.setText("Time: - ms");
        responseArea.setText("");
        
        service.executeRequest(project, httpMethod, url, body, null, null)
            .thenAccept(result -> {
                SwingUtilities.invokeLater(() -> {
                    if (result.getError() != null) {
                        statusLabel.setText("Status: Error");
                        responseArea.setText("Error: " + result.getError());
                    } else {
                        statusLabel.setText("Status: " + result.getStatus() + " " + result.getStatusText());
                        timeLabel.setText("Time: " + result.getTime() + " ms");
                        
                        String responseBody = result.getBody();
                        if (responseBody != null) {
                            responseArea.setText(responseBody);
                        }
                    }
                });
            });
    }
    
    @Override
    protected void doOKAction() {
        // Don't close on OK, let user send multiple requests
        sendRequest();
    }
    
    @Override
    public JComponent getPreferredFocusedComponent() {
        return urlField;
    }
}
