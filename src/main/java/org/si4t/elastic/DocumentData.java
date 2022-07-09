package org.si4t.elastic;

import java.util.HashMap;
import java.util.Map;

/**
 * DispatcherAction.
 *
 * @author Marko Milic
 */
public class DocumentData {

    private String id;

    private Map<String, Object> fields;

    public DocumentData(String id)
    {
        this.id = id;
        this.fields = new HashMap<String,Object>();
    }

    public Map<String, Object> getFields() {
        return fields;
    }

    public void setFields(Map<String, Object> fields) {
        this.fields = fields;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
