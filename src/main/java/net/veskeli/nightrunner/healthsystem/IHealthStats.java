package net.veskeli.nightrunner.healthsystem;

public interface IHealthStats {
    int getHealth();

    void setMaxHealth(int health);

    void addMaxHealth(int amount);

    void subtractMaxHealth(int amount);

    float getCurrentReviveHealth();

    void setCurrentReviveHealth(float health);

    float getReviveItemMaxHealth();

    void setReviveItemMaxHealth(float health);

    float getReviveHealthDegradeStep();

    void setReviveHealthDegradeStep(float step);

    boolean hasPendingSelfRevive();

    void setPendingSelfRevive(boolean pending);

    String getPendingSelfReviveSourceId();

    void setPendingSelfReviveSourceId(String sourceId);
}
