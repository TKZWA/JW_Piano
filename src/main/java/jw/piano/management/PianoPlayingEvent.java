package jw.piano.management;

import jw.dependency_injection.InjectionManager;
import jw.piano.data.Settings;
import jw.piano.model.PianoKey;
import jw.piano.model.PianoModel;
import jw.piano.model.PianoPedal;
import jw.piano.utility.ArmorStandFactory;
import jw.piano.utility.PedalType;
import jw.task.TaskTimer;
import jw.utilites.MathHelper;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class PianoPlayingEvent {


    private final Player player;
    private final PianoModel pianoModel;
    private final Consumer<PianoPlayingEvent> pianoPlayingEventConsumer;
    private final TaskTimer taskTimer;
    private final int TICKS = 5;
    private final Location keyLocation;
    private final Settings settings;
    private final PianoPedal sustainPedal;
    private PianoKey highlightedKey;
    private LivingEntity pig;
    private PianoKey[] sortedKeys;

    public PianoPlayingEvent(Player player, PianoModel pianoModel, Consumer<PianoPlayingEvent> pianoPlayingEventConsumer) {
        this.player = player;
        this.pianoModel = pianoModel;
        this.pianoPlayingEventConsumer = pianoPlayingEventConsumer;
        this.keyLocation = pianoModel.getPianoKeysCenterLocation();
        this.sustainPedal = pianoModel.getPianoPedals()[2];
        this.settings = InjectionManager.getObject(Settings.class);
        this.sortedKeys = pianoModel.getPianoKeys().clone();
        Arrays.sort(this.sortedKeys, new Comparator<PianoKey>() {
            @Override
            public int compare(PianoKey o1, PianoKey o2)
            {
                return Boolean.compare(o1.isWhite(), o2.isWhite());
            }
        });
        this.taskTimer = new TaskTimer(3, (time, taskTimer1) ->
        {
            for(PianoKey pianoKey:sortedKeys)
            {
                if(pianoKey.getHitBox().isCollider(player.getEyeLocation(),10))
                {
                    if (highlightedKey != null && pianoKey != highlightedKey) {
                        highlightedKey.setHighlighted(false);
                    }
                    highlightedKey = pianoKey;
                    if (!highlightedKey.isPressed()) {
                        highlightedKey.setHighlighted(true);
                    }
                    if (!isPlayerInPianoRange(player.getEyeLocation()))
                    {
                        onStopPlaying();
                        taskTimer1.cancel();
                    }
                    break;
                }
            }
        });
        this.taskTimer.run();
        onStartPlaying();
    }




    public PianoModel getPianoModel()
    {
        return pianoModel;
    }

    public void onStopPlaying()
    {
        highlightedKey.setHighlighted(false);
        pianoPlayingEventConsumer.accept(this);
        pig.remove();
    }

    public void onStartPlaying()
    {
        Location location = this.pianoModel.getLocation().clone().add(0,1,1);
    //    pig = (LivingEntity)location.getWorld().spawnEntity(location, EntityType.CHICKEN);
        pig = ArmorStandFactory.createInvisibleArmorStand(location);
        pig.setInvisible(true);
        pig.setInvisible(true);
        pig.setAI(false);
        pig.setPassenger(player);
    }

    public Player getPlayer() {
        return player;
    }

    public void onPlayerClick(Location location)
    {
        for(PianoKey pianoKey:sortedKeys)
        {
            if(pianoKey.getHitBox().isCollider(location,10))
            {
                HitKey(pianoKey);
                break;
            }
        }

    }
    public void HitKey(PianoKey pianoKey) {

        float size = 0.3F;
        Color color = Color.fromRGB(MathHelper.getRandom(0,255), MathHelper.getRandom(0,255), MathHelper.getRandom(0,255));
        Particle.DustOptions options = new Particle.DustOptions(color, size);
       pianoKey.getLocation().getWorld().spawnParticle(Particle.REDSTONE,  pianoKey.getLocation().clone().add(0,2,0), 1,options);
        new TaskTimer(TICKS, (time, taskTimer1) ->
        {
            pianoKey.setPedalPressed(sustainPedal.isPressed());
            pianoKey.press(pianoKey.getIndex(), 100, 1);
        }).stopAfter(1).onStop(taskTimer1 ->
        {
            pianoKey.release(pianoKey.getIndex(), 0, 1);
            if(pianoKey == highlightedKey)
            {
                highlightedKey.setHighlighted(true);
            }
        }).run();
    }

    public void onPressPedal()
    {
        if(sustainPedal.isPressed())
        {
            sustainPedal.release(0,0,0);
        }
        else
        {
            sustainPedal.press(1,1,1);
        }
    }

    public Location getCursorLocation(Location eyeLocation) {
        double distance = eyeLocation.distance(keyLocation);
        distance = distance / 2.4f;
        Vector rayDirection = eyeLocation.clone().getDirection().multiply(distance);
        return eyeLocation.clone().add(rayDirection);
    }

    public boolean isPlayerInPianoRange(Location eyeLocation) {
        return eyeLocation.distance(keyLocation) < settings.maxDistanceFromPiano;
    }

    public PianoKey getClosestKey(Location eyeLocation,boolean a)
    {
        PianoKey[] keys = pianoModel.getPianoKeys();
        double rotation = MathHelper.yawToRotation(eyeLocation.getYaw());
        double headRotation = MathHelper.yawToRotation(eyeLocation.getPitch());

        double persent = MathHelper.getPersent(160,rotation);
        double yRotation = MathHelper.getPersent(16,headRotation-308);

        int index = (int)Math.round((keys.length-1)*persent);

        PianoKey pianoKey = keys[index%keys.length];

        if(!pianoKey.isBlack() && yRotation < 0.5f)
        {
            index+=1;
        }

        return keys[index%keys.length];
    }


    public PianoKey getClosestKey(Location location)
    {
        PianoKey[] keys = pianoModel.getPianoKeys();
        int keyIndex = keys.length / 2;
        double distance = getDistanceToKey(location, keys[keyIndex]);
        double distance2 = getDistanceToKey(location, keys[keyIndex+1]);
        int direction = distance < distance2? -1:1;
        double smalledDistance = Math.min(direction,distance2);
        while (true)
        {
            if(keyIndex<0 || keyIndex>keys.length-1)
            {
                keyIndex = 0;
                break;
            }

            double tempDistance =getDistanceToKey(location,keys[keyIndex+direction]);
            if (tempDistance < smalledDistance)
            {
                keyIndex +=direction;
                smalledDistance =tempDistance;
            }
            else
                break;
        }
        return keys[keyIndex];
    }

    private double getDistanceToKey(Location location,PianoKey pianoKey)
    {
        return Math.abs(location.getX() - pianoKey.getLocation().getX());
    }


}