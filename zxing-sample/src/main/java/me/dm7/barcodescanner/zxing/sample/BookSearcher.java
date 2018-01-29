package me.dm7.barcodescanner.zxing.sample;



import java.util.HashMap;
import java.util.Map;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;

import android.os.AsyncTask;

//import com.amazon.associates.sample;

//import javax.xml.sax;
import java.io.StringReader;
import org.xml.sax.InputSource;

public class BookSearcher {

    /*
     * Your Access Key ID, as taken from the Your Account page.
     */
    private static final String ACCESS_KEY_ID = "AKIAICARUBUDQCSPKCMQ";

    /*
     * Your Secret Key corresponding to the above ID, as taken from the
     * Your Account page.
     */
    private static final String SECRET_KEY = "sL9Ycmo4JZ/Met2YXBE9DmZf5rVYDZhvHFx6jTUF";

    /*
     * Use the end-point according to the region you are interested in.
     */
    private static final String ENDPOINT = "webservices.amazon.com";


    private static final String restAPIUrl = "http://littlescanner.herokuapp.com";
    //private static final String restAPIUrl = "http://172.16.0.32:3000"; //debug local server.

    public static BookInfo Send(String sId, String sIdType) {

        /*
         * Set up the signed requests helper.
         */
        SignedRequestsHelper  helper;
        BookInfo book = new BookInfo(sId, sIdType);

        try {
            helper = SignedRequestsHelper.getInstance(ENDPOINT, ACCESS_KEY_ID, SECRET_KEY);
        } catch (Exception e) {
            e.printStackTrace();
            book.errorMessage = "error when call signed request";

            return book;
        }

        String requestUrl = null;

        Map<String, String> params = new HashMap<String, String>();

        if(sIdType.compareToIgnoreCase("EAN_13") == 0 || sIdType.compareToIgnoreCase("EAN_8") == 0) {
            sIdType = "EAN";
        }
        if(sIdType.compareToIgnoreCase("UPC_A") == 0) {
            sIdType = "UPC";
        }
        params.put("Service", "AWSECommerceService");
        params.put("Operation", "ItemLookup");
        params.put("AWSAccessKeyId", "AKIAICARUBUDQCSPKCMQ");
        params.put("AssociateTag", "cpmentoring-20");
        //params.put("ItemId", "1888983302");
        //params.put("IdType", "ISBN");
        params.put("ItemId", sId);
        params.put("IdType", sIdType);
        params.put("ResponseGroup", "Images,ItemAttributes,Offers");

        params.put("SearchIndex", "Books");
        requestUrl = helper.sign(params);

        System.out.println("Signed URL: \"" + requestUrl + "\"");


        String resp = null;
        try {
            resp = sendGet(requestUrl);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Document doc = GetDocFromString(resp);
        NodeList nodes = doc.getElementsByTagName("Title");
        if(nodes == null || nodes.getLength()== 0){
            nodes = doc.getElementsByTagName("Message");
        }
        if(nodes == null || nodes.getLength()== 0){
            String errormsg = "Error happens in retrieving book info";
            System.out.println(errormsg);
            book.errorMessage = errormsg;
            return book;
        }
        Element line = (Element) nodes.item(0);
        //System.out.println(": " + line.getFirstChild().getTextContent());


        book.title = line.getFirstChild().getTextContent();

        NodeList imageNodes = doc.getElementsByTagName("MediumImage");
        if(imageNodes != null && imageNodes.getLength() > 0){
            Element temp = (Element) imageNodes.item(0);
            NodeList urlNodes = temp.getElementsByTagName("URL");
            if(urlNodes != null && urlNodes.getLength()>0){
                Element urlEl = (Element) urlNodes.item(0);
                book.imageUrl = urlEl.getFirstChild().getTextContent();
            }
        }


        book.pageUrl = getNodeText(doc, "DetailPageURL");
        book.ASIN = getNodeText(doc, "ASIN");
        book.author = getNodeText(doc, "author");
        book.binding = getNodeText(doc, "binding");

        String jsonPayload = book.toJSONString();
        System.out.println("payload is " + jsonPayload);
        sendPostRequest(restAPIUrl+"/api/book", jsonPayload);

        System.out.println(book);
        return book;
    };

    public static String sendPostRequest(String requestUrl, String payload) {
        StringBuffer jsonString = new StringBuffer();
        System.out.println(payload);
        System.out.println(requestUrl);
        try {
            URL url = new URL(requestUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
            writer.write(payload);
            writer.close();
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String line;
            while ((line = br.readLine()) != null) {
                jsonString.append(line);
            }
            br.close();
            connection.disconnect();
        } catch (Exception e) {
            System.out.println("We got err sending post " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
        return jsonString.toString();
    }

    public static String  getNodeText(Document doc, String tagName){
        NodeList nodes = doc.getElementsByTagName(tagName);
        if(nodes != null && nodes.getLength() > 0){
            Element temp = (Element) nodes.item(0);
            return temp.getFirstChild().getTextContent();
        }
        return "";
    }

    public  static String sendGet(String url) throws Exception {

        //String url = "http://www.google.com/search?q=mkyong";

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // optional default is GET
        con.setRequestMethod("GET");

        //add request header
        con.setRequestProperty("User-Agent", "Android Test");

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'GET' request to URL : " + url);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //print result
        System.out.println(response.toString());
        return response.toString();

    };

    public  static Document GetDocFromString(String xmlString){
        //String xmlString = "<?xml version=\"1.0\" encoding=\"utf-8\"?><a><b></b><c></c></a>";

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        Document document = null;
        try {
            builder = factory.newDocumentBuilder();
            document = builder.parse(new InputSource(new StringReader(xmlString)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return document;
    }



}
