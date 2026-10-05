package minclaude.content;

import arc.graphics.Color;
import arc.struct.Seq;
import mindustry.type.Item;

/** Ressources du mod. Le nom interne reçoit automatiquement le préfixe « minclaude- » au chargement du mod. */
public final class MCItems{
    public static Item cobalt, nickel, zinc, bauxite, aluminum, brass, chrome, steel, invar;

    /** Toutes les ressources du mod, dans l'ordre de déclaration (utilisé par les tests et le générateur de sprites). */
    public static final Seq<Item> all = new Seq<>();

    private MCItems(){}

    public static void load(){
        cobalt = add(new Item("cobalt", Color.valueOf("3f6fd8")){{
            hardness = 3;
            cost = 1.2f;
            charge = 0.1f;
        }});
        nickel = add(new Item("nickel", Color.valueOf("b9b48f")){{
            hardness = 2;
            cost = 0.9f;
        }});
        zinc = add(new Item("zinc", Color.valueOf("9fb7c4")){{
            hardness = 2;
            cost = 0.7f;
        }});
        bauxite = add(new Item("bauxite", Color.valueOf("b5653d")){{
            hardness = 2;
            cost = 0.6f;
        }});
        aluminum = add(new Item("aluminum", Color.valueOf("d6dde3")){{
            cost = 1f;
        }});
        brass = add(new Item("brass", Color.valueOf("e0b84f")){{
            cost = 1.1f;
        }});
        chrome = add(new Item("chrome", Color.valueOf("b8c7d9")){{
            hardness = 3;
            cost = 1.3f;
        }});
        steel = add(new Item("steel", Color.valueOf("8d98a8")){{
            cost = 1.5f;
        }});
        invar = add(new Item("invar", Color.valueOf("a9b49b")){{
            cost = 1.7f;
            charge = 0.05f;
        }});
    }

    private static Item add(Item item){
        all.add(item);
        return item;
    }
}
