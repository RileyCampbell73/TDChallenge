package rileycampbell.example.com.tdchallenge;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Riley4 on 11/4/2014.
 */
public class Place {
    private String id;
    private String icon;
    private String name;
    private String vicinity;
    private Double latitude;
    private Double longitude;
    private String[] types;

    public String[] getTypes() {
        return types;
    }

    public void setTypes(String[] types) {
        this.types = types;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getIcon() {
        return icon;
    }
    public void setIcon(String icon) {
        this.icon = icon;
    }
    public Double getLatitude() {
        return latitude;
    }
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }
    public Double getLongitude() {
        return longitude;
    }
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getVicinity() {
        return vicinity;
    }
    public void setVicinity(String vicinity) {
        this.vicinity = vicinity;
    }

    static Place jsonToPontoReferencia(JSONObject pontoReferencia) {
        try {
            Place result = new Place();
            JSONObject geometry = (JSONObject) pontoReferencia.get("geometry");
            JSONObject location = (JSONObject) geometry.get("location");
            result.setLatitude((Double) location.get("lat"));
            result.setLongitude((Double) location.get("lng"));
            result.setIcon(pontoReferencia.getString("icon"));
            result.setName(pontoReferencia.getString("name"));
            result.setVicinity(pontoReferencia.getString("vicinity"));
            result.setId(pontoReferencia.getString("id"));
            JSONArray arr = pontoReferencia.getJSONArray("types");

            String[] types = new String[arr.length()];
            for(int i = 0; i < arr.length(); i++){
                types[i] = (arr.get(i).toString());

            }
            result.setTypes(types);

            return result;
        } catch (JSONException ex) {
            Logger.getLogger(Place.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public String toString() {
        return "Place{" + "id=" + id + ", icon=" + icon + ", name=" + name + ", latitude=" + latitude + ", longitude=" + longitude + '}';
    }

}
