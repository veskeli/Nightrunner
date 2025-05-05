package net.veskeli.nightrunner.healthsystem;

public interface IHealthStats {
    int getHealth();

    void setMaxHealth(int health);

    void addMaxHealth(int amount);

    void subtractMaxHealth(int amount);
}
