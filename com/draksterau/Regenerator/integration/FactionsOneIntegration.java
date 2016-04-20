///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package com.draksterau.Regenerator.integration;
//
//import com.draksterau.Regenerator.config.integrationConfigHandler;
//import com.massivecraft.factions.Board;
//import com.massivecraft.factions.FLocation;
//import com.massivecraft.factions.FPlayer;
//import com.massivecraft.factions.FPlayers;
//import com.massivecraft.factions.Faction;
//import com.massivecraft.factions.Factions;
//import com.massivecraft.factions.struct.Rel;
//import org.bukkit.ChatColor;
//import org.bukkit.Chunk;
//import org.bukkit.Location;
//import org.bukkit.entity.Player;
//
//
///**
// *
// * @author draks
// */
//public final class FactionsOneIntegration extends Integration {
//
//    // INTEGRATION METHODS ARE HERE.
//
//    @Override
//    public String getPermissionRequiredToRegen(Player player, Chunk chunk) {
//            return ("regenerator.regen.factionsone." + getPlayerRelation(player,chunk).name());
//    }
//    
//    @Override
//    public String getPlayerRegenReason(Player player, Chunk chunk) {
//        if (player.hasPermission(getPermissionRequiredToRegen(player,chunk))) {
//            return (ChatColor.GREEN + "You have regenerated the chunk at: " + ChatColor.BLUE + chunk.getX() + ChatColor.GRAY + "," + ChatColor.BLUE + chunk.getZ() + ChatColor.GREEN + " in " + ChatColor.BLUE + getFactionForChunk(chunk).getTag() + ChatColor.GREEN + " territory.");
//        } else {
//            return ChatColor.RED + "You cannot regenerate the chunk at " + ChatColor.BLUE + chunk.getX() + ChatColor.GRAY + "," + ChatColor.BLUE + chunk.getZ() + ChatColor.RED + " manually as your relation is " + getPlayerRelation(player,chunk).name()+ " to " + getFactionForChunk(chunk).getTag() + ".";
//        }
//    }
//    
//    @Override
//    public boolean isChunkClaimed(Chunk chunk) {
//        if (!getFactionForChunk(chunk).isNone()) {
//            return true;
//        } else {
//            return false;
//        }
//    }
//
//    @Override
//    public boolean canPlayerRegen(Player player, Chunk chunk) {
//        if (isChunkClaimed(chunk)) {
//            if (player.hasPermission("regenerator.regen.factionsone." + getPlayerRelation(player,chunk).name())) {
//                return true;
//            } else {
//                return false;
//            }
//        } else {
//            return false;
//        }
//    }
//
//    @Override
//    public boolean shouldChunkAutoRegen(Chunk chunk) {
//        if (!isChunkClaimed(chunk)) {
//            return true;
//        } else {
//            return false;
//        }
//    }
//    @Override
//    public void validateConfig() {
//        integrationConfigHandler iConfig = new integrationConfigHandler(RegeneratorPlugin, this);
//        iConfig.saveDefaultIntegrationConfig();
//        if (!iConfig.integrationConfig.isSet("notice")) {
//            iConfig.integrationConfig.set("notice", "FactionsOne integration does not utilize a config file. All factions except the wilderness are protected from automatic regeneration.");
//        }
//        iConfig.saveIntegrationConfig();
//    }
//    
//    // FACTIONS SPECIFIC METHODS ARE HERE.
//    
//    public Faction getFactionForChunk(Chunk chunk) {
//        Location loc = new Location(chunk.getWorld(), chunk.getX() * 16, 100.0, chunk.getZ() * 16);
//        FLocation floc = new FLocation(loc);
//        return Board.getFactionAt(floc);
//    }
//    
//    public boolean claimExists(String name) {
//        Faction faction = Factions.i.getByTag(name);
//        return faction instanceof Faction;
//    }
//
//    public Rel getFactionRole(FPlayer player) {
//        return player.getRole();
//    }
//    
//    public static Rel getRoleForString(String role) {        
//        for (Rel roleSelected : Rel.values()) {
//            if (roleSelected.name().toLowerCase().equals(role.toLowerCase())) {
//                return roleSelected;
//            }
//        }
//        return null;
//    }
//    
//    public Rel getPlayerRelation(Player player, Chunk chunk) {
//        Faction pFaction = FPlayers.i.get(player).getFaction();
//        Faction cFaction = getFactionForChunk(chunk);
//        return pFaction.getRelationTo(cFaction, true);
//    }
//}
