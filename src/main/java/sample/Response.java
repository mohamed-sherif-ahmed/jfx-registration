package sample;

public class Response {
    private String statusCode;
    private String msg;
    private body body;

    public sample.body getBody() {
        return body;
    }

    public void setBody(sample.body body) {
        this.body = body;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }



}
