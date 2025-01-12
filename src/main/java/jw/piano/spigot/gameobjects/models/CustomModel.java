package jw.piano.spigot.gameobjects.models;

import jw.piano.api.data.PluginConfig;
import jw.piano.api.data.PluginConsts;
import jw.piano.spigot.gameobjects.factories.ArmorStandFactory;
import jw.fluent.api.spigot.gameobjects.api.GameObject;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public abstract class CustomModel extends GameObject
{
     private ArmorStand armorStand;
     private ItemStack itemStack;
     private Location location;

     protected boolean isPressed;

     public CustomModel(Location location, String guid)
     {
          this.armorStand = ArmorStandFactory.create(location, guid);
          this.getArmorStand().setSmall(true);
          this.itemStack  = new ItemStack(PluginConsts.SKINS_MATERIAL,1);
          this.location = location;
     }

     public boolean isPressed()
     {
          return isPressed;
     }

     public abstract void  press(int id, int velocity);

     public abstract void release(int id, int velocity);

     public ArmorStand getArmorStand()
     {
          return  armorStand;
     }

     public void setCustomModelData(int id)
     {
          ItemMeta meta = itemStack.getItemMeta();
          meta.setCustomModelData(id);
          itemStack.setItemMeta(meta);
          armorStand.setHelmet(itemStack);
     }

     public Location getLocation(){return location;}

}
