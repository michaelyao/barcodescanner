package me.dm7.barcodescanner.zxing.sample;

import org.json.simple.JSONObject;

public class BookInfo {

    public String bookId;
    public String codeType;

    public String title;
    public String imageUrl;
    public String ASIN;
    public String pageUrl;
    public String author;
    public String binding;
    public String errorMessage;

    public BookInfo(String bookId, String codeType){
        this.bookId = bookId;
        this.codeType = codeType;
    }

    public String toString(){
        String szText = "ID = [" + bookId + "] Type = [" + codeType + "]";
        if(title != null && title.length() > 0 ){
            szText += " Title=[" + title + "]";
        }
        if(ASIN != null && ASIN.length() > 0 ){
            szText += " ASIN=[" + ASIN + "]";
        }
        if(author != null && author.length() > 0 ){
            szText += " Author=[" + title + "]";
        }
        if(errorMessage != null && errorMessage.length() > 0 ){
            szText += " error=[" + errorMessage + "]";
        }

        return szText ;
    }

    public String toJSONString(){

        JSONObject obj=new JSONObject();
        obj.put("ID",bookId);
        obj.put("codeType",codeType);

        if(title != null && title.length() > 0 ){
            obj.put("title",title);
        }
        if(ASIN != null && ASIN.length() > 0 ){
            obj.put("ASIN",ASIN);
        }
        if(author != null && author.length() > 0 ){
            obj.put("author", author);
        }
        if(imageUrl != null && imageUrl.length() > 0 ){
            obj.put("imageUrl",imageUrl);
        }
        if(pageUrl != null && pageUrl.length() > 0 ){
            obj.put("pageUrl",pageUrl);
        }
        if(errorMessage != null && errorMessage.length() > 0 ){
            obj.put("errorMessage",errorMessage);
        }
        String json = obj.toString();
        return json ;
    }
}
