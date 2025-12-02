package fr.cytech.pau.hia_jee.model;

public enum SponsorType {
    HARDWARE("Matériel PC, Claviers...)"),
    SOFTWARE("Logiciels et VPN"),
    DRINKS("Boissons et Food"),
    APPAREL("Vêtements et Mode"),
    OTHER("Autre");

    private final String displayName;

    SponsorType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}