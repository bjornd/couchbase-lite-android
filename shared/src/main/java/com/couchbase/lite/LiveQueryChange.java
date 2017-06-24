package com.couchbase.lite;

class LiveQueryChange {
    //---------------------------------------------
    // member variables
    //---------------------------------------------
    private LiveQuery query;
    private ResultSet rows;
    private Throwable error; // TODO: CouchbaseLiteException????

    //---------------------------------------------
    // constructors
    //---------------------------------------------

    /*package*/ LiveQueryChange(LiveQuery query, ResultSet rows, Throwable error) {
        this.query = query;
        this.rows = rows;
        this.error = error;
    }

    //---------------------------------------------
    // API - public methods
    //---------------------------------------------

    public LiveQuery getQuery() {
        return query;
    }

    public ResultSet getRows() {
        return rows;
    }

    public Throwable getError() {
        return error;
    }
}
