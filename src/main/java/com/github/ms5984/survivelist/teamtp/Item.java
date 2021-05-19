/*
 * MIT License
 *
 * Copyright (c) 2021 Matt (ms5984) <https://github.com/ms5984>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.github.ms5984.survivelist.teamtp;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public enum Item {
    SLAP_SALMON;

    private ItemStack item;

    @SuppressWarnings("SameParameterValue")
    protected void setupItem(FileConfiguration config, @NotNull String itemName) {
        final ConfigurationSection section = config.getConfigurationSection("item." + itemName);
        if (section == null) throw new IllegalArgumentException("Section cannot be null");
        final Material material = Material.valueOf(section.getString("material"));
        this.item = new ItemStack(material);
        // "name" section
        Optional.ofNullable(section.getString("name")).ifPresent(name -> {
            final ItemMeta meta = item.getItemMeta();
            if (meta == null) throw new IllegalStateException();
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            item.setItemMeta(meta);
        });
        /*// "enchants" section
        Optional.ofNullable(section.getConfigurationSection("enchants")).ifPresent(enchantsSection -> {
            final ItemMeta meta = item.getItemMeta();
            if (meta == null) throw new IllegalStateException();
            for (String key : enchantsSection.getKeys(false)) {
                final Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(key));
                if (enchantment == null) continue;
                meta.addEnchant(enchantment, enchantsSection.getInt(key), true);
            }
            item.setItemMeta(meta);
        });*/
        // manually set enchant for now
        final ItemMeta meta = item.getItemMeta();
        if (meta == null) throw new IllegalStateException();
        meta.addEnchant(Enchantment.KNOCKBACK, 3, true);
        item.setItemMeta(meta);
    }

    public boolean checkHas(Player player) {
        return player.getInventory().containsAtLeast(item, 1);
    }

    public void give(Player player) {
        player.getInventory().addItem(new ItemStack(item));
    }
}
