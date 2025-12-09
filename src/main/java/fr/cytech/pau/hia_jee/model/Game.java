package fr.cytech.pau.hia_jee.model;

public enum Game {
    
    LOL("League of Legends"),
    VALORANT("Valorant"),
    CSGO("Counter-Strike 2"),
    ROCKET("Rocket League"),
    FIFA("EA FC 24"),
    SSBU("Smash Bros Ultimate");

    // Attribut pour stocker le "Joli Nom" (celui qu'on affiche dans le HTML)
    private final String displayName;

   
    Game(String displayName) {
        this.displayName = displayName;
    }

    // Getter pour récupérer le nom d'affichage.
    public String getDisplayName() {
        return displayName;
    }
}