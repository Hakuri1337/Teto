package client.feature;


import java.util.List;

public class Category {

    public static String combat = "战斗";
    public static String move = "移动";
    public static String block = "方块";
    public static String inventory = "背包";
    public static String render = "视觉";
    public static String misc = "杂项";

    public static List<String> getAll() {
        return List.of(combat, move, block, inventory, render, misc);
    }
}

