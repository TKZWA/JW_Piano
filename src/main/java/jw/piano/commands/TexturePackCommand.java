package jw.piano.commands;

import jw.piano.data.Settings;
import jw.spigot_fluent_api.desing_patterns.dependecy_injection.annotations.Inject;
import jw.spigot_fluent_api.desing_patterns.dependecy_injection.annotations.Injection;
import jw.spigot_fluent_api.fluent_commands.implementation.SimpleCommand;
import jw.spigot_fluent_api.fluent_commands.FluentCommand;
import jw.spigot_fluent_api.fluent_commands.old.interfaces.SimpleCommandConfig;


@Injection
public class TexturePackCommand implements SimpleCommandConfig {

    @Inject
    private Settings settings;

    @Override
    public SimpleCommand configureCommand() {

        return FluentCommand.create_OLDWAY("texturepack")
                .setDescription("use this command to get latest texturepack")
                .nextStep()
                .nextStep()
                .onPlayerExecute(event ->
                {
                    event.getPlayerSender().setTexturePack(settings.getTexturesURL());
                })
                .nextStep()
                .build();
    }
}
