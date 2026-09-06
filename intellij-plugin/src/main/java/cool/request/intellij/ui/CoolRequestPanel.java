package cool.request.intellij.ui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.treeStructure.Tree;
import cool.request.intellij.model.*;
import cool.request.intellij.service.CoolRequestService;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Main panel for the Cool Request tool window
 */
public class CoolRequestPanel extends JPanel {
    
    private final Project project;
    private final CoolRequestService service;
    private final Gson gson;
    
    private Tree controllerTree;
    private DefaultMutableTreeNode rootNode;
    private JBTextArea responseArea;
    private JLabel statusLabel;
    private JLabel timeLabel;
    private JTextField urlField;
    private JComboBox<String> methodCombo;
    private JBTextArea bodyArea;
    
    public CoolRequestPanel(Project project) {
        this.project = project;
        this.service = CoolRequestService.getInstance();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        
        setLayout(new BorderLayout());
        
        // Initialize UI components
        initComponents();
        
        // Load controllers asynchronously
        loadControllers();
    }
    
    private void initComponents() {
        // Create split pane for tree and editor
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplit.setResizeWeight(0.3);
        
        // Left panel - Controller tree
        JPanel leftPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel(" Controllers");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
        leftPanel.add(titleLabel, BorderLayout.NORTH);
        
        rootNode = new DefaultMutableTreeNode("Controllers");
        controllerTree = new Tree(rootNode);
        controllerTree.setRootVisible(true);
        controllerTree.setShowsRootHandles(true);
        
        // Custom cell renderer
        controllerTree.setCellRenderer(new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value,
                    boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
                
                if (value instanceof DefaultMutableTreeNode) {
                    Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
                    if (userObject instanceof ControllerMetadata) {
                        setIcon(UIManager.getIcon("FileView.directoryIcon"));
                    } else if (userObject instanceof ActionMetadata) {
                        setIcon(UIManager.getIcon("ClassView.methodIcon"));
                    } else if (userObject instanceof EndpointMetadata) {
                        setIcon(UIManager.getIcon("FileViews.fileIcon"));
                    }
                }
                
                return this;
            }
        });
        
        // Add selection listener
        controllerTree.addTreeSelectionListener(e -> {
            TreePath path = e.getPath();
            if (path != null) {
                Object node = path.getLastPathComponent();
                if (node instanceof DefaultMutableTreeNode) {
                    Object userObject = ((DefaultMutableTreeNode) node).getUserObject();
                    if (userObject instanceof EndpointMetadata) {
                        selectEndpoint((EndpointMetadata) userObject);
                    } else if (userObject instanceof ActionMetadata) {
                        // Find associated endpoint
                        ActionMetadata action = (ActionMetadata) userObject;
                        if (action.getEndpoints() != null && !action.getEndpoints().isEmpty()) {
                            selectEndpoint(action.getEndpoints().get(0));
                        }
                    }
                }
            }
        });
        
        leftPanel.add(new JBScrollPane(controllerTree), BorderLayout.CENTER);
        mainSplit.setLeftComponent(leftPanel);
        
        // Right panel - Request/Response editor
        JPanel rightPanel = new JPanel(new BorderLayout());
        
        // Request panel
        JPanel requestPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Method selector
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        requestPanel.add(new JLabel("Method:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1;
        methodCombo = new JComboBox<>(new String[]{"GET", "POST", "PUT", "DELETE", "PATCH"});
        requestPanel.add(methodCombo, gbc);
        
        // URL field
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        requestPanel.add(new JLabel("URL:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1;
        urlField = new JBTextField();
        requestPanel.add(urlField, gbc);
        
        // Send button
        gbc.gridx = 2;
        gbc.weightx = 0;
        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(l -> sendRequest());
        requestPanel.add(sendButton, gbc);
        
        // Body area
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        requestPanel.add(new JLabel("Request Body (JSON):"), gbc);
        
        gbc.gridy = 3;
        bodyArea = new JBTextArea(10, 50);
        requestPanel.add(new JBScrollPane(bodyArea), gbc);
        
        rightPanel.add(requestPanel, BorderLayout.NORTH);
        
        // Response panel
        JPanel responsePanel = new JPanel(new BorderLayout());
        responsePanel.setBorder(BorderFactory.createTitledBorder("Response"));
        
        // Status bar
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusLabel = new JLabel("Status: -");
        timeLabel = new JLabel("Time: - ms");
        statusPanel.add(statusLabel);
        statusPanel.add(Box.createHorizontalStrut(20));
        statusPanel.add(timeLabel);
        responsePanel.add(statusPanel, BorderLayout.NORTH);
        
        // Response area
        responseArea = new JBTextArea(20, 50);
        responseArea.setEditable(false);
        responseArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        responsePanel.add(new JBScrollPane(responseArea), BorderLayout.CENTER);
        
        rightPanel.add(responsePanel, BorderLayout.CENTER);
        
        mainSplit.setRightComponent(rightPanel);
        add(mainSplit, BorderLayout.CENTER);
    }
    
    private void loadControllers() {
        CompletableFuture<List<ControllerMetadata>> future = service.getControllers(project);
        future.thenAccept(controllers -> {
            SwingUtilities.invokeLater(() -> {
                rootNode.removeAllChildren();
                
                for (ControllerMetadata controller : controllers) {
                    DefaultMutableTreeNode controllerNode = 
                        new DefaultMutableTreeNode(controller);
                    
                    if (controller.getActions() != null) {
                        for (ActionMetadata action : controller.getActions()) {
                            DefaultMutableTreeNode actionNode = 
                                new DefaultMutableTreeNode(action);
                            
                            if (action.getEndpoints() != null) {
                                for (EndpointMetadata endpoint : action.getEndpoints()) {
                                    DefaultMutableTreeNode endpointNode = 
                                        new DefaultMutableTreeNode(endpoint);
                                    actionNode.add(endpointNode);
                                }
                            }
                            
                            controllerNode.add(actionNode);
                        }
                    }
                    
                    rootNode.add(controllerNode);
                }
                
                controllerTree.expandRow(0);
            });
        });
    }
    
    private void selectEndpoint(EndpointMetadata endpoint) {
        SwingUtilities.invokeLater(() -> {
            if (endpoint != null) {
                urlField.setText(endpoint.getPath());
                if (endpoint.getHttpMethod() != null) {
                    methodCombo.setSelectedItem(endpoint.getHttpMethod());
                }
                
                // Clear body for GET requests
                if ("GET".equalsIgnoreCase(endpoint.getHttpMethod())) {
                    bodyArea.setText("");
                }
            }
        });
    }
    
    private void sendRequest() {
        String method = (String) methodCombo.getSelectedItem();
        String url = urlField.getText();
        String body = bodyArea.getText();
        
        if (url.isEmpty()) {
            Messages.showErrorDialog(this, "Please enter a URL", "Invalid Request");
            return;
        }
        
        statusLabel.setText("Status: Sending...");
        timeLabel.setText("Time: - ms");
        responseArea.setText("");
        
        CompletableFuture<CoolRequestService.ExecutionResult> future = 
            service.executeRequest(project, method, url, body, null, null);
        
        future.thenAccept(result -> {
            SwingUtilities.invokeLater(() -> {
                if (result.getError() != null) {
                    statusLabel.setText("Status: Error");
                    responseArea.setText("Error: " + result.getError());
                } else {
                    statusLabel.setText("Status: " + result.getStatus() + " " + result.getStatusText());
                    timeLabel.setText("Time: " + result.getTime() + " ms");
                    
                    // Format response
                    String responseBody = result.getBody();
                    if (responseBody != null) {
                        try {
                            // Try to format as JSON
                            if (result.getContentType() != null && 
                                result.getContentType().contains("application/json")) {
                                Object json = gson.fromJson(responseBody, Object.class);
                                responseBody = gson.toJson(json);
                            }
                        } catch (Exception e) {
                            // Keep original format
                        }
                        responseArea.setText(responseBody);
                    }
                }
            });
        });
    }
}
