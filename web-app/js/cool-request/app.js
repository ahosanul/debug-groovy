/**
 * Cool Request for Grails - Frontend Application
 * Main JavaScript file for the developer UI
 */

// Global state
var appState = {
    metadata: null,
    currentEndpoint: null,
    currentController: null,
    history: [],
    savedRequests: []
};

// API base path (relative to the application)
var API_BASE = window.location.pathname.replace(/\/$/, '') + '/api';

/**
 * Initialize the application on page load
 */
$(document).ready(function() {
    console.log('Cool Request for Grails initialized');
    
    // Load application metadata
    loadMetadata();
    
    // Setup tab navigation
    setupTabs();
    
    // Setup keyboard shortcuts
    setupKeyboardShortcuts();
    
    // Auto-focus search
    $('#search-input').focus();
});

/**
 * Load application metadata from server
 */
function loadMetadata() {
    showLoading(true);
    
    $.ajax({
        url: API_BASE,
        method: 'GET',
        success: function(data) {
            appState.metadata = data;
            renderControllersTree(data);
            updateHeaderInfo(data);
            showLoading(false);
        },
        error: function(xhr) {
            console.error('Failed to load metadata:', xhr);
            showError('Failed to load application metadata');
            showLoading(false);
        }
    });
}

/**
 * Update header with application info
 */
function updateHeaderInfo(data) {
    if (data.applicationName) {
        $('#app-name').text(data.applicationName);
    } else {
        $('#app-name').text('Grails Application');
    }
    
    if (data.grailsVersion) {
        $('#grails-version').text('Grails ' + data.grailsVersion);
    }
    
    if (data.environment) {
        var envClass = data.environment.toLowerCase() === 'production' ? 'status-error' : 'status-success';
        $('#environment')
            .text(data.environment)
            .removeClass('status-success status-error status-warning')
            .addClass(envClass);
        
        $('#environment-select').val(data.environment.toLowerCase());
    }
    
    // Update stats
    var controllerCount = data.controllerCount || 0;
    var endpointCount = data.endpointCount || 0;
    $('#stats').text(controllerCount + ' controllers, ' + endpointCount + ' endpoints');
}

/**
 * Render the controllers tree view
 */
function renderControllersTree(data) {
    var html = '';
    
    if (!data.controllers || data.controllers.length === 0) {
        html = '<div class="empty-state">' +
               '<div class="empty-state-icon">📦</div>' +
               '<div class="empty-state-text">No controllers discovered</div>' +
               '</div>';
    } else {
        // Group by controller
        data.controllers.forEach(function(controller) {
            html += renderControllerNode(controller);
        });
    }
    
    $('#controllers-tree').html(html);
}

/**
 * Render a single controller node with its actions/endpoints
 */
function renderControllerNode(controller) {
    var html = '<div class="tree-item" data-controller="' + escapeHtml(controller.name) + '">' +
               '<div class="tree-item-header" onclick="toggleController(this)">' +
               '<span class="tree-arrow">▶</span>' +
               '<span class="tree-icon">📁</span>' +
               '<span class="tree-label">' + escapeHtml(controller.name) + 'Controller</span>' +
               '</div>' +
               '<div class="tree-children">';
    
    // Add actions as children
    if (controller.actions && controller.actions.length > 0) {
        controller.actions.forEach(function(action) {
            // If action has endpoints, render each endpoint
            if (action.endpoints && action.endpoints.length > 0) {
                action.endpoints.forEach(function(endpoint) {
                    var method = endpoint.httpMethod || 'GET';
                    html += '<div class="endpoint-item" onclick="selectEndpoint(\'' + 
                            escapeHtml(controller.name) + '\', \'' + 
                            escapeHtml(action.name) + '\', \'' + 
                            escapeHtml(endpoint.path) + '\')">' +
                            '<span class="endpoint-method method-' + method.toLowerCase() + '">' + method + '</span>' +
                            '<span>' + escapeHtml(endpoint.path) + '</span>' +
                            '</div>';
                });
            } else {
                // No endpoint mapped, show action name
                var method = (action.httpMethods && action.httpMethods.length > 0) ? 
                             action.httpMethods[0] : 'GET';
                html += '<div class="endpoint-item" onclick="selectEndpoint(\'' + 
                        escapeHtml(controller.name) + '\', \'' + 
                        escapeHtml(action.name) + '\', null)">' +
                        '<span class="endpoint-method method-' + method.toLowerCase() + '">' + method + '</span>' +
                        '<span>/' + escapeHtml(controller.name.toLowerCase()) + '/' + escapeHtml(action.name) + '</span>' +
                        '</div>';
            }
        });
    }
    
    html += '</div></div>';
    return html;
}

