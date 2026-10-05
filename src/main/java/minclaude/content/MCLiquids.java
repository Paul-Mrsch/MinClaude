package minclaude.content;

import arc.graphics.Color;
import arc.struct.Seq;
import mindustry.content.StatusEffects;
import mindustry.type.Liquid;

/** Liquides du mod. */
public final class MCLiquids{
    public static Liquid brine, nitrogen;

    public static final Seq<Liquid> all = new Seq<>();

    private MCLiquids(){}

    public static void load(){
        brine = add(new Liquid("brine", Color.valueOf("7fb9c9")){{
            heatCapacity = 0.45f;
            temperature = 0.5f;
            viscosity = 0.6f;
            coolant = false;
        }});
        // Azote liquide : meilleur refroidissant que le cryofluide, gèle les ennemis.
        nitrogen = add(new Liquid("liquid-nitrogen", Color.valueOf("bfeaff")){{
            heatCapacity = 1.1f;
            temperature = 0.12f;
            effect = StatusEffects.freezing;
            lightColor = Color.valueOf("8fd9ff").a(0.2f);
            boilPoint = 0.4f;
            gasColor = Color.valueOf("e6f7ff");
        }});
    }

    private static Liquid add(Liquid l){
        all.add(l);
        return l;
    }
}
