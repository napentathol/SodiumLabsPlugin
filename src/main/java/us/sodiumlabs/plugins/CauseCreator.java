package us.sodiumlabs.plugins;

import org.spongepowered.api.event.cause.Cause;

public interface CauseCreator {
    Cause createNamedCause(final String namedCause);
}