/**
 * Toggle controller expansion
 */
function toggleController(element) {
    var $header = $(element);
    var $children = $header.next('.tree-children');
    var $arrow = $header.find('.tree-arrow');
    
    $children.toggleClass('expanded');
    $arrow.toggleClass('expanded');
    
    // Highlight selected
    $('.tree-item-header').removeClass('selected');
    $header.addClass('selected');
}

/**
 * Select an endpoint for testing
 */
function selectEndpoint(controllerName, actionName, path) {
    appState.currentController = controllerName;
    appState.currentEndpoint = {
        controller: controllerName,
        action: actionName,
        path: path || '/' + controllerName.toLowerCase() + '/' + actionName
    };
    
    // Hide empty state, show form
    $('#no-selection').hide();
    $('#request-form').show();
    
    // Set URL and method
    var inferredMethod = inferHttpMethod(actionName);
    $('#http-method').val(inferredMethod);
    $('#url-input').val(appState.currentEndpoint.path);
    
    // Clear previous values
    clearParameters();
    clearBody();
    
    // Find endpoint metadata and populate parameters
    if (appState.metadata && appState.metadata.controllers) {
        var controller = appState.metadata.controllers.find(function(c) {
            return c.name === controllerName;
        });
        
        if (controller && controller.actions) {
            var action = controller.actions.find(function(a) {
                return a.name === actionName;
            });
            
            if (action && action.parameters && action.parameters.length > 0) {
                populateParameters(action.parameters);
            }
        }
    }
    
    // Show/hide body section based on method
    toggleBodySection(inferredMethod);
    
    // Scroll to request form
    $('html, body').scrollTop(0);
}

/**
 * Infer HTTP method from action name based on Grails conventions
 */
function inferHttpMethod(actionName) {
    if (!actionName) return 'GET';
    
    var lower = actionName.toLowerCase();
    switch(lower) {
        case 'index':
        case 'list':
        case 'show':
        case 'create':
        case 'edit':
            return 'GET';
        case 'save':
            return 'POST';
        case 'update':
            return 'PUT';
        case 'delete':
            return 'DELETE';
        default:
            return 'GET';
    }
}

/**
 * Populate parameter fields from metadata
 */
function populateParameters(parameters) {
    var pathParams = parameters.filter(function(p) { return p.isPathParameter; });
    var queryParams = parameters.filter(function(p) { return !p.isPathParameter; });
    
    // Add path parameters
    if (pathParams.length > 0) {
        $('#path-params-section').show();
        var tbody = $('#path-params-table tbody');
        tbody.empty();
        
        pathParams.forEach(function(param) {
            tbody.append(createParamRow(param.name, param.example || '', false));
        });
    }
    
    // Add query parameters
    if (queryParams.length > 0) {
        var tbody = $('#query-params-table tbody');
        tbody.empty();
        
        queryParams.forEach(function(param) {
            tbody.append(createParamRow(param.name, param.example || ''));
        });
    }
}

/**
 * Create a parameter table row
 */
function createParamRow(name, value, canRemove) {
    if (canRemove !== false) canRemove = true;
    
    var html = '<tr>' +
               '<td><input type="text" value="' + escapeHtml(name) + '" readonly/></td>' +
               '<td><input type="text" value="' + escapeHtml(value) + '"/></td>';
    
    if (canRemove) {
        html += '<td><button class="btn btn-secondary" onclick="removeParamRow(this)">×</button></td>';
    }
    
    html += '</tr>';
    return html;
}

/**
 * Clear all parameters
 */
