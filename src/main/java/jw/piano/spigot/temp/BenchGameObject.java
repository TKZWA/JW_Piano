package jw.piano.spigot.temp;

import jw.piano.api.data.models.PianoSkin;
import jw.piano.api.enums.PianoKeysConst;
import jw.fluent.api.spigot.gameobjects.api.GameObject;
import jw.fluent.api.spigot.gameobjects.api.ModelRenderer;

public class BenchGameObject extends GameObject
{

    ModelRenderer renderer;


    @Override
    public void onRotation(int degree) {
        var distance = Math.sqrt(offset.getX()*offset.getX() + offset.getZ()*offset.getZ());

        var locX =  Math.cos(Math.toRadians(degree)) * distance;
        var locZ =   Math.sin(Math.toRadians(degree)) * distance;

        var dir = getParent().getLocation().getDirection().clone();
        var d = dir.multiply(offset);
       // FluentLogger.log("dir",dir);
        var finalLoz = getParent().getLocation().clone().add(d.getX(),offset.getY(),d.getZ());
        setLocation(finalLoz);
    }

    @Override
    public void onCreated()
    {
        var benchModel = new PianoSkin(PianoKeysConst.BENCH.getId(), "bench");
        renderer = addGameComponent(new ModelRenderer());
        renderer.setCustomModel(benchModel.getItemStack());
        setOffset(1,0.6,0);
    }
}
