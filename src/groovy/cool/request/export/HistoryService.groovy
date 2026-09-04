package cool.request

import cool.request.model.*
import groovy.json.JsonSlurper

/**
 * Manages request history and saved requests
 */
class HistoryService {
    
    static transactional = false
    
    // In-memory storage (in production, this could be file-based or database)
    private List<Map<String, Object>> history = []
    private Map<String, List<Map<String, Object>>> savedRequests = [:]
    private Set<String> favorites = [] as Set
    
    /**
     * Save a request execution to history
     */
    void saveToHistory(RequestContext context, RequestResult result) {
        def entry = [
            id: UUID.randomUUID().toString(),
            timestamp: new Date(),
            method: context.method,
            url: context.url ?: context.path,
            path: context.path,
            queryParams: context.queryParams,
            headers: filterSensitiveHeaders(context.headers),
            body: context.body,
            status: result.status,
            statusText: result.statusText,
            executionTimeMs: result.executionTimeMs,
            contentType: result.contentType,
            success: result.success
        ]
        
        history.add(0, entry) // Add to beginning
        
        // Trim history if too large
        def maxHistory = 100 // Could be configured
        if (history.size() > maxHistory) {
            history = history[0..<maxHistory]
        }
    }
    
    /**
     * Get recent history
     */
    List<Map<String, Object>> getHistory(int limit = 50) {
        return history.take(limit)
    }
    
    /**
     * Clear history
     */
    void clearHistory() {
        history = []
    }
    
    /**
     * Delete a specific history entry
     */
    void deleteHistory(String id) {
        history.removeAll { it.id == id }
    }
    
    /**
     * Save a request to a collection
     */
    void saveRequest(RequestContext context, String name, String collection = 'Default') {
        if (!savedRequests.containsKey(collection)) {
            savedRequests[collection] = []
        }
        
        def request = [
            id: UUID.randomUUID().toString(),
            name: name ?: "${context.method} ${context.path}",
            collection: collection,
            method: context.method,
            url: context.url,
            path: context.path,
            pathParams: context.pathParams,
            queryParams: context.queryParams,
            headers: context.headers,
            cookies: context.cookies,
            body: context.body,
            contentType: context.contentType,
            preRequestScript: context.preRequestScript,
            postRequestScript: context.postRequestScript,
            createdAt: new Date()
        ]
        
        savedRequests[collection] << request
    }
    
    /**
     * Get saved requests by collection
     */
    List<Map<String, Object>> getSavedRequests(String collection = null) {
        if (collection) {
            return savedRequests[collection] ?: []
        }
        
        // Return all collections
        return savedRequests.collectMany { coll, requests ->
            requests.collect { it + [collection: coll] }
        }
    }
    
    /**
     * Get collections
     */
    List<String> getCollections() {
        return savedRequests.keySet() as List
    }
    
    /**
     * Delete a saved request
     */
    void deleteSavedRequest(String id) {
        savedRequests.each { coll, requests ->
            savedRequests[coll] = requests.findAll { it.id != id }
        }
    }
    
    /**
     * Toggle favorite status for an endpoint
     */
    void toggleFavorite(String endpointKey) {
        if (favorites.contains(endpointKey)) {
            favorites.remove(endpointKey)
        } else {
            favorites << endpointKey
        }
    }
    
    /**
     * Get favorites
     */
    Set<String> getFavorites() {
        return favorites
    }
    
    /**
     * Filter sensitive headers for storage
     */
    private Map<String, String> filterSensitiveHeaders(Map<String, String> headers) {
        if (!headers) return [:]
        
        def sensitiveKeys = ['authorization', 'cookie', 'x-api-key', 'x-auth-token']
        
        return headers.findAll { key, value ->
            !sensitiveKeys.contains(key.toLowerCase())
        }
    }
    
    /**
     * Export history to JSON
     */
    String exportToJson() {
        return grails.converters.JSON.render([
            history: history,
            savedRequests: savedRequests,
            favorites: favorites
        ]).toString()
    }
    
    /**
     * Import history from JSON
     */
    void importFromJson(String json) {
        try {
            def data = new JsonSlurper().parseText(json)
            
            if (data.history instanceof List) {
                history = data.history
            }
            
            if (data.savedRequests instanceof Map) {
                savedRequests = data.savedRequests
            }
            
            if (data.favorites instanceof Collection) {
                favorites = data.favorites as Set
            }
            
        } catch (Exception e) {
            log.error "Error importing history", e
        }
    }
}