function clearParameters() {
    $('#path-params-table tbody').empty();
    $('#query-params-table tbody').html(
        '<tr>' +
        '<td><input type="text" placeholder="name"/></td>' +
        '<td><input type="text" placeholder="value"/></td>' +
        '<td><button class="btn btn-secondary" onclick="removeParamRow(this)">×</button></td>' +
        '</tr>'
    );
    $('#path-params-section').hide();
}

/**
 * Clear request body
 */
function clearBody() {
    $('#json-body').val('');
    $('#body-section').hide();
}

/**
 * Toggle body section visibility based on HTTP method
 */
function toggleBodySection(method) {
    var methodsWithBody = ['POST', 'PUT', 'PATCH'];
    if (methodsWithBody.indexOf(method) >= 0) {
        $('#body-section').show();
    } else {
        $('#body-section').hide();
    }
}

/**
 * Add a parameter row to a table
 */
function addParamRow(tableId) {
    var tbody = $('#' + tableId + ' tbody');
    tbody.append(
        '<tr>' +
        '<td><input type="text" placeholder="name"/></td>' +
        '<td><input type="text" placeholder="value"/></td>' +
        '<td><button class="btn btn-secondary" onclick="removeParamRow(this)">×</button></td>' +
        '</tr>'
    );
}

/**
 * Remove a parameter row
 */
function removeParamRow(button) {
    $(button).closest('tr').remove();
}

/**
 * Send the HTTP request
 */
function sendRequest() {
    var method = $('#http-method').val();
    var url = $('#url-input').val();
    var directInvocation = $('#direct-invocation').is(':checked');
    
    if (!url) {
        alert('Please enter a URL');
        return;
    }
    
    // Collect parameters
    var pathParams = {};
    var queryParams = {};
    var headers = {};
    
    // Path params
    $('#path-params-table tbody tr').each(function() {
        var inputs = $(this).find('input');
        var name = $(inputs[0]).val().trim();
        var value = $(inputs[1]).val().trim();
        if (name && value) {
            pathParams[name] = value;
        }
    });
    
    // Query params
    $('#query-params-table tbody tr').each(function() {
        var inputs = $(this).find('input');
        var name = $(inputs[0]).val().trim();
        var value = $(inputs[1]).val().trim();
        if (name && value) {
            queryParams[name] = value;
        }
    });
    
    // Headers
    $('#headers-table tbody tr').each(function() {
        var inputs = $(this).find('input');
        var name = $(inputs[0]).val().trim();
        var value = $(inputs[1]).val().trim();
        if (name && value) {
            headers[name] = value;
        }
    });
    
    // Body
    var body = null;
    var contentType = headers['Content-Type'] || headers['content-type'];
    if ($('#body-section').is(':visible')) {
        body = $('#json-body').val();
        if (body && contentType === 'application/json') {
            try {
                JSON.parse(body);
            } catch (e) {
                alert('Invalid JSON in request body');
                return;
            }
        }
    }
    
    // Build request payload
    var payload = {
        method: method,
        url: url,
        path: url,
        pathParams: pathParams,
        queryParams: queryParams,
        headers: headers,
        body: body,
        contentType: contentType || 'application/json',
        useDirectInvocation: directInvocation,
        controllerName: appState.currentController,
        actionName: appState.currentEndpoint ? appState.currentEndpoint.action : null
    };
    
    // Show loading state
    $('#response-panel').show();
    $('#response-status').text('Sending...').removeClass('status-success status-error status-warning');
    $('#response-time').text('-');
    $('#response-size').text('-');
    $('#response-content').text('Loading...');
    
    // Execute request
    var endpoint = directInvocation ? API_BASE + '/action' : API_BASE + '/request';
    
    $.ajax({
        url: endpoint,
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(payload),
        success: function(response) {
            displayResponse(response);
        },
        error: function(xhr) {
            displayErrorResponse(xhr);
        }
    });
}

/**
 * Display successful response
 */
