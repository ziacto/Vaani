package com.vyasware.vaani;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by vyas on 4/25/15.
 */
public class XMLNewsParser implements Runnable {

    ArrayList<String> title;
    ArrayList<String> description;

    @Override
    public void run() {

        try {

            URL url = new URL(
                    "http://navbharattimes.indiatimes.com/rssfeedsdefault.cms");
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new InputSource(url.openStream()));
            doc.getDocumentElement().normalize();
            //title = new ArrayList<String>();

            NodeList nodeList = doc.getElementsByTagName("item");
            title = new ArrayList<String>(nodeList.getLength());
            description = new ArrayList<String>(nodeList.getLength());
            for (int i = 0; i < nodeList.getLength(); i++) {

                Node node = nodeList.item(i);

                Element fstElmnt = (Element) node;
                NodeList nameList = fstElmnt.getElementsByTagName("title");
                Element nameElement = (Element) nameList.item(0);
                nameList = nameElement.getChildNodes();

                title.add(""+ ((Node) nameList.item(0)).getNodeValue());



                NodeList websiteList = fstElmnt.getElementsByTagName("description");
                Element websiteElement = (Element) websiteList.item(0);
                websiteList = websiteElement.getChildNodes();

                description.add(""+ ((Node) websiteList.item(0)).getNodeValue());

                System.out.println(title.get(i));



            }
        } catch (Exception e) {
            System.out.println("XML Pasing Excpetion = " + e);
        }

       // System.out.println(title.get(0));


    }
}

