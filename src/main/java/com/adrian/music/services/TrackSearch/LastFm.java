package com.adrian.music.services.TrackSearch;

import com.adrian.music.models.Track;
import com.adrian.music.utils.Constants;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: lordfire
 * Date: 1/09/13
 * Time: 13:54
 * To change this template use File | Settings | File Templates.
 */
public class LastFm implements TrackSearch {

    String apiKey = Constants.LAST_FM_API_KEY;
    String secret = Constants.LAST_FM_SECRET;
    String noImageAvariable = "http://media-mcw.cursecdn.com/es/thumb/1/1d/No_image.svg/50px-No_image.svg.png";

    String serviceURL =  "http://ws.audioscrobbler.com/2.0/";
    String method = "?method=track.getInfo&";
    String type = "json";

    private final String USER_AGENT = "Mozilla/5.0";


     //TODO Mejorar esta clase..


    public String performSearch(String title, String artist) throws IOException, URISyntaxException, HttpException {


        //Objeto de tipo httpCliente que hara la conexion

        DefaultHttpClient httpClient = new DefaultHttpClient();


        List<NameValuePair> params = new LinkedList<NameValuePair>();

        params.add(new BasicNameValuePair("artist",artist));
        params.add(new BasicNameValuePair("track", title));
        params.add(new BasicNameValuePair("format", type));
        params.add(new BasicNameValuePair("api_key", apiKey));



        String paramString = URLEncodedUtils.format(params, "utf-8");

        HttpGet httpGet = new HttpGet(serviceURL+method+paramString);


        //Se crea un objeto httpResponse para almacenar la respuesta
        HttpResponse httpResponse = httpClient.execute(httpGet);

        //Respuesta
        HttpEntity httpEntity = httpResponse.getEntity();

        //is es un inputStream
        InputStream is = httpEntity.getContent();

        //Se crea un reader que leera el objeto is (inputStream)
        BufferedReader reader = new BufferedReader(new InputStreamReader(is), 8);
        //sb es un ojbeto de tipo stringbuildier, sirve para ir creando un string mientras se lee el reader
        StringBuilder sb = new StringBuilder();
        //linea que se leera del buffer
        String line = null;

        //Bucle que leera lineas del buffer mientras no sea nulo,
        while ((line = reader.readLine()) != null) {

            //Cada linea extraida del buffer se añade al String buildier
            sb.append(line + "\n");
        }
        //Finalmente se cierra el inputStream
        is.close();

        //Guardamos el String creado en una variable de tipo String, contiene el objeto en notacion JSON

        return sb.toString();

    }

    @Override
    public Track searhTrack(String title, String artist) {

        Track track = new Track();


            try{
                 String json = performSearch(title, artist);

                JsonElement jelement = new JsonParser().parse(json);
                JsonObject jobject = jelement.getAsJsonObject();
                JsonObject trackObject = jobject.getAsJsonObject("track");


                JsonObject album = trackObject.get("album").getAsJsonObject();
                JsonArray imageArray = album.get("image").getAsJsonArray();

                String smallURL = imageArray.get(0).getAsJsonObject().get("#text").getAsString();
                String mediamURL = imageArray.get(1).getAsJsonObject().get("#text").getAsString();
                String bigImageUrl = imageArray.get(2).getAsJsonObject().get("#text").getAsString();

                track.setSmallImageUrl(smallURL);
                track.setMediumImageUrl(mediamURL);
                track.setBigImageUrl(bigImageUrl);

                track.setArtist(title);
                track.setAlbumName(artist);


            }catch (NullPointerException | IllegalStateException e){

                //NUll pointer exception por que no hay album, es decir imagenes

                //Se pone imagen por defecto
                track.setSmallImageUrl(noImageAvariable);
                track.setMediumImageUrl(noImageAvariable);
                track.setBigImageUrl(noImageAvariable);

             } catch (IOException | URISyntaxException | HttpException  e) {

                track =  new Track();
           }

        return track;
    }



}

