package fr.cytech.pau.hia_jee.model;

public enum SponsorType {
    HARDWARE("Matériel PC, Claviers...)"),
    SOFTWARE("Logiciels et VPN"),
    DRINKS("Boissons et Food"),
    APPAREL("Vêtements et Mode"),
    OTHER("Autre");
      // Attribut pour stocker le "Joli Nom" (celui qu'on affiche dans le HTML)
    private final String displayName;

    SponsorType(String displayName) {
        this.displayName = displayName;
    }
    
    // Getter pour récupérer le nom d'affichage.
    public String getDisplayName() {
        return displayName;
    }
}