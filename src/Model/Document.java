package Model;

public class Document {

    private String docNo;
    private String publishDate;
    private String title;
    private String text;

    public Document() {
        docNo = "";
        publishDate = "";
        title = "";
        text = "";
    }

    public String getDocNo() {
        return docNo;
    }

    public void setDocNo(String docNo) {
        if(docNo.charAt(0) == ' '){
            docNo = docNo.substring(1);
        }
        if(docNo.charAt(docNo.length()-1) == ' '){
            docNo = docNo.substring(0, docNo.length()-1);
        }
        this.docNo = docNo;
    }

    public String getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(String publishDate) {
        this.publishDate = publishDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
