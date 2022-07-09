package org.si4t.elastic;

import java.util.ArrayList;

/**
 * DispatcherAction.
 *
 * @author Marko Milic
 */
public class DocumentBatch {

    private ArrayList<DocumentData> items;

    public DocumentBatch()
    {
        items = new ArrayList<DocumentData>();
    }

    public ArrayList<DocumentData> getItems() {
        return items;
    }

    public void setItems(ArrayList<DocumentData> items) {
        this.items = items;
    }

}