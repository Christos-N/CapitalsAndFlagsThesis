package com.unipi.chris.capitalsandflags;


import android.os.Parcel;
import android.os.Parcelable;

public class Country implements Parcelable {
    private String name;
    private String capital;
    private String flagImageName;
    private String continent;

    public Country(String name, String capital, String continent, String flagImageName) {
        this.name = name;
        this.capital = capital;
        this.continent = continent;
        this.flagImageName = flagImageName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCapital() {
        return capital;
    }

    public String getFlagImageName() {
        return flagImageName;
    }

    public String getContinent() {
        return continent;
    }

    public void setContinent(String continent) {
        this.continent = continent;
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(capital);
        dest.writeString(flagImageName);
        dest.writeString(continent); // Write the new field to Parcel
    }

    protected Country(Parcel in) {
        name = in.readString();
        capital = in.readString();
        flagImageName = in.readString();
        continent = in.readString(); // Read the new field from Parcel
    }

    public static final Creator<Country> CREATOR = new Creator<Country>() {
        @Override
        public Country createFromParcel(Parcel in) {
            return new Country(in);
        }

        @Override
        public Country[] newArray(int size) {
            return new Country[size];
        }
    };
    @Override
    public String toString() {
        return name; // Return the country name as the string representation
    }
}


