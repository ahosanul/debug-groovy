<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Cool Request for Grails</title>
    
    <!-- Stylesheets -->
    <g:javascript library="jquery"/>
    <link rel="stylesheet" href="${resource(dir: 'css/cool-request', file: 'style.css')}"/>
    
    <style>
        /* Inline critical CSS for faster initial load */
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
            background-color: #1e1e1e;
            color: #d4d4d4;
            height: 100vh;
            overflow: hidden;
        }
        
        .app-container {
            display: flex;
            flex-direction: column;
            height: 100vh;
        }
        
        /* Header */
        .header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 10px 20px;
            background-color: #252526;
            border-bottom: 1px solid #3c3c3c;
        }
        
        .header h1 {
            font-size: 18px;
            font-weight: 500;
            color: #ffffff;
        }
        
        .header-info {
            display: flex;
            gap: 15px;
            font-size: 12px;
            color: #858585;
        }
        
        .header-actions {
            display: flex;
            gap: 10px;
        }
        
        .btn {
            padding: 6px 12px;
            border: none;
            border-radius: 3px;
            cursor: pointer;
            font-size: 12px;
            transition: background-color 0.2s;
        }
        
        .btn-primary {
            background-color: #0e639c;
            color: white;
        }
        
        .btn-primary:hover {
            background-color: #1177bb;
        }
        
        .btn-secondary {
            background-color: #3c3c3c;
            color: #d4d4d4;
        }
        
        .btn-secondary:hover {
            background-color: #4c4c4c;
        }
        
        .btn-success {
            background-color: #4caf50;
            color: white;
        }
        
        .btn-success:hover {
            background-color: #45a049;
        }
        
        /* Main content */
        .main-content {
            display: flex;
            flex: 1;
            overflow: hidden;
        }
        
        /* Sidebar */
        .sidebar {
            width: 300px;
            background-color: #252526;
            border-right: 1px solid #3c3c3c;
            display: flex;
            flex-direction: column;
            overflow: hidden;
        }
        
        .sidebar-header {
            padding: 10px 15px;
            border-bottom: 1px solid #3c3c3c;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        
        .sidebar-header h2 {
            font-size: 14px;
            font-weight: 500;
        }
        
        .search-box {
            padding: 10px 15px;
            border-bottom: 1px solid #3c3c3c;
        }
        
        .search-box input {
            width: 100%;
            padding: 8px 12px;
            border: 1px solid #3c3c3c;
            border-radius: 3px;
            background-color: #3c3c3c;
            color: #d4d4d4;
            font-size: 13px;
        }
        
        .search-box input:focus {
            outline: none;
            border-color: #0e639c;
        }
        
        .tree-view {
            flex: 1;
            overflow-y: auto;
            padding: 5px 0;
        }
        
        .tree-item {
            cursor: pointer;
            user-select: none;
        }
        
        .tree-item-header {
            display: flex;
            align-items: center;
            padding: 6px 15px;
            gap: 6px;
        }
        
        .tree-item-header:hover {
            background-color: #2a2d2e;
        }
        
        .tree-item-header.selected {
            background-color: #37373d;
        }
        
        .tree-arrow {
            width: 16px;
            height: 16px;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            font-size: 10px;
            color: #858585;
            transition: transform 0.2s;
        }
        
        .tree-arrow.expanded {
            transform: rotate(90deg);
        }
        
        .tree-icon {
            width: 16px;
            height: 16px;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            font-size: 14px;
        }
        
        .tree-label {
            flex: 1;
            font-size: 13px;
        }
        
        .tree-children {
            display: none;
            padding-left: 20px;
        }
        
        .tree-children.expanded {
            display: block;
        }
        
        .endpoint-item {
            display: flex;
            align-items: center;
            padding: 5px 15px 5px 37px;
            gap: 8px;
            font-size: 12px;
            cursor: pointer;
        }
        
        .endpoint-item:hover {
            background-color: #2a2d2e;
        }
        
        .endpoint-method {
            padding: 2px 6px;
            border-radius: 3px;
            font-size: 11px;
            font-weight: 600;
            min-width: 50px;
            text-align: center;
        }
        
        .method-get { background-color: #61affe; color: white; }
        .method-post { background-color: #49cc90; color: white; }
        .method-put { background-color: #fca130; color: white; }
        .method-delete { background-color: #f93e3e; color: white; }
        .method-patch { background-color: #50e3c2; color: white; }
        
        /* Content area */
        .content-area {
            flex: 1;
            display: flex;
            flex-direction: column;
            overflow: hidden;
        }
        
        /* Tabs */
        .tabs {
            display: flex;
            background-color: #252526;
            border-bottom: 1px solid #3c3c3c;
        }
        
        .tab {
            padding: 10px 20px;
            cursor: pointer;
            border-right: 1px solid #3c3c3c;
            font-size: 13px;
            color: #858585;
        }
        
        .tab:hover {
            background-color: #2a2d2e;
        }
        
        .tab.active {
            background-color: #1e1e1e;
            color: #d4d4d4;
            border-bottom: 2px solid #0e639c;
        }
        
        /* Request panel */
        .request-panel {
            flex: 1;
            display: flex;
            flex-direction: column;
            overflow: hidden;
            padding: 15px;
        }
        
        .panel-section {
            margin-bottom: 15px;
        }
        
        .panel-section-title {
            font-size: 12px;
            font-weight: 500;
            color: #858585;
            margin-bottom: 8px;
            text-transform: uppercase;
        }
        
        .url-bar {
            display: flex;
            gap: 10px;
            align-items: center;
        }
        
        .method-select {
            padding: 8px 12px;
            border: 1px solid #3c3c3c;
            border-radius: 3px;
            background-color: #3c3c3c;
            color: #d4d4d4;
            font-size: 13px;
            min-width: 100px;
        }
        
        .url-input {
            flex: 1;
            padding: 8px 12px;
            border: 1px solid #3c3c3c;
            border-radius: 3px;
            background-color: #3c3c3c;
            color: #d4d4d4;
            font-size: 13px;
            font-family: 'Consolas', 'Monaco', monospace;
        }
        
        .url-input:focus {
            outline: none;
            border-color: #0e639c;
        }
        
        /* Parameters table */
        .params-table {
            width: 100%;
            border-collapse: collapse;
            font-size: 12px;
        }
        
        .params-table th,
        .params-table td {
            padding: 8px;
            text-align: left;
            border-bottom: 1px solid #3c3c3c;
        }
        
        .params-table th {
            color: #858585;
            font-weight: 500;
        }
        
        .params-table input {
            width: 100%;
            padding: 6px 8px;
            border: 1px solid #3c3c3c;
            border-radius: 3px;
            background-color: #3c3c3c;
            color: #d4d4d4;
            font-size: 12px;
        }
        
        .params-table input:focus {
            outline: none;
            border-color: #0e639c;
        }
        
        /* JSON Editor */
        .json-editor {
            width: 100%;
            min-height: 150px;
            padding: 12px;
            border: 1px solid #3c3c3c;
            border-radius: 3px;
            background-color: #1e1e1e;
            color: #d4d4d4;
            font-family: 'Consolas', 'Monaco', monospace;
            font-size: 12px;
            resize: vertical;
        }
        
        .json-editor:focus {
            outline: none;
            border-color: #0e639c;
        }
        
        /* Response panel */
        .response-panel {
            height: 40%;
            background-color: #1e1e1e;
            border-top: 1px solid #3c3c3c;
            display: flex;
            flex-direction: column;
        }
        
        .response-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 10px 15px;
            border-bottom: 1px solid #3c3c3c;
        }
        
        .response-meta {
            display: flex;
            gap: 20px;
            font-size: 12px;
        }
        
        .response-meta-item {
            display: flex;
            gap: 5px;
        }
        
        .response-meta-label {
            color: #858585;
        }
        
        .response-meta-value {
            color: #d4d4d4;
            font-weight: 500;
        }
        
        .status-success { color: #4caf50; }
        .status-error { color: #f93e3e; }
        .status-warning { color: #ffc107; }
        
        .response-body {
            flex: 1;
            overflow: auto;
            padding: 15px;
            font-family: 'Consolas', 'Monaco', monospace;
            font-size: 12px;
            line-height: 1.5;
        }
        
        .response-body pre {
            white-space: pre-wrap;
            word-wrap: break-word;
        }
        
        /* Loading spinner */
        .spinner {
            display: inline-block;
            width: 16px;
            height: 16px;
            border: 2px solid #3c3c3c;
            border-top-color: #0e639c;
            border-radius: 50%;
            animation: spin 1s linear infinite;
        }
        
        @keyframes spin {
            to { transform: rotate(360deg); }
        }
        
        /* Scrollbar styling */
        ::-webkit-scrollbar {
            width: 10px;
            height: 10px;
        }
        
        ::-webkit-scrollbar-track {
            background: #1e1e1e;
        }
        
        ::-webkit-scrollbar-thumb {
            background: #424242;
            border-radius: 5px;
        }
        
        ::-webkit-scrollbar-thumb:hover {
            background: #4f4f4f;
        }
        
        /* Empty state */
        .empty-state {
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            height: 100%;
            color: #858585;
        }
        
        .empty-state-icon {
            font-size: 48px;
            margin-bottom: 20px;
        }
        
        .empty-state-text {
            font-size: 14px;
        }
        
        /* Environment selector */
        .env-selector {
            padding: 6px 10px;
            border: 1px solid #3c3c3c;
            border-radius: 3px;
            background-color: #3c3c3c;
            color: #d4d4d4;
            font-size: 12px;
        }
    </style>
</head>
<body>
    <div class="app-container">
        <!-- Header -->
        <div class="header">
            <h1>🚀 Cool Request for Grails</h1>
            
            <div class="header-info">
                <span id="app-name">Loading...</span>
                <span>|</span>
                <span id="grails-version">Grails 2.5.3</span>
                <span>|</span>
                <span id="environment" class="status-success">Development</span>
            </div>
            
            <div class="header-actions">
                <select id="environment-select" class="env-selector">
                    <option value="development">Development</option>
                    <option value="test">Test</option>
                    <option value="staging">Staging</option>
                    <option value="production">Production</option>
                </select>
                <button class="btn btn-secondary" onclick="refreshMetadata()">↻ Refresh</button>
                <button class="btn btn-secondary" onclick="exportOpenAPI()">📄 OpenAPI</button>
            </div>
        </div>
        
        <!-- Main Content -->
        <div class="main-content">
            <!-- Sidebar -->
            <div class="sidebar">
                <div class="sidebar-header">
                    <h2>Controllers & Endpoints</h2>
                    <span id="stats" style="font-size: 11px; color: #858585;">0 controllers, 0 endpoints</span>
                </div>
                
                <div class="search-box">
                    <input type="text" id="search-input" placeholder="Search controllers, actions, endpoints..." onkeyup="searchTree()"/>
                </div>
                
                <div class="tree-view" id="controllers-tree">
                    <div class="empty-state">
                        <div class="empty-state-icon">📡</div>
                        <div class="empty-state-text">Loading application metadata...</div>
                    </div>
                </div>
            </div>
            
            <!-- Content Area -->
            <div class="content-area">
                <!-- Tabs -->
                <div class="tabs">
                    <div class="tab active" data-tab="request">Request</div>
                    <div class="tab" data-tab="history">History</div>
                    <div class="tab" data-tab="saved">Saved</div>
                    <div class="tab" data-tab="jobs">Jobs</div>
                </div>
                
                <!-- Request Panel -->
                <div class="request-panel" id="request-panel">
                    <div class="empty-state" id="no-selection">
                        <div class="empty-state-icon">👈</div>
                        <div class="empty-state-text">Select an endpoint from the sidebar to begin</div>
                    </div>
                    
                    <div id="request-form" style="display: none;">
                        <!-- URL Bar -->
                        <div class="panel-section">
                            <div class="panel-section-title">Request URL</div>
                            <div class="url-bar">
                                <select id="http-method" class="method-select">
                                    <option value="GET">GET</option>
                                    <option value="POST">POST</option>
                                    <option value="PUT">PUT</option>
                                    <option value="DELETE">DELETE</option>
                                    <option value="PATCH">PATCH</option>
                                    <option value="HEAD">HEAD</option>
                                    <option value="OPTIONS">OPTIONS</option>
                                </select>
                                <input type="text" id="url-input" class="url-input" placeholder="Enter URL or select endpoint"/>
                            </div>
                        </div>
                        
                        <!-- Path Parameters -->
                        <div class="panel-section" id="path-params-section" style="display: none;">
                            <div class="panel-section-title">Path Parameters</div>
                            <table class="params-table" id="path-params-table">
                                <thead>
                                    <tr>
                                        <th style="width: 30%;">Name</th>
                                        <th>Value</th>
                                    </tr>
                                </thead>
                                <tbody></tbody>
                            </table>
                        </div>
                        
                        <!-- Query Parameters -->
                        <div class="panel-section">
                            <div class="panel-section-title">Query Parameters</div>
                            <table class="params-table" id="query-params-table">
                                <thead>
                                    <tr>
                                        <th style="width: 30%;">Name</th>
                                        <th>Value</th>
                                        <th style="width: 40px;"></th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <tr>
                                        <td><input type="text" placeholder="name"/></td>
                                        <td><input type="text" placeholder="value"/></td>
                                        <td><button class="btn btn-secondary" onclick="removeParamRow(this)">×</button></td>
                                    </tr>
                                </tbody>
                            </table>
                            <button class="btn btn-secondary" style="margin-top: 8px;" onclick="addParamRow('query-params-table')">+ Add Parameter</button>
                        </div>
                        
                        <!-- Headers -->
                        <div class="panel-section">
                            <div class="panel-section-title">Headers</div>
                            <table class="params-table" id="headers-table">
                                <thead>
                                    <tr>
                                        <th style="width: 30%;">Name</th>
                                        <th>Value</th>
                                        <th style="width: 40px;"></th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <tr>
                                        <td><input type="text" placeholder="Content-Type" value="Content-Type"/></td>
                                        <td><input type="text" placeholder="application/json" value="application/json"/></td>
                                        <td><button class="btn btn-secondary" onclick="removeParamRow(this)">×</button></td>
                                    </tr>
                                </tbody>
                            </table>
                            <button class="btn btn-secondary" style="margin-top: 8px;" onclick="addParamRow('headers-table')">+ Add Header</button>
                        </div>
                        
                        <!-- Request Body -->
                        <div class="panel-section" id="body-section" style="display: none;">
                            <div class="panel-section-title">Request Body (JSON)</div>
                            <textarea id="json-body" class="json-editor" placeholder='{"key": "value"}'></textarea>
                            <div style="margin-top: 8px;">
                                <button class="btn btn-secondary" onclick="formatJson()">Format JSON</button>
                                <button class="btn btn-secondary" onclick="copyAsCurl()">Copy as cURL</button>
                            </div>
                        </div>
                        
                        <!-- Send Button -->
                        <div class="panel-section" style="margin-top: 20px;">
                            <button class="btn btn-success" style="padding: 10px 30px; font-size: 14px;" onclick="sendRequest()">
                                ▶ Send Request
                            </button>
                            <label style="margin-left: 15px; font-size: 12px; color: #858585;">
                                <input type="checkbox" id="direct-invocation"/> Direct Invocation (bypass HTTP)
                            </label>
                        </div>
                    </div>
                </div>
                
                <!-- Response Panel -->
                <div class="response-panel" id="response-panel" style="display: none;">
                    <div class="response-header">
                        <div class="response-meta">
                            <div class="response-meta-item">
                                <span class="response-meta-label">Status:</span>
                                <span class="response-meta-value status-success" id="response-status">200 OK</span>
                            </div>
                            <div class="response-meta-item">
                                <span class="response-meta-label">Time:</span>
                                <span class="response-meta-value" id="response-time">0ms</span>
                            </div>
                            <div class="response-meta-item">
                                <span class="response-meta-label">Size:</span>
                                <span class="response-meta-value" id="response-size">0 KB</span>
                            </div>
                        </div>
                        <div>
                            <button class="btn btn-secondary" onclick="saveResponse()">💾 Save</button>
                        </div>
                    </div>
                    <div class="response-body">
                        <pre id="response-content">// Response will appear here</pre>
                    </div>
                </div>
            </div>
        </div>
    </div>
    
    <!-- JavaScript -->
    <script src="${resource(dir: 'js/cool-request', file: 'app.js')}"></script>
</body>
</html>