function displayResponse(response) {
    var status = response.status || response.httpStatus || 0;
    var statusText = response.statusText || getStatusText(status);
    var time = response.executionTimeMs || 0;
    var size = response.size || 0;
    var body = response.body || response.responseBody || '';
    
    // Update meta
    var statusClass = status >= 200 && status < 300 ? 'status-success' : 
                      status >= 400 ? 'status-error' : 'status-warning';
    
    $('#response-status')
        .text(status + ' ' + statusText)
        .removeClass('status-success status-error status-warning')
        .addClass(statusClass);
    
    $('#response-time').text(time + 'ms');
    $('#response-size').text(formatSize(size));
    
    // Format and display body
    var formattedBody = formatResponseBody(body, response.contentType);
    $('#response-content').text(formattedBody);
}

/**
 * Display error response
 */
function displayErrorResponse(xhr) {
    $('#response-status')
        .text(xhr.status + ' ' + xhr.statusText)
        .removeClass('status-success status-error status-warning')
        .addClass('status-error');
    
    $('#response-time').text('-');
    $('#response-size').text('-');
    
    var errorMsg = xhr.responseText || 'Request failed';
    try {
        var json = JSON.parse(xhr.responseText);
        errorMsg = JSON.stringify(json, null, 2);
    } catch (e) {
        // Keep as text
    }
    
    $('#response-content').text(errorMsg);
}

/**
 * Format response body based on content type
 */
function formatResponseBody(body, contentType) {
    if (!body) return '';
    
    if (typeof body === 'object') {
        return JSON.stringify(body, null, 2);
    }
    
    // Try to parse as JSON
    if (contentType && contentType.indexOf('application/json') >= 0) {
        try {
            return JSON.stringify(JSON.parse(body), null, 2);
        } catch (e) {
            return body;
        }
    }
    
    // Try to parse as XML
    if (contentType && (contentType.indexOf('application/xml') >= 0 || contentType.indexOf('text/xml') >= 0)) {
        try {
            var xml = $.parseXML(body);
            return new XMLSerializer().serializeToString(xml);
        } catch (e) {
            return body;
        }
    }
    
    return body;
}

/**
 * Get HTTP status text
 */
function getStatusText(status) {
    var statusTexts = {
        200: 'OK',
        201: 'Created',
        204: 'No Content',
        301: 'Moved Permanently',
        302: 'Found',
        304: 'Not Modified',
        400: 'Bad Request',
        401: 'Unauthorized',
        403: 'Forbidden',
        404: 'Not Found',
        500: 'Internal Server Error',
        502: 'Bad Gateway',
        503: 'Service Unavailable'
    };
    return statusTexts[status] || '';
}

/**
 * Format byte size to human readable
 */
function formatSize(bytes) {
    if (bytes === 0) return '0 B';
    var k = 1024;
    var sizes = ['B', 'KB', 'MB', 'GB'];
    var i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
}

/**
 * Refresh metadata
 */
function refreshMetadata() {
    showLoading(true);
    
    $.ajax({
        url: API_BASE + '/refresh',
        method: 'POST',
        success: function(data) {
            appState.metadata = data;
            renderControllersTree(data);
            updateHeaderInfo(data);
            showLoading(false);
            alert('Metadata refreshed successfully!');
        },
        error: function(xhr) {
            console.error('Failed to refresh metadata:', xhr);
            showError('Failed to refresh metadata');
            showLoading(false);
        }
    });
}

/**
 * Export OpenAPI specification
 */
function exportOpenAPI() {
    window.open(API_BASE + '/openapi?format=json', '_blank');
}

/**
 * Copy request as cURL command
 */
function copyAsCurl() {
    var method = $('#http-method').val();
    var url = $('#url-input').val();
    
    // Build basic curl command
    var curl = 'curl -X ' + method + ' "' + url + '"';
    
    // Add headers
    $('#headers-table tbody tr').each(function() {
        var inputs = $(this).find('input');
        var name = $(inputs[0]).val().trim();
        var value = $(inputs[1]).val().trim();
        if (name && value) {
            curl += ' \\\n  -H "' + name + ': ' + value + '"';
        }
    });
    
    // Add body
    if ($('#body-section').is(':visible')) {
        var body = $('#json-body').val();
        if (body) {
            curl += ' \\\n  -d \'' + body.replace(/'/g, "'\"'\"'") + "'";
        }
    }
    
    // Copy to clipboard
    copyToClipboard(curl);
    alert('cURL command copied to clipboard!');
}

