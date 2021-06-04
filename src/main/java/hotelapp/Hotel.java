package hotelapp;

import com.google.gson.annotations.SerializedName;

public class Hotel {
    @SerializedName(value = "f")
    private String name;
    private int id;
    @SerializedName(value = "lat")
    private double latitude;
    @SerializedName(value = "lng")
    private double longitude;
    @SerializedName(value = "ad")
    private String streets;
    @SerializedName(value = "ci")
    private String city;
    @SerializedName(value = "pr")
    private String province;
    @SerializedName(value = "c")
    private String country;

    public int getId(){ return id; }

    public String getName(){ return name; }

    public String getCity(){ return city; }

    public String getStreets() { return streets;}

    public String getProvince() { return province;}

    public String getCountry() { return country;}

    public String toString(){
        return "Name: " + name + System.lineSeparator() +
                "ID: " + id + System.lineSeparator() +
                "Latitude: " + latitude + System.lineSeparator() +
                "Longitude: " + longitude + System.lineSeparator() +
                "Street: " + streets + System.lineSeparator() +
                "City: " + city + System.lineSeparator() +
                "Province/State: " + province + System.lineSeparator() +
                "Country: " + country + System.lineSeparator();
    }
}
