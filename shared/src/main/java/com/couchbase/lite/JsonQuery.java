package com.couchbase.lite;

import java.util.Map;

public class JsonQuery extends AbstractQuery {
    private Map<String, Object> jsonSchema;

    JsonQuery(Map<String, Object> jsonSchema) {
        this.jsonSchema = jsonSchema;
    }

    protected Map<String, Object> _asJSON() {
        return this.jsonSchema;
    }
}