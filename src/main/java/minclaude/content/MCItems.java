package minclaude.content;

import arc.graphics.Color;
import arc.struct.Seq;
import mindustry.type.Item;

/** Ressources du mod. Le nom interne reçoit automatiquement le préfixe « minclaude- » au chargement du mod. */
public final class MCItems{
    public static Item cobalt;

    /** Toutes les ressources du mod, dans l'ordre de déclaration (utilisé par les tests et le générateur de sprites). */
    public static final Seq<Item> all = new Seq<>();

    private MCItems(){}

    public static void load(){
        cobalt = add(new Item("cobalt", Color.valueOf("3f6fd8")){{
            hardness = 3;
            cost = 1.2f;
            charge = 0.1f;
        }});
    }

    private static Item add(Item item){
        all.add(item);
        return item;
    }
}
