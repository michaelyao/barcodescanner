package me.dm7.barcodescanner.zxing.sample;

public class BookInfo {

    public String bookId;
    public String codeType;

    public String title;

    public BookInfo(String bookId, String codeType){
        this.bookId = bookId;
        this.codeType = codeType;
    }

    public String toString(){
        return "ID = [" + bookId + "] Type = [" + codeType + "] Title=[" + title + "]";
    }
}
