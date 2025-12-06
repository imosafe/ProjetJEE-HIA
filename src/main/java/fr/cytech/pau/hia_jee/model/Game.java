package fr.cytech.pau.hia_jee.model;

public enum Game {
    LOL("League of Legends"),
    VALORANT("Valorant"),
    CSGO("Counter-Strike 2"),
    ROCKET("Rocket League"),
    FIFA("EA FC 24"),
    SSBU("Smash Bros Ultimate");

    private final String displayName;

    Game(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
