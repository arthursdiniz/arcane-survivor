package com.arthurdiniz.arcanesurvivor.system;

import com.arthurdiniz.arcanesurvivor.model.Player;
import com.arthurdiniz.arcanesurvivor.weapon.*;
import java.util.*;

public final class UpgradeManager {
    public enum Kind { MAGIC_BOLT, ORBITING_ORB, FIRE_AURA, PIERCING_ARROW, SPEED, HEALTH, ARMOR, PICKUP, ATTACK_SPEED, DAMAGE }
    private final Random random;
    public UpgradeManager(Random random) { this.random = random; }

    public List<Kind> choices(List<Weapon> weapons) {
        List<Kind> available = new ArrayList<>(Arrays.asList(Kind.values()));
        available.removeIf(kind -> {
            Weapon weapon = find(weapons, kind);
            return weapon != null && weapon.level >= Weapon.MAX_LEVEL;
        });
        Collections.shuffle(available, random);
        return new ArrayList<>(available.subList(0, Math.min(3, available.size())));
    }

    public static String title(Kind kind, List<Weapon> weapons) {
        Weapon weapon = find(weapons, kind);
        return switch (kind) {
            case MAGIC_BOLT -> "Magic Bolt " + (weapon == null ? "unlock" : "Lv " + (weapon.level + 1));
            case ORBITING_ORB -> "Orbiting Orb " + (weapon == null ? "unlock" : "Lv " + (weapon.level + 1));
            case FIRE_AURA -> "Fire Aura " + (weapon == null ? "unlock" : "Lv " + (weapon.level + 1));
            case PIERCING_ARROW -> "Piercing Arrow " + (weapon == null ? "unlock" : "Lv " + (weapon.level + 1));
            case SPEED -> "Fleet Step  |  +10% speed";
            case HEALTH -> "Vitality  |  +20 max HP";
            case ARMOR -> "Iron Will  |  +1 armor";
            case PICKUP -> "Magnetism  |  +25% pickup range";
            case ATTACK_SPEED -> "Quick Casting  |  +10% attack speed";
            case DAMAGE -> "Arcane Force  |  +15% damage";
        };
    }

    public static void apply(Kind kind, Player player, List<Weapon> weapons) {
        Weapon existing = find(weapons, kind);
        if (existing != null) { existing.levelUp(); return; }
        switch (kind) {
            case MAGIC_BOLT -> weapons.add(new MagicBoltWeapon());
            case ORBITING_ORB -> weapons.add(new OrbitingOrbWeapon());
            case FIRE_AURA -> weapons.add(new FireAuraWeapon());
            case PIERCING_ARROW -> weapons.add(new PiercingArrowWeapon());
            case SPEED -> player.speed *= 1.1f;
            case HEALTH -> player.healAndIncreaseMax(20);
            case ARMOR -> player.armor++;
            case PICKUP -> player.pickupRadius *= 1.25f;
            case ATTACK_SPEED -> player.attackSpeedMultiplier *= 1.1f;
            case DAMAGE -> player.damageMultiplier *= 1.15f;
        }
    }

    private static Weapon find(List<Weapon> weapons, Kind kind) {
        String name = switch (kind) {
            case MAGIC_BOLT -> "Magic Bolt"; case ORBITING_ORB -> "Orbiting Orb";
            case FIRE_AURA -> "Fire Aura"; case PIERCING_ARROW -> "Piercing Arrow";
            default -> null;
        };
        if (name == null) return null;
        for (Weapon weapon : weapons) if (weapon.name.equals(name)) return weapon;
        return null;
    }
}
