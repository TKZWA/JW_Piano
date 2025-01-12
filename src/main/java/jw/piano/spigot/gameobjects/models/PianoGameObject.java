package jw.piano.spigot.gameobjects.models;

import jw.piano.api.data.models.PianoSkin;
import jw.piano.api.enums.PianoEffect;
import jw.piano.spigot.gameobjects.factories.ArmorStandFactory;
import jw.piano.spigot.gameobjects.effects.EffectManager;
import jw.fluent.api.spigot.gameobjects.api.GameObject;
import jw.fluent.api.utilites.math.collistions.HitBox;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.Comparator;

@Getter
public class PianoGameObject extends GameObject {

    private PianoKeyModel[] pianoKeys = new PianoKeyModel[88];
    private final PianoPedalModel[] pianoPedals = new PianoPedalModel[3];
    private HitBox openViewHitBox;
    private ArmorStand pianoArmorStand;
    private EffectManager effectManager;
    private BenchGameObject pianoBench;
    private PianoInteractionHandler pianoInteractionHandler;

    private String guid;

    public PianoGameObject(String guid) {
        effectManager = new EffectManager();
        pianoInteractionHandler = new PianoInteractionHandler(this);
        this.guid = guid;
    }


    public void invokeNote(int pressed, int index, int velocity) {
        if (index < PianoConsts.MIDI_KEY_OFFSET)
            return;
        if (index - PianoConsts.MIDI_KEY_OFFSET > pianoKeys.length - 1)
            return;

        if (pressed != 0) {
            pianoKeys[index - PianoConsts.MIDI_KEY_OFFSET].press(index, velocity);

        } else
            pianoKeys[index - PianoConsts.MIDI_KEY_OFFSET].release(index, velocity);
    }


    public void invokePedal(int isPressed, int index, int velocity) {
        final var pedal = switch (index) {
            case 64 -> pianoPedals[2];
            case 65 -> pianoPedals[1];
            case 67 -> pianoPedals[0];
            default -> null;
        };
        if (pedal == null)
            return;
        if (isPressed == PianoConsts.PRESSED_CODE)
            pedal.press(index, velocity);
        else
            pedal.release(index, velocity);
    }

    public void create(Location location) {
        effectManager.create();
        location.setYaw(0);
        location.setPitch(0);
        pianoBench = new BenchGameObject(location, guid);
        pianoArmorStand = ArmorStandFactory.create(location.clone(), guid);
        setPianoSkin(new PianoSkin(109,"Grand piano"));
        openViewHitBox = new HitBox(location.clone().add(-0.7, 1.7, -0.1), location.clone().add(0.3, 2, 0.1));
        openViewHitBox.show();

        //Creating pedals
        for (int i = 0; i < 3; i++) {
            pianoPedals[i] = new PianoPedalModel(guid, location.clone().add(-0.4 + (i * 0.20), -0.1, 0.1f));
        }
        //Creating keys
        var startKeysLocation = location.clone().add(-1.5, -0.4, 0.3f);
        startKeysLocation.setDirection(new Vector(0, 1, 0));
        var key = 1;
        for (int i = 1; i <= 88; i++) {
            if (i > 3 && i < 88) {
                key = (i - 4) % 12;
            }
            if (i <= 3) {
                key = i + 8;
            }

            switch (key) {
                case 1, 3, 6, 8, 10:
                    pianoKeys[i - 1] = new PianoKeyModel(
                            guid,
                            pianoPedals[2],
                            startKeysLocation.clone().add(0.025f, 0.02f, -0.05f),
                            true,
                            i + PianoConsts.MIDI_KEY_OFFSET - 1);

                    break;
                default:
                    pianoKeys[i - 1] = new PianoKeyModel(
                            guid,
                            pianoPedals[2],
                            startKeysLocation.clone().add(0.05f, 0, 0),
                            false,
                            i + PianoConsts.MIDI_KEY_OFFSET - 1);
                    startKeysLocation = startKeysLocation.clone().add(0.05f, 0, 0);
                    break;
            }
            pianoKeys[i - 1].setEffectManager(effectManager);
        }
    }


    public boolean handlePlayerClick(Player player, Action action) {

        if (action != Action.LEFT_CLICK_AIR && action != Action.LEFT_CLICK_BLOCK) {
            return pianoBench.handleClick(player);
        }

        if (pianoInteractionHandler.handleClick(player.getEyeLocation())) {
            return true;
        }
        return false;
    }

    public void showGuiHitBox(boolean isVisible)
    {
        if(isVisible)
        {
            openViewHitBox.show();
        }
        else
        {
            openViewHitBox.hide();
        }
    }


    public void refreshKeys() {
        for (var key : pianoKeys) {
            key.release();
        }
    }

    public void onDestroy() {
        openViewHitBox.hide();
        for (var key : pianoKeys) {
            key.onDestroy();
        }
        for (var pedal : pianoPedals) {
            pedal.onDestroy();
        }
        pianoBench.onDestroy();
        effectManager.destroy();
        pianoArmorStand.remove();
    }

    public PianoKeyModel[] getSortedKeys() {
        var keys = getPianoKeys().clone();
        Arrays.sort(keys, new Comparator<PianoKeyModel>() {
            @Override
            public int compare(PianoKeyModel o1, PianoKeyModel o2) {
                return Boolean.compare(o1.isWhite(), o2.isWhite());
            }
        });
        return keys;
    }

    public void setPianoSkin(PianoSkin skin) {
        if (skin.getCustomModelId() == 0) {
            if (pianoArmorStand != null)
                pianoArmorStand.setHelmet(null);
        } else {
            pianoArmorStand.setHelmet(skin.getItemStack());
        }
    }

    public final void setEffect(PianoEffect pianoEffect) {
        effectManager.setEffect(pianoEffect);
    }

    public void setVolume(int volume) {
        for (var key : pianoKeys) {
            key.setVolume(volume);
        }
    }

    public PianoKeyModel[] getPianoKeys() {
        return pianoKeys;
    }

    public PianoPedalModel[] getPianoPedals() {
        return pianoPedals;
    }

    public PianoPedalModel getSustainPedal() {
        return pianoPedals[2];
    }

    public Location getPianoKeysCenterLocation() {
        return pianoKeys[pianoKeys.length / 2].getLocation();
    }
}
