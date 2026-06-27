package joamonca.reactinator.commands._meta.data;

import joamonca.reactinator.util.get.Database;
import net.dv8tion.jda.api.events.Event;

public record CommandDataObject(
        Event event,
        Database database,
        String ownerID,
        String soundsSource
) {}