package sagex.phoenix.metadata.provider.tvdb4;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import sagex.phoenix.configuration.proxy.GroupProxy;
import sagex.phoenix.json.JSON;
import sagex.phoenix.metadata.*;
import sagex.phoenix.metadata.proxy.MetadataProxy;
import sagex.phoenix.metadata.search.MediaSearchResult;
import sagex.phoenix.metadata.search.MetadataSearchUtil;
import sagex.phoenix.metadata.search.SearchQuery;
import sagex.phoenix.util.DateUtils;
import sagex.phoenix.util.url.UrlUtil;
import sagex.remote.json.JSONArray;
import sagex.remote.json.JSONException;
import sagex.remote.json.JSONObject;

import java.io.*;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class TVDB4JsonHandler {
    private Logger log = Logger.getLogger(this.getClass());
    private static final Integer SeriesBanner = 1;
    private static final Integer SeriesPoster = 2;
    private static final Integer SeriesBackground = 3;
    private static final Integer SeasonBanner = 6;
    private static final Integer SeasonPoster = 7;
    private static final Integer SeasonBackground = 8;
    private String token = null;
    private String baseUrl = "https://api4.thetvdb.com/v4";
    private String apiKey = "a91c908d-d76d-4412-8713-837d0b28ad52"; //official sagetv/phoenix apiKey
    private String pin = "";
    TVDB4Configuration config = null;

    public TVDB4JsonHandler() {
        config = GroupProxy.get(TVDB4Configuration.class);
    }

    public String getPin() {
        pin = config.getPIN();
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
        config.setPIN(pin);
    }

    public Boolean hasPin(){
        if(getPin().isEmpty()) return false;
        return true;
    }

    /*
     * Check to ensure a PIN is available in the configuration and if so, use it to get a token
     */
    public Boolean validConfig(){
        pin = config.getPIN();
        if(pin.isEmpty()){
            log.warn("No TVDBv4 PIN found in configuration.  PIN is required to access TVDBv4 API. Go to TheTVDB.com to signup for this subscription service and then enter your PIN in the TVDB4 Configuration in BMT or in the sage.properties file under 'phoenix/metadata/tvdb4/pin'.");
            return false;
        }else{
            token = GetToken();
            if(token==null || token.isEmpty()){
                log.warn("TVDBv4 login FAILED.  Ensure your PIN is correct in configuration.  PIN is required to access TVDBv4 API. Go to TheTVDB.com to signup for this subscription service and then enter your PIN in the TVDB4 Configuration in BMT or in the sage.properties file under 'phoenix/metadata/tvdb4/pin'.");
                return false;
            }else {
                return true;
            }
        }
    }

    public String GetToken(){
        token = config.getToken();
        if(token==null || token.isEmpty()){
            final Charset UTF8_CHARSET = Charset.forName("UTF-8");

            String url = "https://api4.thetvdb.com/v4/login";
            URI uri = URI.create(url);
            CloseableHttpClient client = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(uri);

            String json = "{\"apikey\": \"" + apiKey + "\",\"pin\": \"" + pin + "\"}";
            StringEntity entity = null;
            try {
                entity = new StringEntity(json);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            httpPost.setEntity(entity);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");

            CloseableHttpResponse response = null;
            //HttpResponse response = null;
            try {
                response = client.execute(httpPost);
            } catch (IOException e) {
                e.printStackTrace();
            }
            HttpEntity responseEntity = response.getEntity();

            String output = "";
            JSONObject jsonResponse = null;
            try (InputStream instream = responseEntity.getContent()) {

                output = new BufferedReader(
                        new InputStreamReader(instream, StandardCharsets.UTF_8))
                        .lines()
                        .collect(Collectors.joining("\n"));
                log.debug("output:" + output);
                try {
                    jsonResponse = new JSONObject(output);
                    //log.info("jsonResponse:" + jsonResponse);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            String newToken = null;
            if(jsonResponse!=null){
                JSONObject dataResponse = JSON.get("data", jsonResponse);
                log.debug("dataResponse:" + dataResponse);
                newToken = JSON.getString("token", dataResponse);
            }

            if(newToken==null){
                log.debug("RESPONSE:" + response.getStatusLine().getStatusCode() + ":No token found" );
                token = null;
            }else{
                log.debug("RESPONSE:" + response.getStatusLine().getStatusCode() + ":token:" + newToken );
                token = newToken;
            }

            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //store the token in the config as it is good for 30 days
            if(token!=null){
                config.setToken(token);
            }
            return token;

        }else{
            log.debug("Token: returning existing token:" + token);
            return token;
        }

    }

    //Call this one if the complete URL is known
    public String GetDataFromTVDB4(String url){
        URI uri = null;
        try {
            uri = URI.create(url);
        } catch (Exception e) {
            log.warn("URI exception:" + uri);
            e.printStackTrace();
            return null;
        }
        log.debug("URI:" + uri);
        return GetDataFromTVDB4(uri);
    }

    //Call this one if we need to build the URL from parameters
    public String GetDataFromTVDB4(String urlAddon, List<String> parameters){
        StringBuffer parametersList = new StringBuffer();

        //not all calls need parameters so check if there are any
        if(parameters.size()>0){
            Collections.sort(parameters);
            parametersList.append("?");
            for (int i = 0; i < parameters.size(); i++) {
                parametersList.append(((i > 0) ? "&" : "") + parameters.get(i));
            }
        }

        String url = baseUrl +  urlAddon;
        URI uri = null;
        try {
            uri = URI.create(url + parametersList.toString());
        } catch (Exception e) {
            log.warn("URI exception:" + uri);
            e.printStackTrace();
            return null;
        }
        log.debug("URI:" + uri);
        return GetDataFromTVDB4(uri);
    }

    //Call this one if we already have an uri built
    public String GetDataFromTVDB4(URI uri){

        final Charset UTF8_CHARSET = Charset.forName("UTF-8");
        HttpClient client = HttpClientBuilder.create().build();

        HttpUriRequest request = new HttpGet(uri);
        request.addHeader("accept", "application/json");
        request.addHeader("Content-Type", "application/json");
        request.addHeader("Authorization", "Bearer " + token);

        HttpResponse response = null;
        try {
            response = client.execute(request);
            if (response.getStatusLine().getStatusCode()!=200){
                if (response.getStatusLine().getStatusCode()==401){
                    //not authorized - get new token and try again
                    if(GetToken()==null){
                        //no token retrieved so exit
                        log.debug("GetDataFromTVDB4: RESULT after getting NEW token: failed");
                    }else{
                        //try again with new token
                        request = new HttpGet(uri);
                        request.addHeader("accept", "application/json");
                        request.addHeader("Content-Type", "application/json");
                        request.addHeader("Authorization", "Bearer " + token);
                        response = client.execute(request);
                        if (response.getStatusLine().getStatusCode()!=200){
                            log.warn("GetDataFromTVDB4: response after getting new token: invalid response: code '" + response.getStatusLine().getStatusCode() + "' " + response);
                            return null;
                        }else{
                            log.debug("TEST: 200 response after 2nd attempt with new token");
                        }
                    }

                }

            }
            String responseJSON = EntityUtils.toString(response.getEntity(), UTF8_CHARSET);
            //log.info("responseJSON:" + responseJSON);
            return responseJSON;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }



    /*
    * searches TVDB v4 by Title and uses language if provided
    * @returns a JSON String to be parsed by the calling function
    *  or null if results were no found
     */
    public List<IMetadataSearchResult> search(String title, SearchQuery query, String lang){
        return search(title, query,lang, null);
    }

    public List<IMetadataSearchResult> search(String title, SearchQuery query, String lang, String limit){

        List<String> parameters = new ArrayList<>();
        parameters.add("query=" + UrlUtil.encode(title));
        parameters.add("type=" + "series");
        parameters.add("language=" + lang);
        if(limit!=null){
            parameters.add("limit=" + limit);
        }

        String responseJSON = GetDataFromTVDB4("/search", parameters);

        try {
            return JSONtoSearchResult(responseJSON, query);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    /*
    * Build search result list from JSON String
     */
    private List<IMetadataSearchResult> JSONtoSearchResult(String jsonString, SearchQuery query) throws JSONException {
        if(jsonString==null){
            return null;
        }
        List<IMetadataSearchResult> searchResult = new LinkedList<IMetadataSearchResult>();
        JSONObject jsonData = new JSONObject(jsonString);
        String status = JSON.getString("status", jsonData);
        if(status.equals("failure")){
            String failureMessage = JSON.getString("message", jsonData);
            log.warn("JSONtoSearchResult: failure:" + failureMessage);
            return null;
        }else if(!status.equals("success")){
            log.debug("JSONtoSearchResult: status was not 'success':" + status);
            return null;
        }
        //we have a success result so process the data
        JSON.each("data", jsonData, new JSON.ArrayVisitor() {
            public void visitItem(int i, JSONObject item) {
                //build a search result item
                String searchTitle = query.get(SearchQuery.Field.QUERY);

                MediaSearchResult sr = new MediaSearchResult();
                MetadataSearchUtil.copySearchQueryToSearchResult(query, sr);
                sr.setMediaType(MediaType.TV);
                //sr.setProviderId(provider.getInfo().getId());
                sr.setProviderId("tvdb4");
                //Pair<String, String> pair = ParserUtils.parseTitleAndDateInBrackets(sagex.phoenix.util.StringUtils.unquote(JSON.getString("name", item)));
                //sr.setTitle(pair.first());
                if(item.has("name")){
                    sr.setTitle(JSON.getString("name", item));
                }
                sr.setScore(getScore(sr.getTitle(),searchTitle));
                if(item.has("year")){
                    sr.setYear(JSON.getInt("year", item));
                }
                if(item.has("tvdb_id")){
                    sr.setId(JSON.getString("tvdb_id", item));
                }
                if(item.has("image_url")){
                    sr.setUrl(JSON.getString("image_url", item));
                }
                //get the remote IDs

                if (item.has("remote_ids")){
                    JSON.each("remote_ids", item, new JSON.ArrayVisitor() {
                        public void visitItem(int x, JSONObject remoteItem) {
                            String sourceName = JSON.getString("sourceName", remoteItem);
                            if(sourceName.equals("IMDB")){
                                sr.setIMDBId(JSON.getString("id", remoteItem));
                            }
                        }
                    });
                }

                searchResult.add(sr);
            }
        });
        return searchResult;
    }

    private float getScore(String title, String searchTitle) {
        if (title == null)
            return 0.0f;
        try {
            float score = MetadataSearchUtil.calculateCompressedScore(searchTitle, title);
            log.debug(String.format("Comparing:[%s][%s]: %s", searchTitle, title, score));
            return score;
        } catch (Exception e) {
            return 0.0f;
        }
    }

    public List<IMetadata> GetEpisodes(String id) throws JSONException {
        List<IMetadata> episodeList = new ArrayList<>();
        //Episodes returns 500 items per call so may have to make multiple calls to get all episodes
        Integer page = 0;
        Boolean morePages = true;
        String episodesURL = "/series/" + id + "/episodes/official/" + config.getLanguage() + "?page=0";
        String responseJSON = GetDataFromTVDB4(episodesURL, new ArrayList<>());
        while(morePages){
            log.debug("GetEpisodes: page" + page + " responseJSON" + responseJSON);
            JSONObject jsonData = new JSONObject(responseJSON);
            String status = JSON.getString("status", jsonData);
            if(status.equals("failure")){
                String failureMessage = JSON.getString("message", jsonData);
                log.warn("GetEpisodes: page:" + page + " failure:" + failureMessage);
                return episodeList;
            }else if(!status.equals("success")){
                log.debug("GetEpisodes: page:" + page + " status was not 'success':" + status);
                return episodeList;
            }

            if(responseJSON!=null){
                JSONObject dataResponse = JSON.get("data", jsonData);
                if(!jsonIsValid(dataResponse,"episodes")){
                    log.debug("GetEpisodes: page:" + page + " 'episodes' key not found for id:" + id);
                    return episodeList;
                }
                JSONArray jsonArray = JSON.get("episodes", dataResponse);
                for (int i = 0, size = jsonArray.length(); i < size; i++){
                    JSONObject item = jsonArray.getJSONObject(i);
                    if(!jsonIsValid(item,"seasonNumber") || !jsonIsValid(item,"number")){
                        log.debug("GetEpisodes: page:" + page + " S/E missing from JSON. Skipping item:" + item);
                        continue;
                    }
                    Integer itemSeason = item.getInt("seasonNumber");
                    Integer itemEpisode = item.getInt("number");
                    String itemName = JSON.getString("name",item);
                    String itemDesc = JSON.getString("overview",item);
                    Date itemOAD = null;
                    if(item.has("aired") && !JSON.getString("aired", item).equals("null")){
                        itemOAD = DateUtils.parseDate(JSON.getString("aired", item));
                    }
                    IMetadata thisEpisode = MetadataProxy.newInstance();

                    Integer runtimeInt = JSON.getInt("runtime", item);
                    if(runtimeInt!=null){
                        long runtime = runtimeInt * 60 * 1000;
                        thisEpisode.setRunningTime(runtime);
                    }
                    if(item.has("aired") && !JSON.getString("aired", item).equals("null")){
                        thisEpisode.setYear(DateUtils.parseYear(JSON.getString("aired", item)));
                    }
                    thisEpisode.setExternalID(JSON.getString("id",item));
                    thisEpisode.setSeasonNumber(itemSeason);
                    thisEpisode.setEpisodeNumber(itemEpisode);
                    thisEpisode.setEpisodeName(itemName);
                    thisEpisode.setOriginalAirDate(itemOAD);
                    thisEpisode.setDescription(itemDesc);

                    thisEpisode.setMediaProviderID("tvdb4");
                    thisEpisode.setMediaProviderDataID(id);

                    episodeList.add(thisEpisode);
                    log.debug("GetEpisodes: page:" + page + " added TVEpisode:" + thisEpisode.toString());
                }

                //Now check if there are more pages to process
                //links section - if exists - contains link urls to next page
                morePages = false;
                if(jsonIsValid(jsonData,"links")){
                    JSONObject linksResponse = JSON.get("links", jsonData);
                    log.debug("GetEpisodes: page:" + page + " links section found:" + linksResponse);
                    if(jsonIsValid(linksResponse,"next")){
                        episodesURL = JSON.getString("next",linksResponse);
                        log.debug("GetEpisodes: page:" + page + " Next URL set to:" + episodesURL);
                        morePages = true;
                        page++;
                        responseJSON = GetDataFromTVDB4(episodesURL);
                    }
                }
            }
        }
        return episodeList;
    }

    private String getEpisodesURL(String id, Integer page){
        return "/series/" + id + "/episodes/official/" + config.getLanguage() + "?page=" + page.toString();
    }

    public Boolean GetEpisode(String id, IMetadata md, Integer searchSeason, Integer searchEpisode, Date searchDate, String searchTitle) throws JSONException {
        List<IMetadata> episodeList = GetEpisodes(id);
        if(episodeList.size()>0){
            //Determine the search type
            Boolean bySeasonEpisode = false;
            Boolean byDate = false;
            Boolean byTitle = false;
            final float[] titleScore = {0};

            if(searchSeason!=null && searchEpisode!=null) bySeasonEpisode = true;
            else if(searchDate!=null) byDate = true;
            else if(searchTitle!=null) byTitle = true;

            if(bySeasonEpisode){
                log.debug("GetEpisode: search by SeasonEpisode for id:" + id + " S" + searchSeason + "E" + searchEpisode);
                for (IMetadata item: episodeList) {
                    Integer itemSeason = item.getSeasonNumber();
                    Integer itemEpisode = item.getEpisodeNumber();
                    if(itemSeason.equals(searchSeason) && itemEpisode.equals(searchEpisode)){
                        updateMetadataFromItem(md,item);
                        log.debug("GetEpisode: found SEASON:" + itemSeason + " EPISODE:" + itemEpisode);
                        return true;
                    }
                }
                log.debug("GetEpisode: search by SeasonEpisode: No match found for id:" + id + " S" + searchSeason + "E" + searchEpisode);
                return false;
            }else if(byDate){
                log.debug("GetEpisode: search by Date for id:" + id + " Date:" + searchDate);
                for (IMetadata item: episodeList) {
                    if(item.getOriginalAirDate()==null){
                        log.debug("GetEpisode: search by Date: OAD is null. Skipping item: S" + item.getSeasonNumber() + "E" + item.getEpisodeNumber());
                        continue;
                    }
                    if(item.getOriginalAirDate()!=null && item.getOriginalAirDate().equals(searchDate)){
                        updateMetadataFromItem(md,item);
                        log.debug("GetEpisode: found by DATE:" + item.getOriginalAirDate());
                        return true;
                    }
                }
                log.debug("GetEpisode: search by Date: No match found for id:" + id + " Date:" + searchDate);
                return false;
            }else if(byTitle){
                log.debug("GetEpisode: search by Title for id:" + id + " Title:" + searchTitle);
                float bestScore = 0;
                IMetadata bestItem = null;
                String bestTitle = null;
                for (IMetadata item: episodeList) {
                    if(item.getEpisodeName().equals(searchTitle)){
                        //Exact match
                        updateMetadataFromItem(md,item);
                        log.debug("GetEpisode: found by Title Exact Match:" + item.getEpisodeName());
                        return true;
                    }
                    //title does not match so get a score
                    float score = MetadataSearchUtil.calculateCompressedScore(searchTitle, item.getEpisodeName());

                    if (score > bestScore) {
                        bestScore = score;
                        bestItem = item;
                        bestTitle = item.getEpisodeName();
                        log.debug("GetEpisode: found by Title found a match for searchTitle:" + searchTitle + " match:" + item.getEpisodeName() + " score:" + score);
                    }
                    //do not exit (return) here as we need to check other possible higher scored matches
                }
                //check if we found a match
                if(bestItem!=null){
                    updateMetadataFromItem(md,bestItem);
                    log.debug("GetEpisode: found by Title found a match for searchTitle:" + searchTitle + " match:" + bestTitle + " score:" + bestScore);
                    return true;
                }
                return false;
            }else{
                log.debug("GetEpisode: invalid parameters passed.  Could not determine search type for id:" + id);
                return false;
            }

        }
        return false;
    }

    private void updateMetadataFromItem(IMetadata md, IMetadata item){
        //set all episode info here
        md.setSeasonNumber(item.getSeasonNumber());
        md.setEpisodeNumber(item.getEpisodeNumber());
        md.setEpisodeName(item.getEpisodeName());
        md.setDescription(item.getDescription());
        //running time is stored in milliseconds
        md.setRunningTime(item.getRunningTime());
        md.setOriginalAirDate(item.getOriginalAirDate());
        md.setYear(item.getYear());

        //No user rating available from TVDB
        //md.setUserRating(MetadataSearchUtil.parseUserRating());

        //save the TVDB episode id to run another query to get episode details
        md.setExternalID(item.getExternalID());
        try {
            GetEpisodeDetails(md.getExternalID(),md);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Boolean GetEpisodeDetails(String id, IMetadata md) throws JSONException {
        String responseJSON = GetDataFromTVDB4("/episodes/" + id + "/extended", new ArrayList<>());
        //String responseJSON = GetDataFromTVDB4("/series/" + id + "/episodes/official/" + config.getLanguage() + "?page=0", new ArrayList<>());
        log.debug("GetEpisodeDetails: responseJSON" + responseJSON);
        JSONObject jsonData = new JSONObject(responseJSON);
        String status = JSON.getString("status", jsonData);
        if(status.equals("failure")){
            String failureMessage = JSON.getString("message", jsonData);
            log.warn("GetEpisodeDetails: failure:" + failureMessage);
            return false;
        }else if(!status.equals("success")){
            log.debug("GetEpisodeDetails: status was not 'success':" + status);
            return false;
        }

        if(responseJSON!=null){
            JSONObject dataResponse = JSON.get("data", jsonData);
            //log.info("GetSeries: dataResponse:" + dataResponse);
            //set all the detailed episode info here
            //get the rating (take the first one if multiples)
            if(dataResponse.has("contentRatings")){
                JSON.each("contentRatings", dataResponse, new JSON.ArrayVisitor() {
                    public void visitItem(int i, JSONObject item) {
                        if(i==0){
                            String rating = MetadataUtil.fixContentRating(MediaType.TV, JSON.getString("name", item));
                            //md.setRated(rating);
                            md.setExtendedRatings(rating);
                            md.setParentalRating(rating);
                            return;
                        }
                    }
                });
            }

            //get the IMDB ID
            if(dataResponse.has("remoteIds")){
                JSON.each("remoteIds", dataResponse, new JSON.ArrayVisitor() {
                    public void visitItem(int x, JSONObject remoteItem) {
                        String sourceName = JSON.getString("sourceName", remoteItem);
                        if(sourceName.equals("IMDB")){
                            md.setIMDBID(JSON.getString("id", remoteItem));
                        }
                    }
                });
            }

            if(dataResponse.has("image")){
                md.setDefaultBackground(JSON.getString("image",dataResponse));
            }

            //add cast members
            if(jsonIsValid(dataResponse,"characters")){
                JSON.each("characters", dataResponse, new JSON.ArrayVisitor() {
                    public void visitItem(int x, JSONObject character) {
                        if(JSON.getString("peopleType", character).equals("Director")){
                            CastMember cm = new CastMember();
                            cm.setName(JSON.getString("personName", character));
                            if(JSON.getString("image", character)!="null"){
                                cm.setImage(JSON.getString("image", character));
                            }
                            log.debug("GetEpisodeDetails: adding director:" + cm);
                            md.getDirectors().add(cm);
                        }
                        if(JSON.getString("peopleType", character).equals("Guest Star")){
                            CastMember cm = new CastMember();
                            cm.setName(JSON.getString("personName", character));
                            if(JSON.getString("image", character)!="null"){
                                cm.setImage(JSON.getString("image", character));
                            }
                            log.debug("GetEpisodeDetails: adding guest:" + cm);
                            md.getGuestStars().add(cm);
                            md.getGuests().add(cm);
                        }
                        if(JSON.getString("peopleType", character).equals("Writer")){
                            CastMember cm = new CastMember();
                            cm.setName(JSON.getString("personName", character));
                            if(JSON.getString("image", character)!="null"){
                                cm.setImage(JSON.getString("image", character));
                            }
                            log.debug("GetEpisodeDetails: adding writer:" + cm);
                            md.getWriters().add(cm);
                        }
                    }
                });

            }
            return true;
        }
        return false;
    }

    public String GetSeriesIDFromIMDBID(String imdbID) throws JSONException {
        String SeriesID = null;
        String responseJSON = GetDataFromTVDB4("/search/remoteid/" + imdbID, new ArrayList<>());
        log.debug("GetSeriesIDFromIMDBID: responseJSON" + responseJSON);
        JSONObject jsonData = new JSONObject(responseJSON);
        String status = JSON.getString("status", jsonData);
        if(status.equals("failure")){
            String failureMessage = JSON.getString("message", jsonData);
            log.warn("GetSeriesIDFromIMDBID: failure:" + failureMessage);
            return null;
        }else if(!status.equals("success")){
            log.debug("GetSeriesIDFromIMDBID: status was not 'success':" + status);
            return null;
        }

        if(responseJSON!=null){

            JSONArray dataJsonArray = JSON.get("data", jsonData);
            for (int i = 0, size = dataJsonArray.length(); i < size; i++){
                JSONObject item = dataJsonArray.getJSONObject(i);

                //if the IMDBID is for an episode get the series id from within an episode response
                if(item.has("episode")){
                    JSONObject episodeResponse = JSON.get("episode", item);
                    if(episodeResponse.has("seriesId")){
                        SeriesID = JSON.getString("seriesId", episodeResponse);
                    }
                }else if(item.has("series")){
                    JSONObject seriesResponse = JSON.get("series", item);
                    if(seriesResponse.has("id")){
                        SeriesID = JSON.getString("id", seriesResponse);
                    }
                }

                log.debug("GetSeriesIDFromIMDBID: result:" + SeriesID);
                return SeriesID;
            }
        }
        return null;
    }

    public Boolean GetSeries(String id, ISeriesInfo sinfo) throws JSONException {
        String responseJSON = GetDataFromTVDB4("/series/" + id + "/extended", new ArrayList<>());
        //log.info("GetSeries: responseJSON" + responseJSON);
        JSONObject jsonData = new JSONObject(responseJSON);
        String status = JSON.getString("status", jsonData);
        if(status.equals("failure")){
            String failureMessage = JSON.getString("message", jsonData);
            log.warn("GetSeries: failure:" + failureMessage);
            return false;
        }else if(!status.equals("success")){
            log.debug("GetSeries: status was not 'success':" + status);
            return false;
        }

        if(responseJSON!=null){
            JSONObject dataResponse = JSON.get("data", jsonData);
            //log.info("GetSeries: dataResponse:" + dataResponse);
            //set all the series info here
            sinfo.setTitle(JSON.getString("name", dataResponse));

            //get the rating (take the first one if multiples)
            if(dataResponse.has("contentRatings")){
                JSON.each("contentRatings", dataResponse, new JSON.ArrayVisitor() {
                    public void visitItem(int i, JSONObject item) {
                        if(i==0){
                            sinfo.setContentRating(MetadataUtil.fixContentRating(MediaType.TV, JSON.getString("name", item)));
                        }
                    }
                });
            }

            if(dataResponse.has("firstAired")){
                sinfo.setPremiereDate(JSON.getString("firstAired", dataResponse));
            }

            //get all the genre
            if(dataResponse.has("genres")){
                JSON.each("genres", dataResponse, new JSON.ArrayVisitor() {
                    public void visitItem(int i, JSONObject item) {
                        String genre = JSON.getString("name", item);
                        if (!StringUtils.isEmpty(genre)) {
                            sinfo.getGenres().add(genre.trim());
                            log.debug("GetSeries: adding genre:" + genre);
                        }
                    }
                });
            }

            if(dataResponse.has("overview")){
                sinfo.setDescription(JSON.getString("overview", dataResponse));
                log.debug("GetSeries: description:" + sinfo.getDescription());
            }

            //get the airday - use the first one
            if(dataResponse.has("airsDays")){
                JSONObject airDays = JSON.get("airsDays", dataResponse);
                String dow = "sunday,monday,tuesday,wednesday,thursday,friday,saturday";
                for (String day: dow.split(",")) {
                    if(JSON.getString(day,airDays).equals("true")){
                        sinfo.setAirDOW(day);
                        log.debug("GetSeries: getDOW: found:" + day);
                        break;
                    }
                }
            }

            if(dataResponse.has("airsTime")){
                sinfo.setAirHrMin(JSON.getString("airsTime", dataResponse));
            }

            if(dataResponse.has("nextAired")){
                //get the date of the last airing - assume if NO next airing then the last airing is a finale
                if(StringUtils.isEmpty(JSON.getString("nextAired", dataResponse))){
                    if(dataResponse.has("lastAired")){
                        sinfo.setFinaleDate(JSON.getString("lastAired", dataResponse));
                    }
                }
            }

            if(dataResponse.has("image")){
                sinfo.setImage(JSON.getString("image", dataResponse));
                log.debug("GetSeries: getBanner '" + sinfo.getImage() + "'");
            }

            //get the oringinal network
            if(dataResponse.has("originalNetwork")){
                JSONObject network = JSON.get("originalNetwork", dataResponse);
                if(network!=null){
                    if(dataResponse.has("name")){
                        sinfo.setNetwork(JSON.getString("name", network));
                    }
                }
            }

            //get the Zap2ItID
            if(dataResponse.has("remoteIds")){
                JSON.each("remoteIds", dataResponse, new JSON.ArrayVisitor() {
                    public void visitItem(int x, JSONObject remoteItem) {
                        String sourceName = JSON.getString("sourceName", remoteItem);
                        if(sourceName.equals("TMS (Zap2It)")){
                            sinfo.setZap2ItID(JSON.getString("id", remoteItem));
                        }
                    }
                });
            }

            if(dataResponse.has("averageRuntime")){
                sinfo.setRuntime(JSON.getInt("averageRuntime", dataResponse));
            }

            //process the actors list - series only has actors for cast members so skip all others
            if(dataResponse.has("characters")){
                JSON.each("characters", dataResponse, new JSON.ArrayVisitor() {
                    public void visitItem(int x, JSONObject character) {
                        if(JSON.getString("peopleType", character).equals("Actor")){
                            CastMember cm = new CastMember();
                            cm.setName(JSON.getString("personName", character));
                            cm.setRole(JSON.getString("name", character));
                            cm.setImage(JSON.getString("image", character));
                            log.debug("GetSeries: adding actor:" + cm);
                            sinfo.getCast().add(cm);
                        }
                    }
                });
            }

            /*
            // sinfo.setHistory();
             */
            log.debug("GetSeries: result:" + sinfo);
            return true;
        }
        return false;
    }

    public Boolean GetFanart(String id, IMetadata md, String season) throws JSONException {
        String responseJSON = GetDataFromTVDB4("/series/" + id + "/artworks?lang=" + config.getLanguage(), new ArrayList<>());
        log.debug("GetSeries: responseJSON" + responseJSON);
        JSONObject jsonData = new JSONObject(responseJSON);
        String status = JSON.getString("status", jsonData);
        if(status.equals("failure")){
            String failureMessage = JSON.getString("message", jsonData);
            log.warn("GetFanart: failure:" + failureMessage);
            return false;
        }else if(!status.equals("success")){
            log.debug("GetFanart: status was not 'success':" + status);
            return false;
        }

        int inSeason = NumberUtils.toInt(season, -9);


        if(responseJSON!=null){
            JSONObject dataResponse = JSON.get("data", jsonData);

            //process all the fanart
            if(dataResponse.has("artworks")){
                JSON.each("artworks", dataResponse, new JSON.ArrayVisitor() {
                    public void visitItem(int i, JSONObject item) {
                        Integer artType = JSON.getInt("type", item);
                        String artLang = JSON.getString("language", item);
                        String artImage = JSON.getString("image", item);
                        //check each type and only save valid types
                        if(artType.equals(SeriesBanner)){
                            addFanartItem(md,MediaArtifactType.BANNER,artImage,artLang);
                        }else if(artType.equals(SeriesPoster)){
                            addFanartItem(md,MediaArtifactType.POSTER,artImage,artLang);
                        }else if(artType.equals(SeriesBackground)){
                            addFanartItem(md,MediaArtifactType.BACKGROUND,artImage,artLang);
                        }

                        //only process season art if a season was passed
                        if (inSeason>0) {
                            if(artType.equals(SeasonBanner)){
                                addFanartItem(md,MediaArtifactType.BANNER,artImage,artLang);
                            }else if(artType.equals(SeasonPoster)){
                                addFanartItem(md,MediaArtifactType.POSTER,artImage,artLang);
                            }else if(artType.equals(SeasonBackground)){
                                addFanartItem(md,MediaArtifactType.BACKGROUND,artImage,artLang);
                            }
                        }
                    }
                });
                return true;
            }
        }

        /*
        String epImage = el.getFilename();
        if (!StringUtils.isEmpty(epImage)) {
            // Added for EvilPenguin
            md.getFanart().add(
                    new MediaArt(MediaArtifactType.EPISODE, epImage, md.getSeasonNumber()));
        }
         */

        return false;
    }

    private void addFanartItem(IMetadata md, MediaArtifactType mat, String path, String lang){
        if(config.getLanguage().equals(lang)){
            log.debug("addFanartItem: adding: " + mat.name() + " path:" + path + " lang:" + lang);
            MediaArt ma = new MediaArt();
            ma.setType(mat);
            ma.setDownloadUrl(path);
            md.getFanart().add(ma);

        }else{
            log.debug("addFanartItem: SKIPPING: " + mat.name() + " path:" + path + " lang:" + lang);
        }
    }

    private Boolean jsonIsValid(JSONObject item, String key){
        if(item.has(key) && !JSON.getString(key, item).equals("null")){
            return true;
        }
        return false;
    }
}
