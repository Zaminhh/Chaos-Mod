package dev.chaosclient;

public enum Module {
    ESP("Render", true, "Highlights selected entities, labels, health and tracers."),
    XRAY("Render", false, "Highlights configured valuable blocks through terrain."),
    FULLBRIGHT("Render", true, "Raises client gamma for clear vision in darkness."),
    FREECAM("Render", false, "Moves the local camera while server position stays fixed."),
    FLY("Movement", false, "Provides configurable creative-style air movement."),
    SPEED("Movement", false, "Multiplies horizontal movement speed."),
    NO_FALL("Movement", true, "Sends grounded state before fall damage is applied."),
    NO_SLOW("Movement", true, "Removes item-use movement slowdown."),
    SPRINT("Movement", true, "Automatically sprints while moving forward."),
    STEP("Movement", false, "Raises the automatic step height."),
    KILLAURA("Combat", false, "Attacks nearby targets at the configured CPS and reach."),
    REACH("Combat", false, "Extends client entity and block targeting distance."),
    AUTO_TOTEM("Combat", true, "Moves a totem into the offhand when needed."),
    AUTO_ARMOR("Combat", true, "Equips armor into empty equipment slots."),
    VELOCITY("Combat", false, "Cancels local knockback motion updates."),
    SCAFFOLD("World", false, "Places hotbar blocks beneath the player."),
    SAFE_WALK("World", true, "Stops horizontal movement at unsupported edges."),
    FAST_PLACE("World", true, "Removes the client place delay."),
    FAST_BREAK("World", true, "Accelerates client block breaking progress."),
    CHEST_STEALER("World", false, "Quick-moves container contents into inventory."),
    AUTO_TOOL("World", true, "Selects the fastest hotbar tool for the aimed block.");

    public final String category;
    public final boolean defaultEnabled;
    public final String description;

    Module(String category, boolean defaultEnabled, String description) {
        this.category = category;
        this.defaultEnabled = defaultEnabled;
        this.description = description;
    }
}
