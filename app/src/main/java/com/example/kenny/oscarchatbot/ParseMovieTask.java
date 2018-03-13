package com.example.kenny.oscarchatbot;

import android.os.AsyncTask;
import android.util.Log;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

class ParseMovieTask extends AsyncTask<Void, Void, Void> {
    private String pageUrl;
    private String name;
    private String enName;
    private String date;
    private String content;
    private String link;
    private ArrayList<ParserMovie> parserMovies = new ArrayList<>();

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            // 下兩個月的月份
            SimpleDateFormat formatter = new SimpleDateFormat("M");
            Date curDate = new Date(System.currentTimeMillis()) ;
            Calendar rightNow = Calendar.getInstance();
            rightNow.setTime(curDate);
            rightNow.add(Calendar.MONTH, 1);

            Calendar rightNow1 = Calendar.getInstance();
            rightNow1.setTime(curDate);
            rightNow1.add(Calendar.MONTH, 2);

            Date one = rightNow.getTime();
            Date two = rightNow1.getTime();
            String date1 = formatter.format(one);
            String date2 = formatter.format(two);
            Logger.d(date1 + " " + date2);
            ArrayList<String> dateCollection = new ArrayList<>();
            dateCollection.add(date1);
            dateCollection.add(date2);
            int pageNumber;
            // 爬蟲下兩個月上映的電影
            for(int k = 0; k < dateCollection.size(); k++) {
                String page = "https://movies.yahoo.com.tw/movie_comingsoon.html?month=" + dateCollection.get(k);
                org.jsoup.nodes.Document pageDoc =  Jsoup.connect(page).get();
                if(!pageDoc.select("div.page_numbox").isEmpty()) {
                    Elements numBoxElements = pageDoc.select("div.page_numbox");
                    Element pageElement = numBoxElements.get(0);
                    pageNumber = pageElement.select("a").size();
                    Log.d("Kenny", String.valueOf(pageNumber));
                }
                else {
                    pageNumber = 0;
                    Log.d("Kenny", String.valueOf(pageNumber));
                }

                // 該月要上映的電影只有一頁
                if(pageNumber == 0) {
                    pageUrl = page;

                    // 抓電影主集合
                    org.jsoup.nodes.Document movieDoc =  Jsoup.connect(pageUrl).get();
                    Elements movieElements = movieDoc.select("ul.release_list");
                    Elements subMovieElement = movieElements.select("div.release_info");
                    for(Element attrElement : subMovieElement) {
                        ArrayList<String> type = new ArrayList<>();
                        // 抓電影中文、英文名稱、上映日期、內容大綱、網址連結
                        name = attrElement.select("a.gabtn").get(0).text();
                        enName = attrElement.select("div.en").text();
                        String oldTime = attrElement.select("div.release_movie_time").text();
                        date = attrElement.select("div.release_movie_time").text().substring(oldTime.indexOf("期") + 3, oldTime.length());
                        date = date.replace(" ", "");
                        content = attrElement.select("div.release_text").text();
                        link = attrElement.select("a").attr("href").toString();
                        // 抓電影類型
                        org.jsoup.nodes.Document movieInnerDoc =  Jsoup.connect(link).get();
                        Elements movieInnerElements = movieInnerDoc.select("div.level_name_box");
                        for(Element element : movieInnerElements.select("div.level_name")) {
                            if(element.select("a").text().contains("/")) {
                                String x = element.select("a").text();
                                type.add(element.select("a").text().substring(0, x.indexOf("/")));
                                type.add(element.select("a").text().substring(x.indexOf("/") + 1, x.length()));
                            }
                            else {
                                type.add(element.select("a").text());
                            }
                        }
                        ParserMovie parserMovie = new ParserMovie(name, enName, date, content, link, type);
                        parserMovies.add(parserMovie);
                    }
                }
                // 該月的未上映的電影超過一頁
                for(int i = 1; i <= pageNumber; i++) {

                    pageUrl = page + "&page=" +i;

                    // 抓電影主集合
                    org.jsoup.nodes.Document movieDoc =  Jsoup.connect(pageUrl).get();
                    Elements movieElements = movieDoc.select("ul.release_list");
                    Elements subMovieElement = movieElements.select("div.release_info");
                    for(Element attrElement : subMovieElement) {
                        ArrayList<String> type = new ArrayList<>();
                        // 抓電影中文、英文名稱、上映日期、內容大綱、網址連結
                        name = attrElement.select("a.gabtn").get(0).text();
                        enName = attrElement.select("div.en").text();
                        String oldTime = attrElement.select("div.release_movie_time").text();
                        date = attrElement.select("div.release_movie_time").text().substring(oldTime.indexOf("期") + 3, oldTime.length());
                        content = attrElement.select("div.release_text").text();
                        link = attrElement.select("a").attr("href").toString();
                        // 抓電影類型
                        org.jsoup.nodes.Document movieInnerDoc =  Jsoup.connect(link).get();
                        Elements movieInnerElements = movieInnerDoc.select("div.level_name_box");
                        for(Element element : movieInnerElements.select("div.level_name")) {
                            if(element.select("a").text().contains("/")) {
                                String x = element.select("a").text();
                                type.add(element.select("a").text().substring(0, x.indexOf("/")));
                                type.add(element.select("a").text().substring(x.indexOf("/") + 1, x.length()));
                            }
                            else {
                                type.add(element.select("a").text());
                            }
                        }
                        ParserMovie parserMovie = new ParserMovie(name, enName, date, content, link, type);
                        parserMovies.add(parserMovie);
                    }
                }

            }

        }
        catch (Exception ex) {
            Logger.d(ex.getMessage());
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        for(int i = 0; i < parserMovies.size(); i++) {
            Logger.d(parserMovies.get(i).getName());
        }
        storeToFireBase();
    }
    // 爬蟲後資料存入Firebase
    private void storeToFireBase() {
        FirebaseDatabase fireDB = FirebaseDatabase.getInstance();
        DatabaseReference myRef = fireDB.getReference("電影");
        myRef.removeValue();
        for(int i = 0; i < parserMovies.size(); i++) {
           myRef.child(String.valueOf(i)).setValue(parserMovies.get(i));
        }
    }
}
