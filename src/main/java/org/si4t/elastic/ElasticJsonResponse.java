package org.si4t.elastic;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * ElasticJsonResponse.
 *
 * @author Marko Milic
 */

public final class ElasticJsonResponse {
    public final long took;
    public final boolean errors;
    public final Item items[];

    @JsonCreator
    public ElasticJsonResponse(@JsonProperty("took") long took, @JsonProperty("errors") boolean errors, @JsonProperty("items") Item[] items){
        this.took = took;
        this.errors = errors;
        this.items = items;
    }

    public static final class Item {
        public final Index index;
        public final Delete delete;

        @JsonCreator
        public Item(@JsonProperty("index") Index index, @JsonProperty("delete") Delete delete){
            this.index = index;
            this.delete = delete;
        }

        public static final class Delete {
            public final String _index;
            public final String _type;
            public final String _id;
            public final long _version;
            public final String result;
            public final _shards _shards;
            public final long _seq_no;
            public final long _primary_term;
            public final long status;
    
            @JsonCreator
            public Delete(@JsonProperty("_index") String _index, @JsonProperty("_type") String _type, @JsonProperty("_id") String _id, @JsonProperty("_version") long _version, @JsonProperty("result") String result, @JsonProperty("_shards") _shards _shards, @JsonProperty("_seq_no") long _seq_no, @JsonProperty("_primary_term") long _primary_term, @JsonProperty("status") long status){
                this._index = _index;
                this._type = _type;
                this._id = _id;
                this._version = _version;
                this.result = result;
                this._shards = _shards;
                this._seq_no = _seq_no;
                this._primary_term = _primary_term;
                this.status = status;
            }
        }
 
        
        public static final class Index {
            public final String _index;
            public final String _type;
            public final String _id;
            public final String _version;
            public final long status;
            public final Error error;
            public final String result;
            public final _shards _shards;
            public final long _seq_no;
            public final long _primary_term;
    
            @JsonCreator
            public Index(@JsonProperty("_index") String _index, @JsonProperty("result") String result, @JsonProperty("_shards") _shards _shards, @JsonProperty("_seq_no") long _seq_no, @JsonProperty("_primary_term") long _primary_term,@JsonProperty("_type") String _type, @JsonProperty("_id") String _id, @JsonProperty("_version") String _version, @JsonProperty("status") long status, @JsonProperty("error") Error error){
                this._index = _index;
                this._type = _type;
                this._id = _id;
                this.status = status;
                this.error = error;
                this._version = _version;
                this.result = result;
                this._shards = _shards;
                this._seq_no = _seq_no;
                this._primary_term = _primary_term;
            }
    
            public static final class Error {
                public final String type;
                public final String reason;
        
                @JsonCreator
                public Error(@JsonProperty("type") String type, @JsonProperty("reason") String reason){
                    this.type = type;
                    this.reason = reason;
                }
            }
        }
        
        public static final class _shards {
            public final long total;
            public final long successful;
            public final long failed;
    
            @JsonCreator
            public _shards(@JsonProperty("total") long total, @JsonProperty("successful") long successful, @JsonProperty("failed") long failed){
                this.total = total;
                this.successful = successful;
                this.failed = failed;
            }
        }
    }
}