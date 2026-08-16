package com.mousejava.simplemsgplugin.chatgroups;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class GroupManager {
    private static final List<Group> groups = new ArrayList<>();

    public static Group createGroup(String name, GroupPlayer owner) {
        Group group = new Group(name, owner);
        groups.add(group);
        return group;
    }

    public static boolean deleteGroup(UUID ownerId) {
        Group removedGroup = groups.stream()
                .filter(group -> group.getOwner().getId().equals(ownerId))
                .findFirst()
                .orElse(null);

        boolean removed = groups.remove(removedGroup);
        if (removed && removedGroup != null) {
            removedGroup.sendMessage("messages.privatechat.delete_successfully", null, null);
            removedGroup.updateCommands();
        }

        return removed;
    }

    public static Group getGroup(UUID groupId) {
        return groups.stream()
                .filter(group -> group.getId().equals(groupId))
                .findFirst()
                .orElse(null);
    }

    public static Group findGroupByPlayer(UUID playerId) {
        return groups.stream()
                .filter(group -> group.getPlayers().stream()
                        .anyMatch(player -> player.getId().equals(playerId)))
                .findFirst()
                .orElse(null);
    }

    public static List<Group> listGroups() {
        return groups;
    }
}
