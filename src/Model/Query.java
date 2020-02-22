package Model;

public class Query {
    private String queryId;
    private String title;
    private String description;
    private String narrative;

    public Query(String queryId, String title, String description, String narrative) {
        this.queryId = queryId;
        this.title = title;
        this.description = description;
        this.narrative = narrative;
    }

    public String getQueryId() {
        return queryId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getNarrative() {
        return narrative;
    }

}