/**
 * Format JSON in body editor
 */
function formatJson() {
    var body = $('#json-body').val();
    if (!body) return;
    
    try {
        var json = JSON.parse(body);
        $('#json-body').val(JSON.stringify(json, null, 2));
    } catch (e) {
        alert('Invalid JSON: ' + e.message);
    }
}

/**
 * Save response to file
 */
function saveResponse() {
    var content = $('#response-content').text();
    if (!content) {
        alert('No response to save');
        return;
    }
    
    var blob = new Blob([content], {type: 'text/plain'});
    var url = URL.createObjectURL(blob);
    var a = document.createElement('a');
    a.href = url;
    a.download = 'response-' + Date.now() + '.txt';
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
}

/**
 * Search in the tree
 */
function searchTree() {
    var query = $('#search-input').val().toLowerCase();
    
    if (!query) {
        $('.tree-item, .endpoint-item').show();
        return;
    }
    
    $('.tree-item').each(function() {
        var $item = $(this);
        var text = $item.text().toLowerCase();
        var matches = text.indexOf(query) >= 0;
        
        $item.toggle(matches);
        
        // Expand parent if child matches
        if (matches) {
            $item.closest('.tree-children').addClass('expanded')
                 .prev('.tree-item-header').find('.tree-arrow').addClass('expanded');
        }
    });
}

/**
 * Setup tab navigation
 */
function setupTabs() {
    $('.tab').click(function() {
        var tabName = $(this).data('tab');
        
        $('.tab').removeClass('active');
        $(this).addClass('active');
        
        // TODO: Implement tab content switching
        if (tabName === 'history') {
            loadHistory();
        } else if (tabName === 'jobs') {
            loadJobs();
        }
    });
}

/**
 * Load request history
 */
function loadHistory() {
    $.ajax({
        url: API_BASE + '/history',
        method: 'GET',
        success: function(history) {
            appState.history = history;
            // TODO: Render history list
            console.log('History loaded:', history);
        },
        error: function(xhr) {
            console.error('Failed to load history:', xhr);
        }
    });
}

/**
 * Load jobs list
 */
function loadJobs() {
    $.ajax({
        url: API_BASE + '/jobs',
        method: 'GET',
        success: function(jobs) {
            // TODO: Render jobs list
            console.log('Jobs loaded:', jobs);
        },
        error: function(xhr) {
            console.error('Failed to load jobs:', xhr);
        }
    });
}

/**
 * Setup keyboard shortcuts
 */
function setupKeyboardShortcuts() {
    $(document).keydown(function(e) {
        // Ctrl/Cmd + Enter = Send request
        if ((e.ctrlKey || e.metaKey) && e.key === 'Enter') {
            if ($('#request-form').is(':visible')) {
                e.preventDefault();
                sendRequest();
            }
        }
        
        // Ctrl/Cmd + K = Focus search
        if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
            e.preventDefault();
            $('#search-input').focus();
        }
    });
}

/**
 * Show/hide loading indicator
 */
function showLoading(show) {
    if (show) {
        $('#controllers-tree').html(
            '<div class="empty-state">' +
            '<div class="spinner"></div>' +
            '<div class="empty-state-text" style="margin-top: 15px;">Loading...</div>' +
            '</div>'
        );
    }
}

/**
 * Show error message
 */
function showError(message) {
    $('#controllers-tree').html(
        '<div class="empty-state">' +
        '<div class="empty-state-icon">⚠️</div>' +
        '<div class="empty-state-text">' + escapeHtml(message) + '</div>' +
        '</div>'
    );
}

/**
 * Copy text to clipboard
 */
function copyToClipboard(text) {
    var textarea = document.createElement('textarea');
    textarea.value = text;
    document.body.appendChild(textarea);
    textarea.select();
    document.execCommand('copy');
    document.body.removeChild(textarea);
}

/**
 * Escape HTML special characters
 */
function escapeHtml(text) {
    if (!text) return '';
    var div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}
